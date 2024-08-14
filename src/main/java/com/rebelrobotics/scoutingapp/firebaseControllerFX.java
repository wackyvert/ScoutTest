package com.rebelrobotics.scoutingapp;

import com.google.cloud.firestore.*;
import com.sun.javafx.collections.ElementObservableListDecorator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    private ListView<String> teamListView;



    // Observable list to hold team names
    private ObservableList<String> teamNames = FXCollections.observableArrayList();



    @FXML
    public void initialize() {
        try {
            loadTeams();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        listenForTeamUpdates();
    }

    private void loadTeams() throws InterruptedException, ExecutionException {
        List<Team> teams = teamController.getAllTeams();
        for (Team team : teams) {
            // Add each team name to the observable list
            teamNames.add(team.getName());
        }

        // Set the items of the ListView to the list of team names
        teamListView.setItems(teamNames);

    }
    private ListenerRegistration listenerRegistration;
    private void listenForTeamUpdates() {
        CollectionReference teamsCollection = db.collection("teams");

        listenerRegistration = teamsCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }

            List<String> updatedTeamList = new ArrayList<>();

            for (DocumentSnapshot document : snapshots.getDocuments()) {
                Team team = document.toObject(Team.class);
                updatedTeamList.add(team.getName() + " (" + team.getNumber() + ") - " + team.getSchool());
            }

            // Update the ListView on the JavaFX Application Thread
            Platform.runLater(() -> {

                teamNames.setAll(updatedTeamList);
            });
        });
    }
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
