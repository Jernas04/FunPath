package com.capstone.funpath.PlayAndLearn;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Activity_Points;
import com.capstone.funpath.Helpers.SpeechRecognitionListener;
import com.capstone.funpath.Helpers.TTSAnimatedTextView;
import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.Permissions.MicPermission;
import com.capstone.funpath.R;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Activity_WordRace extends AppCompatActivity {

    private final List<String> level1Words = new ArrayList<>(List.of(
            "apple", "chair", "cloud", "guitar", "pencil"
    ));
    private final List<String> level2Words = new ArrayList<>(List.of(
            "rocket", "window", "mountain", "camera", "turtle"
    ));
    private final List<String> level3Words = new ArrayList<>(List.of(
            "piano", "jacket", "bicycle", "strawberry", "island"
    ));
    private final List<String> level4Words = new ArrayList<>(List.of(
            "candle", "keyboard", "flower", "puzzle", "notebook"
    ));
    private final List<String> level5Words = new ArrayList<>(List.of(
            "backpack", "elephant", "laptop", "ocean", "planet"
    ));

    private List<String> gameWords;
    private int currentIndex = 0;
    private int currentLevel = 1;

    private TTSAnimatedTextView tvItemName;
    private ImageView[] starImages;
    private TextView tvTimeLeft, tv_level;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 60000; // 60 seconds
    private SpeechRecognizer speechRecognizer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_word_race);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        ImageButton micButton = findViewById(R.id.micButton);

        tv_level = findViewById(R.id.tv_level);
        tvItemName = findViewById(R.id.tv_itemname);
        tvTimeLeft = findViewById(R.id.tv_timer);
        starImages = new ImageView[]{
                findViewById(R.id.star1),
                findViewById(R.id.star2),
                findViewById(R.id.star3),
                findViewById(R.id.star4),
                findViewById(R.id.star5)
        };

        // Start the game at level 1
        loadLevel(currentLevel);
        startGame();

        micButton.setOnTouchListener((v, event) -> {
            zoomInButton(v);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (MicPermission.checkPermission(this)) {
                    micButton.setPressed(true);
                    startVoiceRecognitionActivity();
                } else {
                    MicPermission.requestPermission(this);
                }
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                if (speechRecognizer != null) {
                    speechRecognizer.cancel();
                }
                micButton.setPressed(false);
                return false;
            }
            return true;
        });

        findViewById(R.id.speakerButton).setOnClickListener(v -> {
            zoomInButton(v);
            if (currentIndex < gameWords.size()) {
                tvItemName.speakAndAnimate(gameWords.get(currentIndex));
            }
        });

        findViewById(R.id.ib_close).setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });
        findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            if (isNetworkAvailable()) {
                new UserHelper(helper -> {
                    if (helper != null) {
                        Intent intent = new Intent(Activity_WordRace.this, Activity_Points.class);
                        intent.putExtra("userUID", helper.UserUID());
                        startActivity(intent);
                    } else {
                        new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                                .setTitleText("Warning")
                                .setContentText("You need to login to access this feature.")
                                .show();
                    }
                });
            } else {
                new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                        .setTitleText("No Internet")
                        .setContentText("You need an internet connection to access this feature.")
                        .show();
            }
        });

        setZoomEffectOnHold(findViewById(R.id.ib_menu));
        setZoomEffectOnHold(findViewById(R.id.speakerButton));
        setZoomEffectOnHold(findViewById(R.id.ib_close));
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void loadLevel(int level) {
        switch (level) {
            case 1:
                gameWords = new ArrayList<>(level1Words);
                break;
            case 2:
                gameWords = new ArrayList<>(level2Words);
                break;
            case 3:
                gameWords = new ArrayList<>(level3Words);
                break;
            case 4:
                gameWords = new ArrayList<>(level4Words);
                break;
            case 5:
                gameWords = new ArrayList<>(level5Words);
                break;
            default:
                Toast.makeText(this, "Invalid level", Toast.LENGTH_SHORT).show();
                finish();
                return;
        }
        Collections.shuffle(gameWords);
    }

    private void startGame() {
        currentIndex = 0;
        timeLeftInMillis = 60000; // Reset timer to 60 seconds for each level
        resetStarImages();
        showCurrentWord();
        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                onGameOver();
            }
        }.start();
    }

    private void updateTimer() {
        int seconds = (int) (timeLeftInMillis / 1000);
        tvTimeLeft.setText("Time Left: " + seconds);
    }

    private void showCurrentWord() {
        if (currentIndex < gameWords.size()) {
            tvItemName.setText(gameWords.get(currentIndex));
            tvItemName.setInitialText(gameWords.get(currentIndex));
            tvItemName.speakAndAnimate(gameWords.get(currentIndex));
            tv_level.setText("Level " + currentLevel);
        }
    }

    public void onWordGuessed(String guessedWord) {
        if (guessedWord.equalsIgnoreCase(gameWords.get(currentIndex))) {
            new UserHelper(helper -> {
                if(helper != null){
                    helper.updateUserRewards();
                }
            });
            playSoundEffect(true);
            starImages[currentIndex].setImageResource(R.drawable.vector_s_star);
            currentIndex++;

            if (currentIndex < gameWords.size()) {
                showCurrentWord();
            } else {
                onLevelComplete();
            }
        } else {
            playSoundEffect(false);
        }
    }

    private void onLevelComplete() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (currentLevel < 5) {
            new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                    .setTitleText("Congratulations!")
                    .setContentText("You have successfully completed this level.")
                    .setConfirmText("Next Level")
                    .setConfirmClickListener(dialog -> {
                        currentLevel++;
                        loadLevel(currentLevel);
                        startGame();
                        dialog.cancel();
                    })
                    .setCancellable(false)
                    .show();
        } else {
            new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                    .setTitleText("Congratulations!")
                    .setContentText("You have successfully completed all levels.")
                    .setConfirmText("Finish")
                    .setConfirmClickListener(dialog -> finish())
                    .setCancellable(false)
                    .show();
        }
    }

    private void resetStarImages() {
        for (ImageView star : starImages) {
            star.setImageResource(R.drawable.vector_star); // Reset each star image to the default
        }
    }

    private void onGameOver() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        new StylishAlertDialog(this, StylishAlertDialog.ERROR)
                .setTitleText("Game Over!")
                .setContentText("You ran out of time.")
                .setConfirmText("Try Again")
                .setConfirmClickListener(dialog -> {
                    loadLevel(currentLevel);
                    startGame();
                    dialog.cancel();
                })
                .setCancellable(false)
                .show();
    }

    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        SpeechRecognitionListener listener = new SpeechRecognitionListener(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {}

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> finalResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (finalResults != null && !finalResults.isEmpty()) {
                    String topResult = finalResults.get(0);
                    runOnUiThread(() -> onWordGuessed(topResult));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
        speechRecognizer.startListening(intent);
    }

    private void playSoundEffect(Boolean correct) {
        // Play a sound when the user guesses the word correctly
        if(correct) {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.correct);
            mediaPlayer.start();
        } else {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.wrong);
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
