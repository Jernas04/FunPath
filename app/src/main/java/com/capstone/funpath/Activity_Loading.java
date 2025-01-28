package com.capstone.funpath;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.GameModes.Activity_Assessment;
import com.capstone.funpath.GameModes.Activity_Exercises;
import com.capstone.funpath.GameModes.Activity_PlayAndLearn;


public class Activity_Loading extends AppCompatActivity {
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);

        progressBar = findViewById(R.id.progressBar);
        String type = getIntent().getStringExtra("Type");
        loadType(type);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void loadType(String type){
        // Configure the ValueAnimator
        ValueAnimator animator = ValueAnimator.ofInt(1, 100);
        animator.setDuration(3000); // 3 seconds
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBar.setProgress(progress);
            if(progress == 100){
                @SuppressLint("IntentWithNullActionLaunch") Intent intent = new Intent();
                switch (type) {
                    case "Assessment":
                        intent = new Intent(this, Activity_Assessment.class);
                        break;
                    case "Exercises":
                        intent = new Intent(this, Activity_Exercises.class);
                        break;
                    case "Play & Learn":
                        intent = new Intent(this, Activity_PlayAndLearn.class);
                        break;
                    case "SLP's Lobby":
                        String userType = getIntent().getStringExtra("userType");
                        intent = new Intent(this, Activity_LandingPage.class);
                        intent.putExtra("userType", userType);
                        break;
                }

                startActivity(intent);
                finish();
            }
        });
        animator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}