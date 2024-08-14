package com.rebelrobotics.scoutingapp;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.api.core.ApiFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TeamController {
    private Firestore db;

    // Constructor to initialize Firestore
    public TeamController(Firestore db) {
        this.db = db;
    }

    // Method to save a team to Firestore
    public void saveTeam(Team team) {
        CollectionReference teams = db.collection("teams");
        DocumentReference docRef = teams.document(String.valueOf(team.getNumber())); // Use team number as document ID
        docRef.set(team);
    }

    // Method to get all teams from Firestore
    public List<Team> getAllTeams() throws InterruptedException, ExecutionException {
        CollectionReference teams = db.collection("teams");
        ApiFuture<QuerySnapshot> querySnapshot = teams.get();

        List<Team> teamList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Team team = document.toObject(Team.class);
            teamList.add(team);
        }
        return teamList;
    }

    // Method to get a specific team by its number
    public Team getTeamByNumber(int number) throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection("teams").document(String.valueOf(number));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(Team.class);
        } else {
            return null; // Team not found
        }
    }

    // Method to update a team's information
    public void updateTeam(Team team) {
        CollectionReference teams = db.collection("teams");
        DocumentReference docRef = teams.document(String.valueOf(team.getNumber()));
        docRef.set(team); // This will overwrite the existing document
    }

    // Method to delete a team by its number
    public void deleteTeam(int number) {
        DocumentReference docRef = db.collection("teams").document(String.valueOf(number));
        docRef.delete();
    }
}
