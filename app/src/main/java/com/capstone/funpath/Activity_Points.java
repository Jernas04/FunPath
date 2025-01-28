package com.capstone.funpath;

import static android.content.ContentValues.TAG;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Activity_Points extends AppCompatActivity {

    private double stars;
    private TextView tv_unallocated;
    private ConstraintLayout CLrewards, CLrewards_1, CLrewards_2, CLrewards_3, CLrewards_4;
    private ImageView iv_redeemed_1, iv_redeemed_2, iv_redeemed_3;
    private ProgressBar pb_achiever, pb_explorer, pb_superstar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_points);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_unallocated = findViewById(R.id.textView31);
        CLrewards = findViewById(R.id.constraintLayout24);
        iv_redeemed_1 = findViewById(R.id.iv_redeemed_1);
        iv_redeemed_2 = findViewById(R.id.iv_redeemed_2);
        iv_redeemed_3 = findViewById(R.id.iv_redeemed_3);
        pb_achiever = findViewById(R.id.progressBar2);
        pb_explorer = findViewById(R.id.progressBar3);
        pb_superstar = findViewById(R.id.progressBar4);
        CLrewards_1 = findViewById(R.id.cr_1);
        CLrewards_2 = findViewById(R.id.cr_2);
        CLrewards_3 = findViewById(R.id.cr_3);
        CLrewards_4 = findViewById(R.id.cr_4);

        String userUID = getIntent().getStringExtra("userUID");

        DatabaseReference rewardsRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUID).child("rewards");
        rewardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Double currentStars = snapshot.getValue(Double.class);
                    double starsValue = currentStars != null ? currentStars : 0;
                    updateCLrewards(starsValue);
                    stars = starsValue;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.imageButton4).setOnClickListener(v -> {
            zoomInButton(v);
            CLrewards.setVisibility(View.GONE);
        });
        findViewById(R.id.button).setOnClickListener(v -> {
            zoomInButton(v);
            CLrewards.setVisibility(View.VISIBLE);
        });

        CLrewards_1.setOnClickListener(v -> {
            zoomInButton(v);
            if (!isNetworkAvailable()) {
                showAlertDialog("No Internet Connection", "You are not connected to the internet. Please check your connection.");
                return;
            }

            new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                    .setTitleText("Redeemed!")
                    .setContentText("You have successfully redeemed 100 points!")
                    .setConfirmClickListener(sDialog -> {
                        rewardsRef.setValue(ServerValue.increment(-100));
                        stars = stars - 100;
                        updateCLrewards(stars);
                        sDialog.dismiss();
                    })
                    .show();
        });

        CLrewards_2.setOnClickListener(v -> {
            zoomInButton(v);
            if (!isNetworkAvailable()) {
                showAlertDialog("No Internet Connection", "You are not connected to the internet. Please check your connection.");
                return;
            }

            new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                    .setTitleText("Redeemed!")
                    .setContentText("You have successfully redeemed 250 points!")
                    .setConfirmClickListener(sDialog -> {
                        rewardsRef.setValue(ServerValue.increment(-250));
                        stars = stars - 250;
                        updateCLrewards(stars);
                        sDialog.dismiss();
                    })
                    .show();
        });

        CLrewards_3.setOnClickListener(v -> {
            zoomInButton(v);
            if (!isNetworkAvailable()) {
                showAlertDialog("No Internet Connection", "You are not connected to the internet. Please check your connection.");
                return;
            }

            new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                    .setTitleText("Redeemed!")
                    .setContentText("You have successfully redeemed 500 points!")
                    .setConfirmClickListener(sDialog -> {
                        rewardsRef.setValue(ServerValue.increment(-500));
                        stars = stars - 500;
                        updateCLrewards(stars);
                        sDialog.dismiss();
                    })
                    .show();
        });

        findViewById(R.id.ib_back).setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });

        setZoomEffectOnHold(findViewById(R.id.ib_menu));
        setZoomEffectOnHold(CLrewards);
        setZoomEffectOnHold(CLrewards_1);
        setZoomEffectOnHold(CLrewards_2);
        setZoomEffectOnHold(CLrewards_3);
        setZoomEffectOnHold(findViewById(R.id.imageButton4));
        setZoomEffectOnHold(findViewById(R.id.button));
        setZoomEffectOnHold(CLrewards_2);
        setZoomEffectOnHold(CLrewards_3);

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

    private void updateCLrewards(double stars) {
        Log.d(TAG, "updateCLrewards: " + stars);
        if (stars < 100) {
            CLrewards_4.setVisibility(View.VISIBLE);
        } else {
            CLrewards_4.setVisibility(View.GONE);
        }
        if (stars >= 100) {
            CLrewards_1.setVisibility(View.VISIBLE);
        } else {
            CLrewards_1.setVisibility(View.GONE);
        }

        if (stars >= 250) {
            CLrewards_2.setVisibility(View.VISIBLE);
        } else {
            CLrewards_2.setVisibility(View.GONE);
        }

        if (stars >= 500) {
            CLrewards_3.setVisibility(View.VISIBLE);
        } else {
            CLrewards_3.setVisibility(View.GONE);
        }

        String starInt = String.valueOf((int) stars);
        pb_achiever.setProgress((int) (stars / 100 * 100));
        pb_explorer.setProgress((int) (stars / 250 * 100));
        pb_superstar.setProgress((int) (stars / 500 * 100));
        tv_unallocated.setText(starInt);
    }

    // Check if there is an internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Show an alert dialog when there is no internet connection
    private void showAlertDialog(String title, String message) {
        new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText("OK")
                .setConfirmClickListener(sDialog -> sDialog.dismiss())
                .show();
    }
}
