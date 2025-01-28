package com.capstone.funpath.Exercises;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.GridView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Adapters.EmotionCheckIn_Adapter;
import com.capstone.funpath.Models.EmotionItem;
import com.capstone.funpath.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Activity_Emotion_Check_In extends AppCompatActivity {

    private EmotionItem[] section1 = {
            new EmotionItem(R.drawable.vector_emoji_sad, "Sad"),
            new EmotionItem(R.drawable.vector_emoji_happy, "Happy"),
            new EmotionItem(R.drawable.vector_emoji_angry, "Angry"),
            new EmotionItem(R.drawable.vector_emoji_sacred, "Scared"),
            new EmotionItem(R.drawable.vector_emoji_silly, "Silly"),
            new EmotionItem(R.drawable.vector_emoji_surprised, "Surprised")
    };

    private EmotionItem[] section2 = {
            new EmotionItem(R.drawable.vector_emotion_tired, "Tired"),
            new EmotionItem(R.drawable.vector_emotion_worried, "Worried"),
            new EmotionItem(R.drawable.vector_emotion_shy, "Shy"),
            new EmotionItem(R.drawable.vector_emotion_happy, "Happy"),
            new EmotionItem(R.drawable.vector_emotion_sick, "Sick"),
            new EmotionItem(R.drawable.vector_emotion_surprised, "Surprised"),
            new EmotionItem(R.drawable.vector_emotion_excited, "Excited"),
            new EmotionItem(R.drawable.vector_emotion_mad, "Mad")
    };

    private EmotionItem[] section3 = {
            new EmotionItem(R.drawable.vector_emotion_very_easy, "Very easy!"),
            new EmotionItem(R.drawable.vector_emotion_i_did_it_hard_sometimes, "I find it hard sometimes"),
            new EmotionItem(R.drawable.vector_emotion_very_hard, "Very Hard"),
    };

    TextToSpeech TTS;
    private List<EmotionItem> selectedEmotions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emotion_check_in);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvTitle = findViewById(R.id.tv_title);

        TTS = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                TTS.setLanguage(Locale.US);
                TTS.setSpeechRate(0.7f);
                speak(tvTitle.getText().toString());
                TTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}

                    @Override
                    public void onDone(String utteranceId) {}

                    @Override
                    public void onError(String utteranceId) {}
                });
            }
        });

        GridView gridView = findViewById(R.id.grid_view);
        List<EmotionItem> items = new ArrayList<>(Arrays.asList(section1));

        EmotionCheckIn_Adapter adapter = new EmotionCheckIn_Adapter(this, items);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            EmotionItem selectedEmotion = items.get(position);

            // Add selected emotion
            selectedEmotions.add(selectedEmotion);

            // Animate the selected item with delay for the next step
            animateView(view, () -> {
                // Speak the emotion text
                speak(selectedEmotion.getText());

                // Transition logic
                if (items.equals(Arrays.asList(section1))) {
                    tvTitle.setText("How do you feel when you speak?");
                    tvTitle.setTextSize(24);
                    speak(tvTitle.getText().toString());
                    items.clear();
                    items.addAll(Arrays.asList(section2));
                } else if (items.equals(Arrays.asList(section2))) {
                    tvTitle.setText("How easy is it to speak?");
                    speak(tvTitle.getText().toString());
                    items.clear();
                    items.addAll(Arrays.asList(section3));
                } else if (items.equals(Arrays.asList(section3))) {
                    // Navigate to result activity
                    Intent intent = new Intent(this, Activity_Emotion_Result.class);
                    intent.putParcelableArrayListExtra("selectedEmotions", new ArrayList<>(selectedEmotions));
                    startActivity(intent);
                    finish();
                }

                // Refresh adapter
                adapter.notifyDataSetChanged();
            });
        });

        findViewById(R.id.ib_close).setOnClickListener(v -> finish());
    }

    private void speak(String text) {
        TTS.speak(text, TextToSpeech.QUEUE_ADD, null, "1");
    }

    private void animateView(android.view.View view, Runnable onAnimationEnd) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.2f, // Scale X
                1.0f, 1.2f, // Scale Y
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // Pivot X
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f  // Pivot Y
        );
        scaleAnimation.setDuration(300); // Animation duration
        scaleAnimation.setRepeatCount(1); // Repeat once
        scaleAnimation.setRepeatMode(ScaleAnimation.REVERSE); // Reverse to original size

        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        view.startAnimation(scaleAnimation); // Start animation
    }
}
