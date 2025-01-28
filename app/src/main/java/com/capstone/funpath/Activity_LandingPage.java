package com.capstone.funpath;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.capstone.funpath.GameModes.Activity_SLP_Lobby;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_LandingPage extends AppCompatActivity {
    Button btn_nav_login, btn_nav_signup;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_page);
        btn_nav_login = findViewById(R.id.btn_nav_login);
        btn_nav_signup = findViewById(R.id.btn_nav_signup);

        String userType = getIntent().getStringExtra("userType");
        assert userType != null;
        if(userType.equals("Kid")) {
            btn_nav_signup.setText("Create Profile");
        }

        btn_nav_login.setOnClickListener(view -> {
            zoomInButton(view);
            Intent intent = new Intent(Activity_LandingPage.this, Activity_Login.class);
            intent.putExtra("userType", userType);
            startActivity(intent);
        });

        btn_nav_signup.setOnClickListener(view -> {
            zoomInButton(view);
            Intent intent;
            if(userType.equals("Kid")){
                intent = new Intent(Activity_LandingPage.this, Activity_CreateProfile.class);

            }else {
                intent = new Intent(Activity_LandingPage.this, Activity_SignUp.class);
            }
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set touch listener to hold down zoom effect
        setZoomEffectOnHold(btn_nav_login);
        setZoomEffectOnHold(btn_nav_signup);
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
}