package com.capstone.funpath.Helpers;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.capstone.funpath.Adapters.DiagnosticsRecord;
import com.capstone.funpath.Exercises.Activity_Emotion_Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UserHelper {
    private String name;
    private String email;
    private String gender;
    private String age;
    private String slp;
    private String school_level;
    private String userType;
    private String imageURL;
    private int beginner;
    private int intermediate;
    private int advanced;
    private String level;
    private String note;
    private String week; // Declare week
    private String date; // Declare date
    private List<DiagnosticsRecord> diagnosticEntries = new ArrayList<>(); // Change to List<DiagnosticsRecord>

    public interface UserDataListener {
        void onUserDataLoaded(UserHelper userHelper);
    }

    public UserHelper(UserDataListener listener) {
        loadUserData(listener);
    }

    private void loadUserData(UserDataListener listener) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.child("name").getValue(String.class);
                        email = snapshot.child("email").getValue(String.class);
                        gender = snapshot.child("gender").getValue(String.class);
                        age = snapshot.child("age").getValue(String.class);
                        slp = snapshot.child("slp").getValue(String.class);
                        userType = snapshot.child("userType").getValue(String.class);
                        imageURL = snapshot.child("imageURL").getValue(String.class);
                        school_level = snapshot.child("level").getValue(String.class);
                        beginner = snapshot.child("assessment").child("beginner").getValue(Integer.class) != null ? snapshot.child("assessment").child("beginner").getValue(Integer.class) : 0;
                        intermediate = snapshot.child("assessment").child("intermediate").getValue(Integer.class) != null ? snapshot.child("assessment").child("intermediate").getValue(Integer.class) : 0;
                        advanced = snapshot.child("assessment").child("advanced").getValue(Integer.class) != null ? snapshot.child("assessment").child("advanced").getValue(Integer.class) : 0;
                        level = snapshot.child("assessment").child("level").getValue(String.class) != null ? snapshot.child("assessment").child("level").getValue(String.class) : "Beginner";
                        note = snapshot.child("note").getValue(String.class);
                        week = snapshot.child("week").getValue(String.class);
                        date = snapshot.child("date").getValue(String.class);

                        // Load diagnosis history as a List of DiagnosticsRecord
                        for (DataSnapshot historySnapshot : snapshot.child("diagnosisHistory").getChildren()) {
                            String week = historySnapshot.child("week").getValue(String.class);
                            String date = historySnapshot.child("date").getValue(String.class);
                            String notes = historySnapshot.child("notes").getValue(String.class);
                            diagnosticEntries.add(new DiagnosticsRecord(week, date, notes)); // Create DiagnosticsRecord objects
                        }


                        // Notify listener that user data has been loaded
                        listener.onUserDataLoaded(UserHelper.this);
                    } else {
                        // Notify listener with null user data if the user does not exist
                        listener.onUserDataLoaded(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle error
                    Log.e(TAG, "Failed to load user data: " + error.getMessage());
                }
            });
        } else {
            // Notify listener with null user data if no user is logged in
            listener.onUserDataLoaded(null);
        }
    }

    public void updateUserRewards() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid()).child("rewards");
            userRef.setValue(ServerValue.increment(1));
        }
    }

    public void updateUserFields(Context context, HashMap<String, Object> updates) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid());

            // Update the user fields
            userRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Show success message
                    if(context == null) return;
                    new StylishAlertDialog(context, StylishAlertDialog.SUCCESS)
                            .setTitleText("Success")
                            .setContentText("Profile updated successfully.")
                            .show();
                } else {
                    // Show error message
                    new StylishAlertDialog(context, StylishAlertDialog.ERROR)
                            .setTitleText("Error")
                            .setContentText("Failed to update profile.")
                            .show();
                }
            });
        }
    }

    public void updateChartField(HashMap<String, Object> updates) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("charts").child(user.getUid());

            // Push a new child node with the updates
            userRef.push().setValue(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                        } else {
                            Log.d(TAG, "updateChartField: Failed to update chart.");
                        }
                    });
        }
    }


    // Check if there's a current user logged in
    public boolean isUserLoggedIn() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null;
    }

    public String UserUID() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    // Getter for diagnosis history
    public List<DiagnosticsRecord> getDiagnosticEntries() {
        return diagnosticEntries;  // Returns a list of DiagnosticsRecord
    }

    public String getWeek() {
        return week;
    }

    public String getDate() {
        return date;
    }

    // Getter for other fields
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getSLP() {
        return slp;
    }

    public String getSchoolLevel() {
        return school_level;
    }

    public String getUserType() {
        return userType;
    }

    public String getImageURL() {
        return imageURL;
    }

    public int getBeginner() {
        return (beginner * 100) / 16;
    }

    public int getIntermediate() {
        return (intermediate * 100) / 16;
    }

    public int getAdvanced() {
        return (advanced * 100) / 16;
    }

    public String getLevel() {
        return level;
    }

    @SuppressLint("DefaultLocale")
    public String getNote() {
        // Define the start date for the note (when week 1 starts)
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2024, Calendar.DECEMBER, 20); // Example start date for week 1

        // Get the current date
        Calendar currentCalendar = Calendar.getInstance();

        // Calculate the difference in weeks
        long diffInMillis = currentCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        long diffInWeeks = diffInMillis / (1000 * 60 * 60 * 24 * 7); // Convert milliseconds to weeks

        // Week number starts from 1
        int weekNumber = (int) diffInWeeks + 1;

        // Format the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(currentCalendar.getTime());

        // Construct the output string with desired formatting
        return String.format(
                "<b><span style=\"font-size:24px;\">Week %d</span></b><br>" +  // Apply larger font size to 'Week'
                        "Date: %s<br><br>" +
                        "<b>Note:</b><br>%s",
                weekNumber, formattedDate, note
        );
    }

    public int getPercentage() {
        return ((beginner + intermediate + advanced) * 100) / 48;
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    public String getFormattedNotes() {
        StringBuilder formattedNotes = new StringBuilder();
        for (DiagnosticsRecord record : diagnosticEntries) {
            formattedNotes.append("Week: ").append(record.getWeek())
                    .append(", Date: ").append(record.getDate())
                    .append(", Notes: ").append(record.getNotes())
                    .append("\n"); // Add a newline for better readability
        }
        // Optionally include the last loaded week and date
        formattedNotes.append("Last Week: ").append(week)
                .append(", Last Date: ").append(date)
                .append("\n");
        return formattedNotes.toString();
    }
}
