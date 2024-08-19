package com.rebelrobotics.scoutingapp;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.StorageClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageUploader {
    public static void uploadImage(String localFilePath, String destinationFileName) throws IOException, IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Bucket bucket = StorageClient.getInstance().bucket();
        Path localImagePath = Paths.get(localFilePath);
        BlobInfo blobInfo = BlobInfo.newBuilder("testing-6a1fc.appspot.com", destinationFileName)
                .setContentType(Files.probeContentType(localImagePath))
                .build();

        bucket.create(blobInfo.getName(), Files.readAllBytes(localImagePath), blobInfo.getContentType());

        System.out.println("Image uploaded successfully to: " + destinationFileName);
    }
}
