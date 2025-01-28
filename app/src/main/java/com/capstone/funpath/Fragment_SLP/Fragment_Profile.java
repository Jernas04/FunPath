package com.capstone.funpath.Fragment_SLP;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.capstone.funpath.Activity_KidsProfile;
import com.capstone.funpath.Activity_KidsProfileEmotion;
import com.capstone.funpath.Adapters.DiagnosticsAdapter;
import com.capstone.funpath.Adapters.DiagnosticsRecord;
import com.capstone.funpath.DiagnosticHistoryManager;
import com.capstone.funpath.DiagnosticViewModel;
import com.capstone.funpath.Manuals.Activity_Manual_SLP;
import com.capstone.funpath.R;
import com.google.android.material.imageview.ShapeableImageView;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Profile extends Fragment {

    // Initialize diagnosticEntries as an empty list
    List<DiagnosticsRecord> diagnosticEntries = new ArrayList<>();
    DiagnosticViewModel diagnosticViewModel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment_Profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Profile newInstance(String param1, String param2) {
        Fragment_Profile fragment = new Fragment_Profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        diagnosticViewModel = new ViewModelProvider(requireActivity()).get(DiagnosticViewModel.class);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    ShapeableImageView iv_profile_image;
    TextView tv_profile_name, tv_age, tv_gender, tv_school_level;
    EditText edt_notes;
    ImageButton penButton, checkButton;
    Button btn_emotions;
    RecyclerView rvDiagnosticsHistory;
    DiagnosticsAdapter diagnosticsAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__profile, container, false);

        // Check for internet connection
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return view; // Return early if no internet
        }

        // Apply window insets to the view
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the RecyclerView here after inflating the layout
        rvDiagnosticsHistory = view.findViewById(R.id.rv_diagnostics_history);
        rvDiagnosticsHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        diagnosticsAdapter = new DiagnosticsAdapter(diagnosticEntries);
        rvDiagnosticsHistory.setAdapter(diagnosticsAdapter);

        iv_profile_image = view.findViewById(R.id.iv_profile_image);
        tv_profile_name = view.findViewById(R.id.tv_profile_name);
        tv_age = view.findViewById(R.id.tv_age);
        tv_gender = view.findViewById(R.id.tv_gender);
        tv_school_level = view.findViewById(R.id.tv_school_level);
        penButton = view.findViewById(R.id.penButton);
        checkButton = view.findViewById(R.id.checkButton);
        edt_notes = view.findViewById(R.id.edt_notes);
        btn_emotions = view.findViewById(R.id.btn_emotions);
        ImageButton ib_back = view.findViewById(R.id.ib_back);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String selectedKidKey = sharedPreferences.getString("selectedKidKey", null);

        if (selectedKidKey != null) {
            DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(selectedKidKey);
            kidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve data from the snapshot
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String imageURL = dataSnapshot.child("imageURL").getValue(String.class);
                        String age = dataSnapshot.child("age").getValue(String.class);
                        String gender = dataSnapshot.child("gender").getValue(String.class);
                        String schoolLevel = dataSnapshot.child("level").getValue(String.class);
                        String slpName = dataSnapshot.child("slp").getValue(String.class);
                        String note = dataSnapshot.child("note").getValue(String.class);

                        updateUI(imageURL, name, age, gender, schoolLevel, slpName, note);

                        edt_notes.setText(note);

                        // Fetch diagnostic history
                        loadDiagnosticHistory(kidRef);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            checkButton.setOnClickListener(v -> {
                zoomInButton(v);
                edt_notes.setEnabled(false);
                checkButton.setVisibility(View.GONE);
                penButton.setVisibility(View.VISIBLE);
                String note = edt_notes.getText().toString();

                // Get the current week and date
                String currentWeek = "Current Week"; // Replace with actual week calculation if needed
                String currentDate = getCurrentDate(); // Get the current date

                // Save the note, week, and date to Firebase
                kidRef.child("note").setValue(note);
                kidRef.child("week").setValue(currentWeek); // Save the week
                kidRef.child("date").setValue(currentDate); // Save the date

                // Send broadcast with week, date, and notes
                Intent intent = new Intent("UPDATE_DIAGNOSTIC_HISTORY");
                intent.putExtra("week", currentWeek);
                intent.putExtra("date", currentDate);
                intent.putExtra("notes", note);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                // Save the diagnostic note to the history
                saveDiagnosticNoteToHistory(kidRef, note);
                diagnosticViewModel.addDiagnosticRecord(new DiagnosticsRecord(currentWeek, currentDate, note));
            });

            btn_emotions.setOnClickListener(v -> {
                zoomInButton(v);
                Intent intent = new Intent(getContext(), Activity_KidsProfileEmotion.class);
                intent.putExtra("selectedKidKey", selectedKidKey);
                startActivity(intent);
            });
        }

        penButton.setOnClickListener(v -> {
            zoomInButton(v);
            edt_notes.setEnabled(true);
            edt_notes.requestFocus();
            checkButton.setVisibility(View.VISIBLE);
            penButton.setVisibility(View.GONE);
        });

        view.findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(requireContext(), Activity_Manual_SLP.class);
            startActivity(intent);
        });

        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            getActivity().finish();
        });

        setZoomEffectOnHold(view.findViewById(R.id.ib_menu));
        setZoomEffectOnHold(btn_emotions);
        setZoomEffectOnHold(ib_back);
        setZoomEffectOnHold(penButton);
        setZoomEffectOnHold(checkButton);

        // Access the Activity
        Activity activity = getActivity();
        if (activity instanceof Activity_KidsProfile) {
            Activity_KidsProfile kidsProfileActivity = (Activity_KidsProfile) activity;
            // Retrieve the diagnostic history from the activity
            List<DiagnosticsRecord> retrievedEntries = kidsProfileActivity.getDiagnosticHistory();

            // Clear the existing entries and add the retrieved entries
            diagnosticEntries.clear(); // Clear the current list
            diagnosticEntries.addAll(retrievedEntries); // Add the retrieved entries

            // Update the RecyclerView adapter with the new data
            diagnosticsAdapter.updateData(diagnosticEntries);
        } else {
            Log.e("Fragment_Profile", "The activity is not an instance of Activity_KidsProfile");
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, new IntentFilter("UPDATE_DIAGNOSTIC_HISTORY"));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // You need to pass the kidRef to loadDiagnosticHistory
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String selectedKidKey = sharedPreferences.getString("selectedKidKey", null);
            if (selectedKidKey != null) {
                DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(selectedKidKey);
                loadDiagnosticHistory(kidRef); // Pass the kidRef here
            }
        }
    };

    private void loadDiagnosticHistory(DatabaseReference kidRef) {
        DatabaseReference diagnosticHistoryRef = kidRef.child("diagnostic_history");

        diagnosticHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DiagnosticsRecord> diagnosticEntries = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String week = snapshot.child("week").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);
                    String notes = snapshot.child("notes").getValue(String.class);

                    DiagnosticsRecord entry = new DiagnosticsRecord(week, date, notes);
                    diagnosticEntries.add(entry);
                }

                // Update the RecyclerView adapter
                diagnosticsAdapter.updateData(diagnosticEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
            }
        });
    }

    // Callback interface to return the week number
    public interface WeekCallback {
        void onWeekCalculated(int weekNumber);
    }

    private void saveDiagnosticNoteToHistory(DatabaseReference kidRef, String note) {
        getWeekFromStartDate(kidRef, new WeekCallback() {
            @Override
            public void onWeekCalculated(int weekNumber) {
                String currentWeek = "Week " + weekNumber;
                String currentDate = getCurrentDate();
                DiagnosticsRecord entry = new DiagnosticsRecord(currentWeek, currentDate, note);
                kidRef.child("diagnostic_history").child(currentWeek).setValue(entry)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Send broadcast to update Activity_KidsProfile
                                Intent intent = new Intent("UPDATE_DIAGNOSTIC_HISTORY");
                                intent.putExtra("week", currentWeek);
                                intent.putExtra("date", currentDate);
                                intent.putExtra("notes", note);
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                            }
                        });
            }
        });
    }

    private void getWeekFromStartDate(DatabaseReference kidRef, WeekCallback callback) {
        // Retrieve the reference start date from Firebase (if it exists)
        DatabaseReference startDateRef = kidRef.child("start_date");
        startDateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
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
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    // Zoom-in animation for the button
    private void zoomInButton(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f); // Scale in X direction
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f); // Scale in Y direction
        scaleX.setDuration(100); // Duration of animation
        scaleY.setDuration(100); // Duration of animation
        scaleX.start();
        scaleY.start();
    }

    private void setZoomEffectOnHold(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    zoomInButton(v); // Apply zoom-in effect when pressed
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    zoomInButton(v); // Reset zoom when released
                    break;
            }
            return false;
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        requireActivity().finish(); // Optionally finish the activity
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void updateUI(String imageURL, String name, String age, String gender, String schoolLevel, String slpName, String note) {
        tv_profile_name.setText(name);
        tv_age.setText(age);
        tv_gender.setText(gender);
        tv_school_level.setText(schoolLevel);
        edt_notes.setText(note);
        if(!imageURL.isEmpty()){
            Glide.with(getContext()).load(imageURL).into(iv_profile_image);
        }
    }

}