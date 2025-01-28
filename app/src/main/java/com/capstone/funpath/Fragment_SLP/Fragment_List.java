package com.capstone.funpath.Fragment_SLP;

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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.capstone.funpath.Manuals.Activity_Manual_SLP;
import com.capstone.funpath.Adapters.KidList_Adapter;
import com.capstone.funpath.Models.KidItem;
import com.capstone.funpath.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_List#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_List extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment_List() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_List.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_List newInstance(String param1, String param2) {
        Fragment_List fragment = new Fragment_List();
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

    RecyclerView rv_kidlist;
    private KidList_Adapter kidListAdapter;
    private List<KidItem> kidLists;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__list, container, false);

        // Check for internet connection
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return view; // Return early if no internet
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btn_edit = view.findViewById(R.id.btn_edit);
        ImageButton btn_delete = view.findViewById(R.id.btn_delete);
        ImageButton ib_back = view.findViewById(R.id.ib_back);
        rv_kidlist = view.findViewById(R.id.rv_kidlist); // Make sure you have a RecyclerView in your activity layout
        rv_kidlist.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();
        databaseReference.orderByChild("slp").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                kidLists = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String key = dataSnapshot.getKey();
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String avatarResource = dataSnapshot.child("imageURL").getValue(String.class);
                        kidLists.add(new KidItem(key, name, avatarResource));
                    }
                    kidListAdapter = new KidList_Adapter(kidLists);
                    rv_kidlist.setAdapter(kidListAdapter);
                    kidListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error here if needed
            }
        });

        btn_edit.setOnClickListener(v -> {
            zoomInButton(v);
            String selectedKey = kidListAdapter.getSelectedKey();
            if (selectedKey != null) {
                // Store the selected key in SharedPreferences
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("selectedKidKey", selectedKey);
                editor.apply(); // Save changes

                // Navigate to the Kids Profile
                switchToKidsProfile();
            }
        });

        btn_delete.setOnClickListener(v -> {
            zoomInButton(v);
            String selectedKey = kidListAdapter.getSelectedKey();
            if (selectedKey != null) {
                // Create a confirmation dialog
                new AlertDialog.Builder(requireContext()) // Use requireContext() for fragments
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this kid's profile in your list?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Proceed with deletion
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(selectedKey)
                                    .child("slp");
                            userRef.setValue("");

                            int position = kidListAdapter.getPositionKey(selectedKey);
                            if (position >= 0 && position < kidLists.size()) {
                                kidLists.remove(position);
                                kidListAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Dismiss the dialog if "No" is clicked
                            dialog.dismiss();
                        })
                        .show();
            } else {
                Toast.makeText(requireContext(), "No profile selected for deletion.", Toast.LENGTH_SHORT).show(); // Use requireContext() for Toast
            }
        });


        view.findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(requireContext(), Activity_Manual_SLP.class);
            startActivity(intent);
        });

        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            requireActivity().finish();
        });

        setZoomEffectOnHold(view.findViewById(R.id.ib_menu));
        setZoomEffectOnHold(btn_delete);
        setZoomEffectOnHold(btn_edit);
        setZoomEffectOnHold(ib_back);

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

    private void switchToKidsProfile() {
        // Assuming you are using a BottomNavigationView
        // Replace "R.id.navigation_kids_profile" with your actual menu item ID for Kids Profile
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profile); // Change this to your actual ID

        // Optionally, you can also handle the navigation programmatically:
        Fragment selectedFragment = new Fragment_Profile(); // Replace with your actual Kids Profile Fragment
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout, selectedFragment); // Replace with your actual container ID
        transaction.addToBackStack(null); // Optional: Add to back stack
        transaction.commit();
    }

}