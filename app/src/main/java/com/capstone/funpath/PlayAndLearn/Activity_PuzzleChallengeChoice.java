package com.capstone.funpath.PlayAndLearn;

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

import com.capstone.funpath.R;

public class Activity_PuzzleChallengeChoice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_puzzle_challenge_choice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_wordlvl = findViewById(R.id.btn_wordlvl);
        Button btn_sentencelvl = findViewById(R.id.btn_sentencelvl);
        ImageButton ib_back = findViewById(R.id.ib_back);

        btn_wordlvl.setOnClickListener(v -> {
            zoomInButton(v);
            startGame("word");
        });

        btn_sentencelvl.setOnClickListener(v -> {
            zoomInButton(v);
            startGame("sentence");
        });

        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });

        // Set touch listener to hold down zoom effect
        setZoomEffectOnHold(ib_back);
        setZoomEffectOnHold(btn_wordlvl);
        setZoomEffectOnHold(btn_sentencelvl);
    }

    private void startGame(String level) {
        Intent intent = new Intent();
        switch (level){
            case "word":
                intent = new Intent(Activity_PuzzleChallengeChoice.this, Activity_WordLevel.class);
                break;
            case "sentence":
                intent = new Intent(Activity_PuzzleChallengeChoice.this, Activity_SentenceLevel.class);
                break;
        }
        intent.putExtra("level", level);
        startActivity(intent);
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