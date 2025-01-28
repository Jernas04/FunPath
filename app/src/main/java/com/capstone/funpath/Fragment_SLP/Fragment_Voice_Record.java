package com.capstone.funpath.Fragment_SLP;

import static com.capstone.funpath.asr.Whisper.TAG;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.capstone.funpath.Manuals.Activity_Manual_SLP;
import com.capstone.funpath.Adapters.AudioList_Adapter;
import com.capstone.funpath.Models.AudioItem;
import com.capstone.funpath.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Voice_Record extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Fragment_Voice_Record() {
        // Required empty public constructor
    }

    public static Fragment_Voice_Record newInstance(String param1, String param2) {
        Fragment_Voice_Record fragment = new Fragment_Voice_Record();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    RecyclerView rv_audioList;
    private AudioList_Adapter audioListAdapter;
    private List<AudioItem> audioList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__voice__record, container, false);
        rv_audioList = view.findViewById(R.id.recycler_view_voice_record);
        rv_audioList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Check for internet connection
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return view; // Return early if no internet
        }

        // Initialize audioList and adapter once in onCreateView
        audioList = new ArrayList<>();
        audioListAdapter = new AudioList_Adapter(requireActivity(), audioList);
        rv_audioList.setAdapter(audioListAdapter);

        setupWindowInsets(view);

        Spinner spinner_voice_record = view.findViewById(R.id.spinner_voice_record);
        spinner_voice_record.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String level = parent.getSelectedItem().toString().toLowerCase();
                loadRecordings(level);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        view.findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(requireContext(), Activity_Manual_SLP.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ib_back).setOnClickListener(v -> {
            zoomInButton(v);
            requireActivity().finish();
        });

        setZoomEffectOnHold(view.findViewById(R.id.ib_menu));
        setZoomEffectOnHold(view.findViewById(R.id.ib_back));

        return view;
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

    private void setupWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadRecordings(String level) {
        audioList.clear(); // Clear previous data to avoid duplicates
        audioListAdapter.notifyDataSetChanged(); // Immediately notify the adapter to refresh the view

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String selectedKidKey = sharedPreferences.getString("selectedKidKey", null);

        if (selectedKidKey != null) {
            DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(selectedKidKey)
                    .child("recordings")
                    .child(level);

            kidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot recordingSnapshot : dataSnapshot.getChildren()) {
                            String audioUrl = recordingSnapshot.child("url").getValue(String.class);
                            Long timestamp = recordingSnapshot.child("timestamp").getValue(Long.class);

                            audioList.add(new AudioItem(audioUrl, timestamp));
                        }
                        // Notify the adapter about new data
                        audioListAdapter.notifyDataSetChanged();
                    } else {
                        // If no data exists, ensure the view is empty
                        audioList.clear();
                        audioListAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error loading recordings: " + error.getMessage());
                }
            });
        } else {
            // If selectedKidKey is null, clear the list and notify the adapter
            audioList.clear();
            audioListAdapter.notifyDataSetChanged();
        }
    }

    // In your fragment, override lifecycle methods to stop the media player when needed:

    @Override
    public void onPause() {
        super.onPause();
        if (audioListAdapter != null) {
            audioListAdapter.stopAudio(); // Stop the audio when the fragment is paused
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (audioListAdapter != null) {
            audioListAdapter.stopAudio(); // Ensure audio is stopped if the fragment is stopped
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (audioListAdapter != null) {
            audioListAdapter.stopAudio(); // Clean up resources when the fragment's view is destroyed
        }
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

}
