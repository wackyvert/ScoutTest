package com.rebelrobotics.scoutingapp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FirestoreInitializer {
    public static Firestore initializeFirestore() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("C:\\Users\\jtorgerson\\Downloads\\scout-8125e-firebase-adminsdk-4hius-43ca2b5c9f.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://scout-8125e-default-rtdb.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);
        return FirestoreClient.getFirestore();
    }
    public FirestoreInitializer() throws IOException {
    }



}
