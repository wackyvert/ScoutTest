package com.rebelrobotics.scoutingapp;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

import static com.rebelrobotics.scoutingapp.firebaseControllerFX.teamController;


public class NewTeamController {
    @FXML
    TextField teamName;
    @FXML
    TextField teamNumber;
    @FXML
    TextField teamSchool;
    @FXML
    TextArea teamNotes;
    public void saveTeam(){
        teamController.saveTeam(new Team(teamName.getText(), Integer.parseInt(teamNumber.getText()), teamSchool.getText(), teamNotes.getText()));
    }
    public String getFileExtension(File file) {
        String fileName = file.getName();
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // Empty extension for files without a dot
        }
        return fileName.substring(lastIndexOf);
    }
    public void handleUploadButtonAction() {
        if(!teamNumber.getText().isBlank()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image to Upload");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                try {
                    ImageUploader.uploadImage(selectedFile.getAbsolutePath(), teamNumber.getText() + getFileExtension(selectedFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Alert noTeamNumberAlert = new Alert(Alert.AlertType.ERROR);
            noTeamNumberAlert.setTitle("Error");
            noTeamNumberAlert.setHeaderText("No Team Number");
            noTeamNumberAlert.setContentText("You need to set a team number before you can upload an image.");
            noTeamNumberAlert.showAndWait();
        }
    }



}
