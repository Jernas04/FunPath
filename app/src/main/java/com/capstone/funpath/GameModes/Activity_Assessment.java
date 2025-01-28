package com.capstone.funpath.GameModes;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.capstone.funpath.Assessment.Activity_BeginnerToAdvanced;
import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.Manuals.Activity_Manual_Assessment;
import com.capstone.funpath.R;
import com.marsad.stylishdialogs.StylishAlertDialog;

public class Activity_Assessment extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assessment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_beginner = findViewById(R.id.btn_beginner);
        Button btn_intermediate = findViewById(R.id.btn_intermediate);
        Button btn_advanced = findViewById(R.id.btn_advanced);
        ImageButton ib_back = findViewById(R.id.ib_back);
        ImageButton ib_manual = findViewById(R.id.ib_manual);
        ImageView iv_lock_beg = findViewById(R.id.iv_lock_beg);
        ImageView iv_lock_int = findViewById(R.id.iv_lock_int);
        ImageView iv_lock_adv = findViewById(R.id.iv_lock_adv);

        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }

        new UserHelper(helper -> {
            if (helper != null && helper.getUserType().equals("kid")) {
                switch (helper.getLevel().toLowerCase()) {
                    case "beginner":
                        btn_beginner.setEnabled(true);
                        btn_intermediate.setEnabled(false);
                        btn_advanced.setEnabled(false);
                        iv_lock_int.setVisibility(View.VISIBLE);
                        iv_lock_adv.setVisibility(View.VISIBLE);
                        break;
                    case "intermediate":
                        btn_beginner.setEnabled(true);
                        btn_intermediate.setEnabled(true);
                        btn_advanced.setEnabled(false);
                        iv_lock_adv.setVisibility(View.VISIBLE);
                        break;
                    case "advanced":
                        btn_beginner.setEnabled(true);
                        btn_intermediate.setEnabled(true);
                        btn_advanced.setEnabled(true);
                        iv_lock_int.setVisibility(View.GONE);
                        iv_lock_adv.setVisibility(View.GONE);
                        break;
                }
            } else {
                btn_beginner.setEnabled(false);
                btn_intermediate.setEnabled(false);
                btn_advanced.setEnabled(false);
                iv_lock_beg.setVisibility(View.VISIBLE);
                iv_lock_int.setVisibility(View.VISIBLE);
                iv_lock_adv.setVisibility(View.VISIBLE);

                Toast.makeText(this, "Please login as kid to start the assessment.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listeners for the buttons
        btn_beginner.setOnClickListener(v -> {
            zoomInButton(v);
            startIntent(btn_beginner.getText().toString());
        });

        btn_intermediate.setOnClickListener(v -> {
            zoomInButton(v);
            startIntent(btn_intermediate.getText().toString());
        });

        btn_advanced.setOnClickListener(v -> {
            zoomInButton(v);
            startIntent(btn_advanced.getText().toString());
        });

        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });

        ib_manual.setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(this, Activity_Manual_Assessment.class);
            startActivity(intent);
        });

        // Set touch listener to hold down zoom effect for buttons
        setZoomEffectOnHold(btn_beginner);
        setZoomEffectOnHold(btn_intermediate);
        setZoomEffectOnHold(btn_advanced);

        // Set touch listener to hold down zoom effect for image buttons
        setZoomEffectOnHold(ib_back);
        setZoomEffectOnHold(ib_manual);
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

    // Zoom-in animation for the view (Button/ImageButton)
    private void zoomInButton(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f); // Scale in X direction
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f); // Scale in Y direction
        scaleX.setDuration(100); // Duration of animation
        scaleY.setDuration(100); // Duration of animation
        scaleX.start();
        scaleY.start();
    }

    // Set zoom effect on touch (hold down to zoom in)
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

    private void startIntent(String name) {
        Intent intent = new Intent(this, Activity_BeginnerToAdvanced.class);
        intent.putExtra("Level", name);
        startActivity(intent);
    }
}
