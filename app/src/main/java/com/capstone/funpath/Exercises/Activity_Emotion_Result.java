package com.capstone.funpath.Exercises;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.Models.EmotionItem;
import com.capstone.funpath.R;

import java.util.HashMap;
import java.util.List;

public class Activity_Emotion_Result extends AppCompatActivity {
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emotion_result);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find views
        TextView tv_first_result = findViewById(R.id.tv_first_result);
        ImageView iv_first_result = findViewById(R.id.iv_first_result);
        TextView tv_second_result = findViewById(R.id.tv_second_result);
        ImageView iv_second_result = findViewById(R.id.iv_second_result);
        TextView tv_third_result = findViewById(R.id.tv_third_result);
        ImageView iv_third_result = findViewById(R.id.iv_third_result);

        // Get the data from the previous activity
        List<EmotionItem> selectedEmotions = getIntent().getParcelableArrayListExtra("selectedEmotions");

        new UserHelper(helper -> {
            if(helper == null) return;
            helper.updateUserFields(null, new HashMap<String, Object>() {{
                put("emotions", selectedEmotions);
            }});
        });
        // Check and set data for each view
        if (selectedEmotions != null && !selectedEmotions.isEmpty()) {
            tv_first_result.setText(selectedEmotions.get(0).getText());
            iv_first_result.setImageResource(selectedEmotions.get(0).getImageResId());
        }
        if (selectedEmotions.size() >= 2) {
            tv_second_result.setText(selectedEmotions.get(1).getText());
            iv_second_result.setImageResource(selectedEmotions.get(1).getImageResId());
        }
        if (selectedEmotions.size() >= 3) {
            tv_third_result.setText(selectedEmotions.get(2).getText());
            iv_third_result.setImageResource(selectedEmotions.get(2).getImageResId());
        }

        findViewById(R.id.ib_close).setOnClickListener(v -> {
            zoomInButton(v);
            handler.removeCallbacks(navigateRunnable); // Cancel the automatic navigation
            finish(); // Close the current activity
        });

        // Automatically navigate to the next activity after 5 seconds
        navigateRunnable = () -> {
            Intent intent = new Intent(this, Activity_Emotion_Passed.class); // Replace with your next activity
            startActivity(intent);
            finish();
        };
        handler.postDelayed(navigateRunnable, 5000);

        setZoomEffectOnHold(findViewById(R.id.ib_close));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(navigateRunnable); // Ensure the runnable is removed if the activity is destroyed
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
