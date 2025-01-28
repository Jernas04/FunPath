package com.capstone.funpath;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.GameModes.Activity_SLP_Lobby;
import com.capstone.funpath.Helpers.UserHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.marsad.stylishdialogs.StylishAlertDialog;

public class Activity_Login extends AppCompatActivity {
    private Button btn_login;
    private TextView tv_nav_signup, tv_forgot_password;
    private EditText edt_login_email;
    private TextInputEditText edt_login_password;
    private FirebaseAuth firebaseAuth;
    private ImageButton ib_back;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        userType = getIntent().getStringExtra("userType");

        setupLogin();
    }

    private void setupLogin() {
        btn_login = findViewById(R.id.btn_login);
        tv_nav_signup = findViewById(R.id.tv_nav_signup);
        tv_forgot_password = findViewById(R.id.tv_forgot);
        edt_login_email = findViewById(R.id.edt_login_email);
        edt_login_password = findViewById(R.id.edt_login_password);
        ib_back = findViewById(R.id.ib_back);

        btn_login.setOnClickListener(v -> {
            zoomInButton(v);
            if (!isConnected()) {
                showNoInternetDialog();
                return;
            }
            String email = edt_login_email.getText().toString().trim();
            String password = edt_login_password.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                        .setTitleText("Warning")
                        .setContentText("Please fill up all fields")
                        .show();
                return;
            }
            SignIn(email, password);
        });

        if ("Kid".equals(userType)) {
            tv_nav_signup.setText("Create Profile");
        }

        tv_nav_signup.setOnClickListener(v -> {
            zoomInButton(v);
            if ("Kid".equals(userType)) {
                Intent intent = new Intent(Activity_Login.this, Activity_CreateProfile.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(Activity_Login.this, Activity_SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        tv_forgot_password.setOnClickListener(v -> {
            zoomInButton(v);
            if (!isConnected()) {
                showNoInternetDialog();
                return;
            }

            String enteredEmail = edt_login_email.getText().toString().trim();

            if (enteredEmail.isEmpty()) {
                // Show dialog prompting user to enter email
                new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                        .setTitleText("Missing Email")
                        .setContentText("Please enter your email address to receive a reset link.")
                        .show();
                return;
            }

            // Ask for email confirmation via dialog
            new StylishAlertDialog(this, StylishAlertDialog.NORMAL)
                    .setTitleText("Confirm Email")
                    .setContentText("We will send a password reset link to this email: \n" + enteredEmail)
                    .setConfirmButton("Send", dialog -> {
                        dialog.dismiss();
                        sendPasswordResetEmail(enteredEmail);
                    })
                    .setCancelButton("Cancel", StylishAlertDialog::dismiss)
                    .show();
        });


        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            getOnBackPressedDispatcher().onBackPressed();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set touch listener to hold down zoom effect
        setZoomEffectOnHold(ib_back);
        setZoomEffectOnHold(btn_login);
        setZoomEffectOnHold(tv_nav_signup);
        setZoomEffectOnHold(tv_forgot_password);
        setZoomEffectOnHold(tv_nav_signup);
    }

    private void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                                .setTitleText("Success")
                                .setContentText("Password reset email sent successfully!")
                                .show();
                    } else {
                        String errorMessage = "Failed to send reset email.";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        new StylishAlertDialog(this, StylishAlertDialog.ERROR)
                                .setTitleText("Error")
                                .setContentText(errorMessage)
                                .show();
                    }
                });
    }

    private void SignIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        new UserHelper(helper -> {
                            if (helper != null) {
                                if (helper.getUserType().equals("slp") && userType.equals("Kid")) {
                                    new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                                            .setTitleText("Warning")
                                            .setContentText("You can't login here, please login as a Kid")
                                            .show();
                                    helper.logout();
                                } else if (helper.getUserType().equals("kid") && userType.equals("Kid")) {
                                    Intent intent = new Intent(Activity_Login.this, Activity_Homepage.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else if (helper.getUserType().equals("slp") && userType.equals("SLP")) {
                                    Intent intent = new Intent(Activity_Login.this, Activity_SLP_Lobby.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                } else if (helper.getUserType().equals("kid") && userType.equals("SLP")) {
                                    new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                                            .setTitleText("Warning")
                                            .setContentText("You can't login here, please login as a SLP")
                                            .show();
                                    helper.logout();
                                }
                            }
                        });
                    } else {
                        String errorMessage;
                        if (task.getException() != null) {
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            switch (errorCode) {
                                case "ERROR_INVALID_EMAIL":
                                    edt_login_email.setError("Invalid email address");
                                    errorMessage = "The email address is badly formatted.";
                                    break;
                                case "ERROR_WRONG_PASSWORD":
                                    edt_login_password.setError("Incorrect password");
                                    errorMessage = "Incorrect password.";
                                    break;
                                case "ERROR_USER_NOT_FOUND":
                                    errorMessage = "No account found with this email.";
                                    break;
                                default:
                                    errorMessage = "Invalid email or password.";
                                    break;
                            }
                        } else {
                            errorMessage = "Invalid email or password.";
                        }

                        new StylishAlertDialog(this, StylishAlertDialog.ERROR)
                                .setTitleText("Login Error")
                                .setContentText(errorMessage)
                                .show();
                    }
                });
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showNoInternetDialog() {
        new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                .setTitleText("No Internet")
                .setContentText("Please check your internet connection and try again.")
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
