package com.capstone.funpath.Exercises;

import android.animation.ObjectAnimator;
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

import com.capstone.funpath.Helpers.TTSAnimatedTextView;
import com.capstone.funpath.R;

public class Activity_Story_Building extends AppCompatActivity {

    private int currentStoryIndex = 0;

    // Arrays for story titles and content
    private String[] storyTitles = {
            "The Sheep & the Pig",
            "Ruby and the Rainbow",
            "The Brave Little Squirrel"
    };

    private String[] stories = {
            "One day a shepherd discovered a fat Pig in the meadow where his Sheep were pastured. He very quickly captured the porker, which squealed at the top of its voice the moment the Shepherd laid his hands on it. ",
            "Once upon a time, Ruby the rabbit wanted to touch a rainbow. She jumped and jumped but couldn’t reach it. Her friend Timmy the turtle came by and suggested they climb a hill to see it better. They watched the rainbow.",
            "Sam the squirrel was afraid of heights, so he stayed on the ground. One day, a storm blew his favorite acorn onto a high branch. With encouragement from his friend Lily, Sam bravely climbed the tree and got the acorn. "
    };

    TTSAnimatedTextView story_text;
    TextView story_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story_building);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        story_text = findViewById(R.id.story_text);
        story_title = findViewById(R.id.story_title);
        ImageButton nextButton = findViewById(R.id.nextButton);
        ImageButton prevButton = findViewById(R.id.prevButton);
        ImageButton ib_close = findViewById(R.id.ib_close);
        ImageButton micButton = findViewById(R.id.micButton);

        story_title.setText(storyTitles[currentStoryIndex]);
        story_text.setText(stories[currentStoryIndex]);
        story_text.setInitialText(stories[currentStoryIndex]);

        nextButton.setOnClickListener(v -> {
            zoomInButton(v);
            if (currentStoryIndex < stories.length - 1) {
                story_text.stop();
                currentStoryIndex++;
                displayCurrentStory();
            }else if(currentStoryIndex == stories.length - 1){
                finish();
            }
        });

        prevButton.setOnClickListener(v -> {
            zoomInButton(v);
            if (currentStoryIndex > 0) {
                story_text.stop();
                currentStoryIndex--;
                displayCurrentStory();
            }
        });

        micButton.setOnClickListener(v -> {
            zoomInButton(v);
            story_text.speakAndAnimate(stories[currentStoryIndex]);
        });

        ib_close.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });

        setZoomEffectOnHold(nextButton);
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

    private void displayCurrentStory() {
        // Set the title and the story content
        story_title.setText(storyTitles[currentStoryIndex]);
        story_text.setText(stories[currentStoryIndex]);
        story_text.speakAndAnimate(stories[currentStoryIndex]);
    }
}