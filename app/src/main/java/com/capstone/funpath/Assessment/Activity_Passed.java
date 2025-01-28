package com.capstone.funpath.Assessment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.PlayAndLearn.Activity_PictureCards;
import com.capstone.funpath.PlayAndLearn.Activity_SentenceLevel;
import com.capstone.funpath.R;

public class Activity_Passed extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_passed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tv_difficulty = findViewById(R.id.tv_difficulty);
        TextView tv_message = findViewById(R.id.tv_message);
        TextView tv_itemname = findViewById(R.id.tv_itemname);
        ImageButton ib_close = findViewById(R.id.ib_close);
        ImageButton repeatButton = findViewById(R.id.repeatButton);
        ImageButton prevButton = findViewById(R.id.prevButton);

        String itemText = getIntent().getStringExtra("Level");
        String activity = getIntent().getStringExtra("Activity");
        assert activity != null;
        switch (activity) {
            case "Activity_BeginnerToAdvanced": {
                ImageButton nextButton = findViewById(R.id.nextButton);
                String nextLevel = getIntent().getStringExtra("nextLevel");
                assert itemText != null;
                if (!itemText.equals("Advanced")) {
                    tv_itemname.setVisibility(TextView.GONE);
                    nextButton.setOnClickListener(v -> {
                        zoomInButton(v);
                        Intent intent = new Intent(this, Activity_BeginnerToAdvanced.class);
                        intent.putExtra("Level", nextLevel);
                        startActivity(intent);
                        finish();
                    });
                }else {
                    tv_itemname.setVisibility(TextView.GONE);
                    nextButton.setOnClickListener(v -> {
                        finish();
                    });
                }
                repeatButton.setOnClickListener(v -> {
                    zoomInButton(v);
                    Intent intent = new Intent(this, Activity_BeginnerToAdvanced.class);
                    intent.putExtra("Level", itemText);
                    startActivity(intent);
                    finish();
                });
                break;
            }
        }
        tv_difficulty.setText(itemText);
        tv_message.setText("You passed " +itemText + " level. Keep up the good work!");

        ib_close.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });
        prevButton.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });

        setZoomEffectOnHold(repeatButton);
        setZoomEffectOnHold(prevButton);
        setZoomEffectOnHold(ib_close);
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