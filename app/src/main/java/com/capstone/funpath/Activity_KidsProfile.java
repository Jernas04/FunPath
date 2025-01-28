package com.capstone.funpath;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.funpath.Adapters.DiagnosticsAdapter;
import com.capstone.funpath.Adapters.DiagnosticsRecord;
import com.capstone.funpath.Fragment_SLP.Fragment_Profile;
import com.capstone.funpath.Helpers.UserHelper;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Activity_KidsProfile extends AppCompatActivity {

    UserHelper userHelper;
    RecyclerView rvDiagnosticsHistory;
    DiagnosticsAdapter diagnosticsAdapter;
    String selectedKidKey;
    List<DiagnosticsRecord> diagnosticEntries = new ArrayList<>();
    DiagnosticViewModel diagnosticViewModel;

    private ShapeableImageView iv_profile;
    private TextView tv_name, tv_percentage, tv_percentage_beginner, tv_percentage_intermediate, tv_percentage_advanced, tv_level, tv_notes, tv_week, tv_date;
    private CircularProgressIndicator circularProgressIndicator;
    private ProgressBar progressBar_overall, progressBar_beginner, progressBar_intermediate, progressBar_advanced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kids_profile);

        initializeUI();
        handleEdgeToEdge();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String selectedKidKey = sharedPreferences.getString("selectedKidKey", null);

        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }

        if (selectedKidKey != null) {
            // Get the note from the TextView
            String entry = tv_notes.getText().toString().trim();

            // Only proceed if the note is not empty
            if (!entry.isEmpty()) {
                // Set the note in the Firebase database
                DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(selectedKidKey);
                kidRef.child("note").setValue(entry); // Save the note

                // Save the diagnostic history with the note
                saveDiagnosticHistory(selectedKidKey, entry); // Ensure you pass the correct key

                // Load kid profile data and diagnostic history
                loadKidProfileData();
                loadDiagnosticHistory();
            } else {
                Log.w("KidsProfile", "Note is empty. Not updating Firebase.");
            }
        }

        // Assuming userHelper initialization happens asynchronously
        userHelper = new UserHelper(helper -> {
            if (helper != null) {
                tv_name.setText(helper.getName());
                tv_notes.setText(Html.fromHtml(helper.getNote(), Html.FROM_HTML_MODE_LEGACY));
                tv_level.setText(helper.getLevel());
                tv_percentage.setText(helper.getPercentage() + "%");
                tv_percentage_beginner.setText(helper.getBeginner() + "%");
                tv_percentage_intermediate.setText(helper.getIntermediate() + "%");
                tv_percentage_advanced.setText(helper.getAdvanced() + "%");
                diagnosticsAdapter.updateData(helper.getDiagnosticEntries());

                circularProgressIndicator.setProgress(helper.getPercentage());
                progressBar_overall.setProgress(helper.getPercentage());
                progressBar_beginner.setProgress(helper.getBeginner());
                progressBar_intermediate.setProgress(helper.getIntermediate());
                progressBar_advanced.setProgress(helper.getAdvanced());

                if (!helper.getImageURL().isEmpty()) {
                    Glide.with(this).load(helper.getImageURL()).into(iv_profile);
                }

            } else {
                Log.e("KidsProfile", "UserHelper callback received null data.");
            }
        });

        // Initialize ViewModel
        diagnosticViewModel = new ViewModelProvider(this).get(DiagnosticViewModel.class);

        // Observe the diagnostic history
        diagnosticViewModel.getDiagnosticHistory().observe(this, new Observer<List<DiagnosticsRecord>>() {
            @Override
            public void onChanged(List<DiagnosticsRecord> entries) {
                if (entries != null) {
                    diagnosticsAdapter.updateData(entries); // Update RecyclerView
                }
            }
        });
    }

    private void initializeUI() {
        rvDiagnosticsHistory = findViewById(R.id.rv_diagnostics_history);
        rvDiagnosticsHistory.setLayoutManager(new LinearLayoutManager(this));
        diagnosticsAdapter = new DiagnosticsAdapter(diagnosticEntries);
        rvDiagnosticsHistory.setAdapter(diagnosticsAdapter);

        // Initialize ViewModel
        diagnosticViewModel = new ViewModelProvider(this).get(DiagnosticViewModel.class);
        tv_week = findViewById(R.id.tv_week);
        tv_date = findViewById(R.id.tv_date);

        iv_profile = findViewById(R.id.iv_profile);
        tv_name = findViewById(R.id.tv_name);
        tv_percentage = findViewById(R.id.tv_percentage);
        tv_percentage_beginner = findViewById(R.id.tv_percentage_beginner);
        tv_percentage_intermediate = findViewById(R.id.tv_percentage_intermediate);
        tv_percentage_advanced = findViewById(R.id.tv_percentage_advanced);
        tv_level = findViewById(R.id.tv_level);
        tv_notes = findViewById(R.id.tv_notes);
        circularProgressIndicator = findViewById(R.id.circularProgressIndicator);
        progressBar_overall = findViewById(R.id.progressBar);
        progressBar_beginner = findViewById(R.id.progressBar2);
        progressBar_intermediate = findViewById(R.id.progressBar3);
        progressBar_advanced = findViewById(R.id.progressBar4);

        ImageButton ib_back = findViewById(R.id.ib_back);
        ImageButton ib_menu = findViewById(R.id.ib_three_dots);

        ib_back.setOnClickListener(v -> finish());
        ib_menu.setOnClickListener(this::showPopupMenu);
    }

    private void handleEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("It seems you are not connected to the internet. Please check your connection and try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void loadKidProfileData() {
        DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(selectedKidKey);
        kidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String imageURL = dataSnapshot.child("imageURL").getValue(String.class);
                    String note = dataSnapshot.child("note").getValue(String.class); // Fetch note
                    String week = dataSnapshot.child("week").getValue(String.class); // Fetch week
                    String date = dataSnapshot.child("date").getValue(String.class); // Fetch date

                    // Update UI with kid's profile data
                    updateUI(imageURL, name);

                    // Set the note if it exists
                    if (note != null && !note.isEmpty()) {
                        tv_notes.setText(note);  // Update the TextView with the notes
                    } else {
                        tv_notes.setText("No notes available."); // Handle case where there are no notes
                    }

                    // Set the week and date if they exist
                    if (week != null && !week.isEmpty()) {
                        tv_week.setText("Week: " + week); // Update the week TextView
                    } else {
                        tv_week.setText("Week: Not available"); // Handle case where week is not available
                    }

                    if (date != null && !date.isEmpty()) {
                        tv_date.setText("Date: " + date); // Update the date TextView
                    } else {
                        tv_date.setText("Date: Not available"); // Handle case where date is not available
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load kid profile data: " + error.getMessage());
            }
        });
        // Load the diagnostic history
        loadDiagnosticHistory();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register the BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("UPDATE_DIAGNOSTIC_HISTORY"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister the BroadcastReceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String week = intent.getStringExtra("week");
            String date = intent.getStringExtra("date");
            String note = intent.getStringExtra("notes");

            // Update the UI with the received data
            if (week != null && date != null && note != null) {
                tv_week.setText("Week: " + week);
                tv_date.setText("Date: " + date);
                tv_notes.setText(note); // Update the notes TextView
            }
        }
    };

    private void loadDiagnosticHistory() {
        DatabaseReference kidRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(selectedKidKey)
                .child("diagnostic_history");

        kidRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                diagnosticEntries.clear(); // Clear existing entries

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String week = snapshot.getKey(); // Get the week as the key
                    String date = snapshot.child("date").getValue(String.class);
                    String notes = snapshot.child("notes").getValue(String.class);

                    // Log the retrieved entry
                    Log.d("LoadDiagnosticHistory", "Retrieved entry: week=" + week + ", date=" + date + ", notes=" + notes);

                    if (date != null && notes != null) {
                        DiagnosticsRecord entry = new DiagnosticsRecord(week, date, notes);
                        diagnosticEntries.add(entry);
                    }
                }

                diagnosticsAdapter.updateData(diagnosticEntries); // Update RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to load diagnostic history: " + databaseError.getMessage());
            }
        });
    }

    // Callback interface to return the week number
    public interface WeekCallback {
        void onWeekCalculated(int weekNumber);
    }

    public List<DiagnosticsRecord> getDiagnosticHistory() {
        return diagnosticEntries; // Assuming diagnosticEntries is a List<DiagnosticEntry>
    }

    private void saveDiagnosticHistory(String kidRef, String note) {
        // Get the current date and week number
        getWeekFromStartDate(kidRef, new WeekCallback() {
            @Override
            public void onWeekCalculated(int weekNumber) {
                String currentWeek = "Week " + weekNumber;
                String currentDate = getCurrentDate();
                DiagnosticsRecord entry = new DiagnosticsRecord(currentWeek, currentDate, note);
                FirebaseDatabase.getInstance().getReference("users").child(kidRef).child("diagnostic_history").child(currentWeek).setValue(entry)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("KidsProfile", "Diagnostic history saved successfully.");
                                loadDiagnosticHistory();  // Refresh the list
                            } else {
                                Log.e("KidsProfile", "Failed to save diagnostic history.");
                            }
                        });
            }
        });
    }

    private void getWeekFromStartDate(String kidRef, WeekCallback callback) {
        // Retrieve the reference start date from Firebase (if it exists)
        DatabaseReference startDateRef = FirebaseDatabase.getInstance().getReference("users").child(kidRef).child("start_date");
        startDateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if start date exists
                if (snapshot.exists()) {
                    String storedStartDate = snapshot.getValue(String.class);

                    // If start date exists, calculate weeks based on that
                    if (storedStartDate != null) {
                        Calendar startCalendar = Calendar.getInstance();
                        startCalendar.setTimeInMillis(Long.parseLong(storedStartDate)); // Use the stored start date

                        // Get current date
                        Calendar currentCalendar = Calendar.getInstance();

                        long startTime = startCalendar.getTimeInMillis();
                        long currentTime = currentCalendar.getTimeInMillis();

                        long diffInMillis = currentTime - startTime;
                        long diffInWeeks = diffInMillis / (7 * 24 * 60 * 60 * 1000); // Convert milliseconds to weeks

                        // Callback with the calculated week number
                        callback.onWeekCalculated((int) (diffInWeeks + 1)); // Week 1 starts from the reference date
                    }
                } else {
                    // If no start date exists, set the current date as the start date
                    String currentStartDate = String.valueOf(System.currentTimeMillis());
                    startDateRef.setValue(currentStartDate); // Store the current date as the start date

                    // Since this is the first diagnostic note, return Week 1
                    callback.onWeekCalculated(1); // Week 1
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }


    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_ib_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_profile) {
                Intent editIntent = new Intent(this, Activity_EditProfile.class);
                editIntent.putExtra("userUID", userHelper.UserUID());
                startActivity(editIntent);
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                logoutUser();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void logoutUser() {
        userHelper.logout();
        finish();
    }

    private void updateUI(String imageURL, String name) {
        if (imageURL != null && !imageURL.isEmpty()) {
            Glide.with(this).load(imageURL).into(iv_profile);
        }
        if (name != null) {
            tv_name.setText(name);
        }
    }
}
