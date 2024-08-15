package com.rebelrobotics.scoutingapp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.sun.javafx.collections.ElementObservableListDecorator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutionException;

public class firebaseControllerFX {
    ObservableList<String> teamList;
    private TeamController teamController;
    @FXML
    private TextField number;
    @FXML
    private TextArea notes;
    @FXML
    private TextField name;
    @FXML
    private TextField school;
    @FXML
    private Label nameLabel;
    @FXML
    private Label numberLabel;
    @FXML
    private Label schoolLabel;
    @FXML
    private Text notesLabel;
    @FXML
    public void makeTeam(){
        teamController.saveTeam(new Team(name.getText(), Integer.parseInt(number.getText()), school.getText(), notes.getText()));

    }
    Firestore db;
    public firebaseControllerFX(){
        teamList = FXCollections.observableArrayList();
        try {
            db = FirestoreInitializer.initializeFirestore();
            teamController = new TeamController(db);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML
    private ListView<Team> teamListView;



    // Observable list to hold team names
    private ObservableList<String> teamNames = FXCollections.observableArrayList();
    private ObservableList<Team> teamObservableList = FXCollections.observableArrayList();
    @FXML
    ImageView imageView;
    private byte[] downloadImageFromFirebase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("C:\\Users\\jtorgerson\\Downloads\\scout-8125e-c65d86f209dc.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket("scout-8125e.appspot.com")
                .build();

        FirebaseApp.initializeApp(options);

        // Get the bucket
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Bucket bucket = storage.get("testing-6a1fc.appspot.com");

        // Download the image
        Blob blob = bucket.get("Screenshot 2024-04-09 102545.png"); // Replace with your image name
        return blob.getContent();
    }
    private void setImageView() throws IOException {
        imageView.setFitWidth(300); // Adjust width as needed
        imageView.setPreserveRatio(true);

        // Download image from Firebase Storage
        byte[] imageData = downloadImageFromFirebase();

        if (imageData != null) {
            // Convert byte array to InputStream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);

            // Load the image from the InputStream
            Image image = new Image(inputStream);

            // Set the image in the ImageView
            imageView.setImage(image);
        } else {
            System.out.println("Image didn't download");
        }
    }

    @FXML
    public void initialize() throws IOException {
        listenForTeamUpdates();
        teamListView.setCellFactory(new Callback<ListView<Team>, ListCell<Team>>() {
            @Override
            public ListCell<Team> call(ListView<Team> listView) {
                return new ListCell<Team>() {
                    @Override
                    protected void updateItem(Team team, boolean empty) {
                        super.updateItem(team, empty);
                        if (empty || team == null) {
                            setText(null);
                        } else {
                            setText(team.getName()); // Display the team name
                        }
                    }
                };
            }
        });

        teamListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Team>() {
            @Override
            public void changed(ObservableValue<? extends Team> observable, Team oldValue, Team newValue) {
                // This code will run when a new item is selected
                if (newValue != null) {
                    System.out.println("Selected team: " + newValue.getName());
                    nameLabel.setText(newValue.getName());
                    schoolLabel.setText(newValue.getSchool());
                    notesLabel.setText(newValue.getNotes());
                    numberLabel.setText(String.valueOf(newValue.getNumber()));
                }
            }
        });
        try {
            loadTeams();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //setImageView();
    }
    public void handleUploadButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image to Upload");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                ImageUploader.uploadImage(selectedFile.getAbsolutePath(), selectedFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadTeams() throws InterruptedException, ExecutionException {
        List<Team> teams = teamController.getAllTeams();
        // Add each team name to the observable list
        teamObservableList.addAll(teams);


        teamListView.setItems(teamObservableList);

    }
    private ListenerRegistration listenerRegistration;
    private void listenForTeamUpdates() {
        CollectionReference teamsCollection = db.collection("teams");

        listenerRegistration = teamsCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }

            List<Team> updatedTeamList = new ArrayList<>();

            for (DocumentSnapshot document : snapshots.getDocuments()) {
                Team team = document.toObject(Team.class);
                updatedTeamList.add(team);
            }

            // Update the ListView on the JavaFX Application Thread
            Platform.runLater(() -> {

                teamObservableList.setAll(updatedTeamList);
            });
        });
    }
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
