package com.capstone.funpath.Exercises;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Helpers.TTSAnimatedTextView;
import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.Permissions.MicPermission;
import com.capstone.funpath.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.util.ArrayList;

public class Activity_Pacing_And_Phrasing extends AppCompatActivity {

    private int currentPacing = 0;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 30000; // 15 seconds
    private ProgressBar timerProgressBar;
    private SpeechRecognizer speechRecognizer;
    private TTSAnimatedTextView tv_pacing;
    private ImageView iv_pacing;

    private int[] pacingImages = {
            R.drawable.vector_pacing_1,
            R.drawable.vector_pacing_2,
            R.drawable.vector_pacing_3,
    };

    private String[] pacingTexts = {
            "You make me happy",
            "Let's have fun learning together!",
            "Ready for an adventure?"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pacing_and_phrasing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        timerProgressBar = findViewById(R.id.progressBar2);


        iv_pacing = findViewById(R.id.iv_pacing);
        tv_pacing = findViewById(R.id.tv_pacing);
        ImageButton ib_close = findViewById(R.id.ib_close);
        ImageButton nextButton = findViewById(R.id.nextButton);
        ImageButton prevButton = findViewById(R.id.prevButton);
        ImageButton speakerButton = findViewById(R.id.speakerButton);
        ImageButton micButton = findViewById(R.id.micButton);

        iv_pacing.setImageResource(pacingImages[currentPacing]);
        tv_pacing.setText(pacingTexts[currentPacing]);
        tv_pacing.setInitialText(pacingTexts[currentPacing]);

        nextButton.setOnClickListener(v -> {
            zoomInButton(v);
            if (currentPacing < pacingTexts.length - 1) {
                currentPacing++;
                countDownTimer.cancel();
                timeLeftInMillis = 30000;
                timerProgressBar.setProgress(100);
                tv_pacing.stop();
                tv_pacing.setText(pacingTexts[currentPacing]);
                tv_pacing.speakAndAnimate(pacingTexts[currentPacing]);
                iv_pacing.setImageResource(pacingImages[currentPacing]);
                startTimer();
            }else if(currentPacing == pacingTexts.length - 1){
                finish();
            }
        });

        prevButton.setOnClickListener(v -> {
            zoomInButton(v);
            if (currentPacing > 0) {
                currentPacing--;
                countDownTimer.cancel();
                timeLeftInMillis = 30000;
                timerProgressBar.setProgress(100);
                tv_pacing.stop();
                tv_pacing.setText(pacingTexts[currentPacing]);
                tv_pacing.speakAndAnimate(pacingTexts[currentPacing]);
                iv_pacing.setImageResource(pacingImages[currentPacing]);
                startTimer();
            }
        });

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

        speakerButton.setOnClickListener(v -> {
            zoomInButton(v);
            tv_pacing.speakAndAnimate(pacingTexts[currentPacing]);
        });

        startTimer();

        ib_close.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });

        setZoomEffectOnHold(tv_pacing);
        setZoomEffectOnHold(nextButton);
        setZoomEffectOnHold(prevButton);
        setZoomEffectOnHold(speakerButton);
        setZoomEffectOnHold(iv_pacing);
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

    private void onGameOver() {
        new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                .setTitleText("Time is up!")
                .setContentText("Please try again.")
                .setConfirmText("Restart")
                .setConfirmButtonBackgroundColor(Color.RED)
                .setConfirmClickListener(dialog -> {
                    dialog.dismiss();
                    countDownTimer.cancel();
                    currentPacing = 0;
                    timeLeftInMillis = 30000;
                    timerProgressBar.setProgress(100);
                    tv_pacing.stop();
                    tv_pacing.setText(pacingTexts[currentPacing]);
                    tv_pacing.speakAndAnimate(pacingTexts[currentPacing]);
                    iv_pacing.setImageResource(pacingImages[currentPacing]);
                    startTimer();
                })
                .setCancellable(false)
                .show();
    }

    private void updateTimer() {
        int seconds = (int) (timeLeftInMillis / 1000);
        // Update the progress bar
        double progress = (double) seconds / 30;
        timerProgressBar.setProgress((int) (progress * 100));
    }

    private void startVoiceRecognitionActivity() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

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
                    runOnUiThread(() -> checkAnswer(topResult));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
        speechRecognizer.startListening(intent);
    }

    private void checkAnswer(String text) {
        Toast.makeText(this, text + " - " + pacingTexts[currentPacing], Toast.LENGTH_SHORT).show();

        // Remove non-alphanumeric characters and compare text
        String removedPunctuation = text.replaceAll("[^a-zA-Z0-9]", "");
        String removedPunctuation2 = pacingTexts[currentPacing].replaceAll("[^a-zA-Z0-9]", "");

        if (removedPunctuation.equalsIgnoreCase(removedPunctuation2)) {
            // Correct answer
            if (!isFinishing() && !isDestroyed()) {
                new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                        .setTitleText("Correct")
                        .setContentText("Well done!")
                        .setConfirmText("Next")
                        .setConfirmButtonBackgroundColor(Color.GREEN)
                        .setConfirmClickListener(dialog -> {
                            dialog.dismiss();
                            countDownTimer.cancel();
                            if (currentPacing < pacingTexts.length - 1) {
                                currentPacing++;
                                timeLeftInMillis = 30000;
                                timerProgressBar.setProgress(100);
                                tv_pacing.stop();
                                tv_pacing.setText(pacingTexts[currentPacing]);
                                tv_pacing.speakAndAnimate(pacingTexts[currentPacing]);
                                iv_pacing.setImageResource(pacingImages[currentPacing]);
                                startTimer();
                            } else {
                                if (!isFinishing() && !isDestroyed()) {
                                    new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                                            .setTitleText("Congratulations")
                                            .setContentText("You have completed the exercise!")
                                            .setConfirmText("Finish")
                                            .setConfirmButtonBackgroundColor(Color.GREEN)
                                            .setConfirmClickListener(dialog1 -> {
                                                dialog1.dismiss();
                                                finish();
                                            })
                                            .setCancellable(false)
                                            .show();
                                }
                            }
                        })
                        .setCancellable(false)
                        .show();
            }
        } else {
            // Incorrect answer or not recognized
            if (text.isEmpty()) {
                // Handle case where no speech is recognized
                if (!isFinishing() && !isDestroyed()) {
                    new StylishAlertDialog(this, StylishAlertDialog.ERROR)
                            .setTitleText("Speech Not Recognized")
                            .setContentText("Please try again.")
                            .setConfirmText("Retry")
                            .setConfirmButtonBackgroundColor(Color.RED)
                            .setConfirmClickListener(dialog -> dialog.dismiss())
                            .setCancellable(false)
                            .show();
                }
            } else {
                // Handle incorrect answer
                if (!isFinishing() && !isDestroyed()) {
                    new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                            .setTitleText("Oops! Try again.")
                            .setContentText("Keep going, you're almost there!")
                            .setConfirmText("Retry")
                            .setConfirmButtonBackgroundColor(Color.RED)
                            .setConfirmClickListener(dialog -> dialog.dismiss())
                            .setCancellable(false)
                            .show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}