package com.capstone.funpath.GameModes;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Activity_Homepage;
import com.capstone.funpath.Exercises.Activity_Emotion_Check_In;
import com.capstone.funpath.Exercises.Activity_Pacing_And_Phrasing;
import com.capstone.funpath.Exercises.Activity_Story_Building;
import com.capstone.funpath.Manuals.Activity_Manual_Exercises;
import com.capstone.funpath.R;

public class Activity_Exercises extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exercises);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton ib_back = findViewById(R.id.ib_back);
        ImageButton ib_manual = findViewById(R.id.ib_manual);
        Button btn_emotioncheckin = findViewById(R.id.btn_emotioncheckin);
        Button btn_pacing = findViewById(R.id.btn_pacing);
        Button btn_storybuilding = findViewById(R.id.btn_storybuilding);

        btn_emotioncheckin.setOnClickListener(v -> {
            zoomInButton(v);
            LoadIntent("emotioncheckin");
        });

        btn_pacing.setOnClickListener(v -> {
            zoomInButton(v);
            LoadIntent("pacing");
        });

        btn_storybuilding.setOnClickListener(v -> {
            zoomInButton(v);
            LoadIntent("storybuilding");
        });

        ib_manual.setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(this, Activity_Manual_Exercises.class);
            startActivity(intent);
        });

        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });

        // Set touch listener to hold down zoom effect
        setZoomEffectOnHold(ib_back);
        setZoomEffectOnHold(ib_manual);
        setZoomEffectOnHold(btn_emotioncheckin);
        setZoomEffectOnHold(btn_pacing);
        setZoomEffectOnHold(btn_storybuilding);
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

    private void LoadIntent(String level) {
        Intent intent = new Intent();
        switch (level){
            case "emotioncheckin":
                intent = new Intent(Activity_Exercises.this, Activity_Emotion_Check_In.class);
                break;
            case "pacing":
                intent = new Intent(Activity_Exercises.this, Activity_Pacing_And_Phrasing.class);
                break;
            case "storybuilding":
                intent = new Intent(Activity_Exercises.this, Activity_Story_Building.class);
                break;
        }
        intent.putExtra("level", level);
        startActivity(intent);
    }
}