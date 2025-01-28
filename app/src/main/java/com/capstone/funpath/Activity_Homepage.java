package com.capstone.funpath;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.Manuals.Activity_Manual;
import com.capstone.funpath.utils.NetworkUtil;
import com.google.android.material.textfield.TextInputEditText;

public class Activity_Homepage extends AppCompatActivity {
    ImageButton ib_tutorial, ib_profile;
    Button btn_assessment, btn_exercises, btn_games, btn_lobby;
    ConstraintLayout password_overlay;
    ImageButton ib_password_close;
    TextInputEditText et_password;
    Button btn_enter;
    String SLPPASSWORD = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        // Initialize UI elements
        ib_tutorial = findViewById(R.id.ib_tutorial);
        btn_assessment = findViewById(R.id.btn_assessment);
        btn_exercises = findViewById(R.id.btn_exercises);
        btn_games = findViewById(R.id.btn_games);
        btn_lobby = findViewById(R.id.btn_lobby);
        ib_profile = findViewById(R.id.ib_profile);
        password_overlay = findViewById(R.id.password_overlay);
        ib_password_close = findViewById(R.id.imageButton);
        et_password = findViewById(R.id.edt_login_password);
        btn_enter = findViewById(R.id.btn_enter);

        // Handle tutorial button
        ib_tutorial.setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(this, Activity_Manual.class);
            startActivity(intent);
        });

        UserHelper userHelper = new UserHelper(helper -> {
            if (helper != null) {  // Ensure that 'helper' is not null
                String userType = helper.getUserType();  // Retrieve the user type

                if (userType != null && userType.equals("slp")) {  // Check if userType is not null and equals "slp"
                    helper.logout();
                } else {
                    // If image URL is not empty, load the image; otherwise, set a default image
                    if (helper.getImageURL() != null && !helper.getImageURL().isEmpty()) {
                        Glide.with(this).load(helper.getImageURL()).circleCrop().into(ib_profile);
                    } else {
                        ib_profile.setImageResource(R.drawable.vector_avatar_gamer_boy);  // Default image
                    }
                }
            } else {
                // Handle the case when 'helper' is null
                Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        });


        // Handle button clicks with zoom-in animation
        btn_assessment.setOnClickListener(v -> {
            zoomInButton(v); // Apply zoom-in animation
            if (NetworkUtil.isConnected(this)) {
                loadingIntent(btn_assessment.getText().toString());
            } else {
                showNoInternetDialog();
            }
        });

        btn_exercises.setOnClickListener(v -> {
            zoomInButton(v); // Apply zoom-in animation
            loadingIntent(btn_exercises.getText().toString());
        });

        btn_games.setOnClickListener(v -> {
            zoomInButton(v); // Apply zoom-in animation
            loadingIntent(btn_games.getText().toString());
        });

        btn_lobby.setOnClickListener(v -> {
            zoomInButton(v); // Apply zoom-in animation
            password_overlay.setVisibility(View.VISIBLE);
        });

        ib_password_close.setOnClickListener(v -> {
            zoomInButton(v);
            password_overlay.setVisibility(View.GONE);
        });

        btn_enter.setOnClickListener(v -> {
            zoomInButton(v);
            if (et_password.getText().toString().equals(SLPPASSWORD)) {
                password_overlay.setVisibility(View.GONE);
                et_password.setText("");
                Intent intent = new Intent(this, Activity_Loading.class);
                intent.putExtra("Type", btn_lobby.getText().toString());
                intent.putExtra("userType", "SLP");
                startActivity(intent);
            } else {
                et_password.setError("Incorrect Password");
            }
        });

        ib_profile.setOnClickListener(v -> {
            zoomInButton(v);

            // Check network connection first
            if (NetworkUtil.isConnected(this)) {

                // Ensure userHelper is not null before checking login status and user type
                if (userHelper != null) {
                    if (userHelper.isUserLoggedIn()) {
                        String userType = userHelper.getUserType();  // Get user type
                        if (userType != null && userType.equals("kid")) {
                            // If the user type is "kid", navigate to the Kids Profile
                            Intent intent = new Intent(this, Activity_KidsProfile.class);
                            startActivity(intent);
                        } else {
                            // If the user is not "kid", log them out and navigate to LandingPage
                            userHelper.logout();
                            Intent intent = new Intent(this, Activity_LandingPage.class);
                            intent.putExtra("userType", "Kid");
                            startActivity(intent);
                        }
                    } else {
                        // Handle case when user is not logged in
                        // Navigate to Login or Profile Creation Activity
                        Intent intent = new Intent(this, Activity_LandingPage.class);  // You can replace this with a profile creation activity if needed
                        intent.putExtra("userType", "Kid");  // Indicate that the user type is "Kid"
                        startActivity(intent);
                    }
                } else {
                    // Handle case when userHelper is null
                    Log.e("Activity_Homepage", "UserHelper is null");
                    Toast.makeText(this, "User data is unavailable", Toast.LENGTH_SHORT).show();
                }

            } else {
                // Show dialog when there's no internet connection
                showNoInternetDialog();
            }
        });



        // Handle system UI insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set touch listener to hold down zoom effect
        setZoomEffectOnHold(btn_assessment);
        setZoomEffectOnHold(btn_exercises);
        setZoomEffectOnHold(btn_games);
        setZoomEffectOnHold(btn_lobby);
        setZoomEffectOnHold(ib_tutorial);
        setZoomEffectOnHold(ib_password_close);
        setZoomEffectOnHold(btn_enter);
        setZoomEffectOnHold(ib_profile);
    }

    // Helper method to launch the loading activity
    private void loadingIntent(String type) {
        Intent intent = new Intent(this, Activity_Loading.class);
        intent.putExtra("Type", type);
        startActivity(intent);
    }

    // Helper method to show the "No Internet Connection" dialog
    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Check the user status whenever the activity is resumed
        new UserHelper(helper -> {
            if (helper != null) {  // Ensure that 'helper' is not null
                String userType = helper.getUserType();  // Retrieve the user type

                if (userType != null && userType.equals("slp")) {  // Check if userType is not null and equals "slp"
                    helper.logout();
                } else {
                    // If image URL is not empty, load the image; otherwise, set a default image
                    if (helper.getImageURL() != null && !helper.getImageURL().isEmpty()) {
                        Glide.with(this).load(helper.getImageURL()).circleCrop().into(ib_profile);
                    } else {
                        ib_profile.setImageResource(R.drawable.vector_avatar_gamer_boy);  // Default image
                    }
                }
            } else {
                // Handle the case when 'helper' is null
                Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
}