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

import com.capstone.funpath.Manuals.Activity_Manual_Exercises;
import com.capstone.funpath.Manuals.Activity_Manual_PlayAndLearn;
import com.capstone.funpath.PlayAndLearn.Activity_PictureCardsChoice;
import com.capstone.funpath.PlayAndLearn.Activity_PuzzleChallengeChoice;
import com.capstone.funpath.PlayAndLearn.Activity_WordRace;
import com.capstone.funpath.R;

public class Activity_PlayAndLearn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play_and_learn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_picturecards = findViewById(R.id.btn_picturecards);
        Button btn_wordrace = findViewById(R.id.btn_wordrace);
        Button btn_puzzle = findViewById(R.id.btn_puzzle);

        btn_picturecards.setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(this, Activity_PictureCardsChoice.class);
            startActivity(intent);
        });

        btn_wordrace.setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(this, Activity_WordRace.class);
            startActivity(intent);
        });

        btn_puzzle.setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(this, Activity_PuzzleChallengeChoice.class);
            startActivity(intent);
        });

        ImageButton ib_back = findViewById(R.id.ib_back);
        ImageButton ib_manual = findViewById(R.id.ib_manual);

        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            getOnBackPressedDispatcher().onBackPressed();
        });
        ib_manual.setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(this, Activity_Manual_PlayAndLearn.class);
            startActivity(intent);
        });

        setZoomEffectOnHold(btn_picturecards);
        setZoomEffectOnHold(btn_wordrace);
        setZoomEffectOnHold(btn_puzzle);
        setZoomEffectOnHold(ib_back);
        setZoomEffectOnHold(ib_manual);
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
}