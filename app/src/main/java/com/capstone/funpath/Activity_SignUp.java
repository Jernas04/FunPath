package com.capstone.funpath;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.GameModes.Activity_SLP_Lobby;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Activity_SignUp extends AppCompatActivity {
    Button btn_signup;
    TextView tv_nav_login;
    EditText edt_sign_name, edt_sign_email;
    TextInputEditText edt_sign_password, edt_sign_confirm_password;
    ImageButton ib_back;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        btn_signup = findViewById(R.id.btn_signup);
        tv_nav_login = findViewById(R.id.tv_nav_login);
        edt_sign_name = findViewById(R.id.edt_sign_name);
        edt_sign_email = findViewById(R.id.edt_sign_email);
        edt_sign_password = findViewById(R.id.edt_sign_password);
        edt_sign_confirm_password = findViewById(R.id.edt_sign_confirm_password);
        ib_back = findViewById(R.id.ib_back);

        tv_nav_login.setOnClickListener(v -> {
            zoomInButton(v);
            toLogin();
        });
        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_signup.setOnClickListener(v -> {
            zoomInButton(v);
            String name = edt_sign_name.getText().toString();
            String email = edt_sign_email.getText().toString();
            String password = edt_sign_password.getText().toString();
            String confirmPassword = edt_sign_confirm_password.getText().toString();

            if (isNetworkAvailable()) {
                SignUp(name, email, password, confirmPassword);
            } else {
                showAlertDialog("Network Error", "No internet connection. Please try again later.");
            }
        });

        // Set touch listener to hold down zoom effect
        setZoomEffectOnHold(ib_back);
        setZoomEffectOnHold(btn_signup);
        setZoomEffectOnHold(tv_nav_login);
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlertDialog("Sign Up Failed", "Please fill in all fields");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showAlertDialog("Sign Up Failed", "Passwords do not match");
            return false;
        }
        return true;
    }

    private void SignUp(String name, String email, String password, String confirmPassword) {
        if (!validateInput(name, email, password, confirmPassword)) return;

        firebaseAuth = FirebaseAuth.getInstance();

        // Check if the name already exists in the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // If the name is already taken
                    showAlertDialog("Name Already In Use", "This name is already associated with another account.");
                } else {
                    // Proceed with email sign-up if the name is available
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Activity_SignUp.this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        // Send email verification
                                        sendVerificationEmail(user, name, email);
                                    }
                                } else {
                                    // Handle error during sign-up
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        showAlertDialog("Email Already In Use", "This email is already associated with another account.");
                                    } else {
                                        showAlertDialog("Sign Up Failed", "Invalid email or password.");
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showAlertDialog("Sign Up Failed", "Failed to check name availability. Please try again.");
            }
        });
    }

    private void sendVerificationEmail(FirebaseUser user, String name, String email) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save user data even before email verification
                        saveUserData(user, name, email);
                        showAlertDialog("Email Verification", "A verification email has been sent. Please verify your email before logging in.");
                    } else {
                        showAlertDialog("Email Verification Failed", "Failed to send verification email. Please try again.");
                    }
                });
    }

    private void saveUserData(FirebaseUser user, String name, String email) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("name", name);
        userData.put("userType", "slp");

        userRef.child(user.getUid()).setValue(userData)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        // Prompt user to verify email before proceeding
                        waitForEmailVerification(user);
                    } else {
                        Log.e("Firebase", "Failed to save user data.", t.getException());
                        showAlertDialog("Sign Up Failed", "Failed to save user data. Please try again.");
                    }
                });
    }

    private void waitForEmailVerification(FirebaseUser user) {
        new Thread(() -> {
            while (!user.isEmailVerified()) {
                try {
                    Thread.sleep(3000); // Check every 3 seconds
                    user.reload(); // Reload user to update verification status
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(this::navigateToLobby); // Navigate to the lobby once verified
        }).start();

        showAlertDialog("Verification Pending", "Please verify your email to proceed. The app will automatically redirect once verified.");
    }


    private void showAlertDialog(String title, String message) {
        new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText("OK")
                .setConfirmClickListener(StylishAlertDialog::dismissWithAnimation)
                .show();
    }

    private void navigateToLobby() {
        Intent intent = new Intent(this, Activity_SLP_Lobby.class);
        startActivity(intent);
        finish();
    }

    private void toLogin() {
        Intent intent = new Intent(this, Activity_Login.class);
        startActivity(intent);
        finish();
    }

    // Method to check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
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
