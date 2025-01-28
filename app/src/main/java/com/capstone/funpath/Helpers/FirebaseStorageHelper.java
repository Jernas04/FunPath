package com.capstone.funpath.Helpers;

import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class FirebaseStorageHelper {
    private AppCompatActivity activity;

    public FirebaseStorageHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void uploadImage(Uri imageUri, final UploadCallback callback) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userUID = firebaseAuth.getCurrentUser().getUid();
        if (imageUri != null && !userUID.isEmpty()) {
            // Create a storage reference
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference("images/" + userUID);

            // Upload the image
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Use the download URL
                            String downloadUrl = uri.toString();
                            callback.onSuccess(downloadUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(activity, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onFailure(e);
                    });
        }
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception exception);
    }
}
