package com.capstone.funpath;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.capstone.funpath.Helpers.FirebaseStorageHelper;
import com.capstone.funpath.Helpers.UserHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity_EditProfile extends AppCompatActivity {

    private static final String TAG = "Activity_EditProfile";
    private HashMap<String, String> slpMap = new HashMap<>();  // To store key-value pairs (key as node ID, value as name)
    ImageView iv_profile;
    private EditText edt_kid_name, edt_kid_email;
    private Spinner sp_age, sp_gender, sp_school_level, sp_slp;
    UserHelper userHelper;
    final String[] slp_selected = new String[1];
    Uri imageUri;
    String imageURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Applying window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initializing views
        edt_kid_name = findViewById(R.id.edt_kid_name);
        edt_kid_email = findViewById(R.id.edt_kid_email);
        sp_age = findViewById(R.id.sp_age);
        sp_gender = findViewById(R.id.sp_gender);
        sp_school_level = findViewById(R.id.sp_school_level);
        sp_slp = findViewById(R.id.sp_slp);
        ImageButton ib_back = findViewById(R.id.ib_back);
        Button buttonbtnUpdate = findViewById(R.id.btnUpdate);
        iv_profile = findViewById(R.id.iv_profile);

        userHelper = new UserHelper(helper -> {
            if (helper != null) {
                edt_kid_name.setText(helper.getName());
                edt_kid_email.setText(helper.getEmail());
                imageURL = helper.getImageURL();
                if (!imageURL.isEmpty()) Glide.with(this).load(helper.getImageURL()).circleCrop().into(iv_profile);
                int age_position = ((ArrayAdapter<String>) sp_age.getAdapter()).getPosition(helper.getAge());
                int gender_position = ((ArrayAdapter<String>) sp_gender.getAdapter()).getPosition(helper.getGender());
                int school_level_position = ((ArrayAdapter<String>) sp_school_level.getAdapter()).getPosition(helper.getSchoolLevel());
                sp_gender.setSelection(gender_position);
                sp_school_level.setSelection(school_level_position);
                sp_age.setSelection(age_position);
            }
        });

        // Reference to the Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Query to find users with userType 'slp'
        databaseReference.orderByChild("userType").equalTo("slp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // List to store just the names for Spinner display
                List<String> slpNames = new ArrayList<>();

                // Check if the query returned any data
                if (dataSnapshot.exists()) {
                    // Iterate over the results and add the users to the list
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();  // Firebase node key (user ID)
                        String name = snapshot.child("name").getValue(String.class);  // Assuming 'name' field

                        if (key != null && name != null) {
                            slpMap.put(key, name);
                            slpNames.add(name);
                        }
                        new UserHelper(helper -> {
                            if (helper != null) {
                                if (helper.getSLP().equals(key)) {
                                    sp_slp.setSelection(((ArrayAdapter<String>) sp_slp.getAdapter()).getPosition(name));
                                }
                            }
                        });
                    }

                    // Create an ArrayAdapter using the slpNames list
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(Activity_EditProfile.this, android.R.layout.simple_spinner_item, slpNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // Set the adapter to the Spinner
                    sp_slp.setAdapter(adapter);

                } else {
                    Log.d(TAG, "No users with userType 'slp' found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors here
                Log.e(TAG, "Error querying users", databaseError.toException());
            }
        });

        sp_slp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedName = (String) parentView.getItemAtPosition(position);
                slp_selected[0] = getKeyByName(selectedName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle case where nothing is selected if needed
            }
        });

        iv_profile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getContentLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });

        buttonbtnUpdate.setOnClickListener(v -> {
            if (imageUri == null) {
                updateUserFields(imageURL);
            } else {
                uploadImageToFirebase();
            }
        });

        ib_back.setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        // Check for internet connection before doing any network operation
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }
    }

    private ActivityResultLauncher<Intent> getContentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).circleCrop().into(iv_profile);
                }
            });

    // Helper method to get the key by name from the HashMap
    private String getKeyByName(String name) {
        for (HashMap.Entry<String, String> entry : slpMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void uploadImageToFirebase() {
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
                updateUserFields(downloadUrl);
            }

            @Override
            public void onFailure(Exception exception) {
                // Log the error for debugging purposes
                Log.e(TAG, "Image upload failed: ", exception);

                // Show a user-friendly message to inform the user that the upload failed
                StylishAlertDialog stylishDialog = new StylishAlertDialog(Activity_EditProfile.this, StylishAlertDialog.ERROR);
                stylishDialog.setTitleText("Upload Failed")
                        .setContentText("There was an error uploading the image. Please try again.")
                        .setConfirmText("OK")
                        .show();
            }
        });
    }

    private void updateUserFields(String imageURL) {
        String name = edt_kid_name.getText().toString();
        String email = edt_kid_email.getText().toString();
        String age = sp_age.getSelectedItem().toString();
        String gender = (String) sp_gender.getSelectedItem();
        String school_level = (String) sp_school_level.getSelectedItem();
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("age", age);
        updates.put("gender", gender);
        updates.put("level", school_level);
        updates.put("slp", slp_selected[0]);
        updates.put("imageURL", imageURL);
        userHelper.updateUserFields(this, updates);
    }

    // Method to check if the network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    // Method to show the 'No Internet' dialog
    private void showNoInternetDialog() {
        StylishAlertDialog stylishDialog = new StylishAlertDialog(this, StylishAlertDialog.ERROR);
        stylishDialog.setTitleText("No Internet")
                .setContentText("Please check your internet connection.")
                .setConfirmText("Retry")
                .setConfirmClickListener(dialog -> {
                    if (isNetworkAvailable()) {
                        dialog.dismiss();
                    } else {
                        showNoInternetDialog();
                    }
                })
                .show();
    }
}
