package com.capstone.funpath;

import static com.capstone.funpath.asr.Recorder.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.capstone.funpath.Helpers.FirebaseStorageHelper;
import com.capstone.funpath.Helpers.UserHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Activity_CreateProfile extends AppCompatActivity {

    private EditText edt_kid_name, edt_kid_email;
    TextInputEditText edt_kid_password, edt_kid_confirm_password;
    private Spinner sp_age, sp_gender, sp_school_level, sp_slp;
    private FirebaseAuth firebaseAuth;
    ImageView iv_profile;
    private DatabaseReference databaseReference;
    private final Map<String, String> slpMap = new HashMap<>();
    private String slpSelected;
    private Uri imageUri;
    String imageURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        // Initialize Firebase references
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        initializeUI();
        loadSLPUsers();
    }

    private void initializeUI() {
        edt_kid_name = findViewById(R.id.edt_kid_name);
        sp_age = findViewById(R.id.sp_age);
        sp_gender = findViewById(R.id.sp_gender);
        sp_school_level = findViewById(R.id.sp_school_level);
        sp_slp = findViewById(R.id.sp_slp);
        edt_kid_email = findViewById(R.id.edt_kid_email);
        edt_kid_password = findViewById(R.id.edt_kid_password);
        edt_kid_confirm_password = findViewById(R.id.edt_kid_confirm_password);
        iv_profile = findViewById(R.id.iv_profile);

        iv_profile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getContentLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });

        // Back button listener
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(v -> finish());

        // When user clicks cancel
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("profileInProgress", false);  // Reset profile creation status
            editor.apply();
            finish();  // Close current activity and return to previous one
        });

        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> validateAndSignUp());
    }

    private void loadSLPUsers() {
        databaseReference.orderByChild("userType").equalTo("slp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> slpNames = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        String name = snapshot.child("name").getValue(String.class);

                        if (key != null && name != null) {
                            slpMap.put(key, name);
                            slpNames.add(name);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(Activity_CreateProfile.this, android.R.layout.simple_spinner_item, slpNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_slp.setAdapter(adapter);

                    sp_slp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedName = (String) parent.getItemAtPosition(position);
                            slpSelected = getKeyByName(selectedName);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            slpSelected = null;
                        }
                    });

                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private String getKeyByName(String name) {
        for (Map.Entry<String, String> entry : slpMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return null;  // Return null if no matching name is found
    }

    private void validateAndSignUp() {
        if (!isInternetAvailable()) {
            showNoInternetDialog();
            return;
        }

        String name = edt_kid_name.getText().toString().trim();
        String age = sp_age.getSelectedItem().toString();
        String gender = (String) sp_gender.getSelectedItem();
        String level = (String) sp_school_level.getSelectedItem();
        String email = edt_kid_email.getText().toString().trim();
        String password = edt_kid_password.getText().toString().trim();
        String confirmPassword = edt_kid_confirm_password.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty() || gender == null || level == null || slpSelected == null || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showDialog("Warning", "Please fill in all the fields.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showDialog("Warning", "Passwords do not match.");
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            sendVerificationEmail(user, name, age, gender, level, email, password);
                        }
                    } else {
                        handleAuthError(task.getException());
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user, String name, String age, String gender, String level, String email, String password) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showDialog("Email Verification", "A verification email has been sent to " + email + ". Please verify your email to continue.");
                checkEmailVerification(user, name, age, gender, level, email);
            } else {
                showDialog("Error", "Failed to send verification email. Please try again.");
            }
        });
    }

    private void checkEmailVerification(FirebaseUser user, String name, String age, String gender, String level, String email) {
        // Atomic booleans to track verification and cancellation state
        AtomicBoolean isVerified = new AtomicBoolean(false);
        AtomicBoolean isCanceled = new AtomicBoolean(false);  // Track cancellation state

        // Create and show the StylishAlertDialog with progress
        StylishAlertDialog stylishDialog = new StylishAlertDialog(this, StylishAlertDialog.PROGRESS);
        stylishDialog.setTitleText("Waiting for Email Verification")
                .setContentText("Please verify your email to complete registration.")
                .setConfirmText("Cancel")  // Text for the cancel button
                .setConfirmClickListener(dialog -> dialog.dismiss())
                .setCancellable(true)
                .show();

        // Start a background thread to periodically check email verification status
        new Thread(() -> {
            while (!isVerified.get() && !isCanceled.get()) {  // Stop if canceled
                user.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && user.isEmailVerified()) {
                        isVerified.set(true);
                        runOnUiThread(() -> {
                            stylishDialog.dismiss();  // Dismiss the dialog on successful verification
                            if (imageUri == null) {
                                // If no image selected, save user data
                                saveUserData(user.getUid(), name, age, gender, level, slpSelected, email, imageURL);
                            } else {
                                // Upload image if selected
                                uploadImageToFirebase(user.getUid(), name, age, gender, level, slpSelected, email);
                            }
                        });
                    }
                });

                try {
                    // Check every 3 seconds
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Verification check interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }

            // Handle the case when verification is canceled
            if (isCanceled.get()) {
                runOnUiThread(() -> {
                    // Notify the user that the verification was canceled
                    Toast.makeText(getApplicationContext(), "Email verification canceled.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private void saveUserData(String uid, String name, String age, String gender, String level, String slp, String email, String imageURL) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("age", age);
        userData.put("gender", gender);
        userData.put("level", level);
        userData.put("slp", slp);
        userData.put("email", email);
        userData.put("imageURL", imageURL);
        userData.put("userType", "kid");

        // Creating a nested map for the assessment data
        Map<String, Object> assessmentData = new HashMap<>();
        assessmentData.put("beginner", 0);
        assessmentData.put("intermediate", 0);
        assessmentData.put("advanced", 0);
        assessmentData.put("level", "beginner");

        // Adding the nested assessment map to the main userData map
        userData.put("assessment", assessmentData);

        databaseReference.child(uid).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateToHomePage();
                    } else {
                        Log.e("Firebase", "Failed to save user data.", task.getException());
                        showDialog("Error", "Failed to save user data.");
                    }
                });
    }

    private void uploadImageToFirebase(String uid, String name, String age, String gender, String level, String slpSelected, String email) {
        StylishAlertDialog stylishDialog = new StylishAlertDialog(this, StylishAlertDialog.PROGRESS);
        stylishDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        stylishDialog.setTitleText("Loading")
                .setCancellable(false)
                .show();
        FirebaseStorageHelper firebaseStorageHelper = new FirebaseStorageHelper(this);
        firebaseStorageHelper.uploadImage(imageUri, new FirebaseStorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                stylishDialog.dismiss();
                saveUserData(uid, name, age, gender, level, slpSelected, email, downloadUrl);
            }

            @Override
            public void onFailure(Exception exception) {
                // Log the error for debugging purposes
                Log.e(TAG, "Image upload failed: ", exception);

                // Show a user-friendly message to inform the user that the upload failed
                StylishAlertDialog stylishDialog = new StylishAlertDialog(Activity_CreateProfile.this, StylishAlertDialog.ERROR);
                stylishDialog.setTitleText("Upload Failed")
                        .setContentText("There was an error uploading the image. Please try again.")
                        .setConfirmText("OK")
                        .show();
            }
        });
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(this, Activity_Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleAuthError(Exception e) {
        if (e != null) {
            String message = e.getMessage();
            if (message != null && message.contains("email address is already in use")) {
                showDialog("Error", "This email address is already registered.");
            } else if (message != null && message.contains("Password should be at least")) {
                showDialog("Error", "Password is too weak.");
            } else {
                showDialog("Error", "Authentication failed. Please try again.");
            }
        }
    }

    // Check if there's internet connection
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Show the dialog when there is no internet connection
    private void showNoInternetDialog() {
        new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                .setTitleText("No Internet")
                .setContentText("Please check your internet connection and try again.")
                .show();
    }

    private void showDialog(String title, String message) {
        new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                .setTitleText(title)
                .setContentText(message)
                .show();
    }

    private ActivityResultLauncher<Intent> getContentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    iv_profile.setImageURI(imageUri);
                }
            });
}
