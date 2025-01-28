package com.capstone.funpath.GameModes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.capstone.funpath.Fragment_SLP.Fragment_Analysis;
import com.capstone.funpath.Fragment_SLP.Fragment_List;
import com.capstone.funpath.Fragment_SLP.Fragment_Profile;
import com.capstone.funpath.Fragment_SLP.Fragment_Voice_Record;
import com.capstone.funpath.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.marsad.stylishdialogs.StylishAlertDialog;

public class Activity_SLP_Lobby extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_slp_lobby);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Check for internet connection and show dialog if not available
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("selectedKidKey"); // Removes the specific key-value pair
        editor.apply(); // Apply changes asynchronously

        bottomNavigationView.setOnItemSelectedListener(item -> {
            String selectedKidKey = sharedPreferences.getString("selectedKidKey", null);
            // Handle navigation item selection here
            if (item.getItemId() == R.id.list) {
                replaceFragment(new Fragment_List());
                return true;
            } else if (item.getItemId() == R.id.profile && selectedKidKey != null) {
                replaceFragment(new Fragment_Profile());
                return true;
            } else if (item.getItemId() == R.id.analysis && selectedKidKey != null) {
                replaceFragment(new Fragment_Analysis());
                return true;
            }
            else if (item.getItemId() == R.id.record && selectedKidKey != null) {
                replaceFragment(new Fragment_Voice_Record());
                return true;
            }else {
                new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                        .setTitleText("Warning")
                        .setContentText("Kid must be selected to access this feature")
                        .setConfirmText("OK")
                        .show();
            }
            return false;
        });
        replaceFragment(new Fragment_List());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new StylishAlertDialog(this, StylishAlertDialog.ERROR)
                .setTitleText("No Internet Connection")
                .setContentText("Please check your internet connection and try again.")
                .setConfirmText("OK")
                .setConfirmButtonBackgroundColor(Color.RED)
                .setConfirmClickListener(dialog -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancellable(false)
                .show();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.framelayout, fragment);
        transaction.commit();
    }
}
