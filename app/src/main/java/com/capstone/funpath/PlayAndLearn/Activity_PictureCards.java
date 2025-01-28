package com.capstone.funpath.PlayAndLearn;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Activity_Points;
import com.capstone.funpath.Helpers.JsonHelper;
import com.capstone.funpath.Helpers.TTSAnimatedTextView;
import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.Permissions.MicPermission;
import com.capstone.funpath.R;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.util.ArrayList;
import java.util.List;

public class Activity_PictureCards extends AppCompatActivity {

    private List<JsonHelper.Item> items;
    private int currentIndex = 0;
    private TTSAnimatedTextView tv_itemname;
    private ImageView iv_itemImage;
    private SpeechRecognizer speechRecognizer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_picture_cards);
        JsonHelper jsonHelper = new JsonHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_itemname = findViewById(R.id.tv_itemname);
        iv_itemImage = findViewById(R.id.imageView4);
        ImageButton ib_close = findViewById(R.id.ib_close);
        ImageButton prevButton = findViewById(R.id.prevButton);
        ImageButton nextButton = findViewById(R.id.nextButton);
        ImageButton speakerButton = findViewById(R.id.speakerButton);
        ImageButton micButton = findViewById(R.id.micButton);

        String itemText = getIntent().getStringExtra("Letter");
        items = jsonHelper.loadItemsFromJson(itemText, R.raw.picture_cards);
        if (items != null && !items.isEmpty()) {
            updateUI();
        }

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

        prevButton.setOnClickListener(v -> {
            zoomInButton(v);
            if (currentIndex > 0) {
                currentIndex--;
                updateUI();
            }
        });

        nextButton.setOnClickListener(v -> {
            zoomInButton(v);
            getNextLevel();
        });

        speakerButton.setOnClickListener(v -> {
            zoomInButton(v);
            tv_itemname.speakAndAnimate(tv_itemname.getText().toString());
        });

        ib_close.setOnClickListener(v -> {
            zoomInButton(v);
            getOnBackPressedDispatcher().onBackPressed();
        });

        findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            if (isInternetAvailable()) {
                new UserHelper(helper -> {
                    if (helper != null) {
                        Intent intent = new Intent(Activity_PictureCards.this, Activity_Points.class);
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
                        .setTitleText("No Internet Connection")
                        .setContentText("Please check your internet connection and try again.")
                        .show();
            }
        });

        setZoomEffectOnHold(nextButton);
        setZoomEffectOnHold(prevButton);
        setZoomEffectOnHold(ib_close);
        setZoomEffectOnHold(speakerButton);
        setZoomEffectOnHold(prevButton);
        setZoomEffectOnHold(findViewById(R.id.ib_menu));
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

    private void updateUI() {
        JsonHelper.Item currentItem = items.get(currentIndex);
        tv_itemname.setText(currentItem.name);
        tv_itemname.setInitialText(tv_itemname.getText().toString());
        tv_itemname.speakAndAnimate(currentItem.name);
        @SuppressLint("DiscouragedApi") int imageResId = getResources().getIdentifier(currentItem.image, "drawable", getPackageName());
        iv_itemImage.setImageResource(imageResId);
    }

    private String getNextLetter(String currentLetter) {
        if (currentLetter == null || currentLetter.length() != 2) {
            throw new IllegalArgumentException("Input must be a two-letter string");
        }

        char firstChar = currentLetter.charAt(0);
        char secondChar = currentLetter.charAt(1);

        if (firstChar == 'Z' && secondChar == 'z') {
            return "END";
        }

        firstChar++;
        secondChar++;

        return "" + firstChar + secondChar;
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
        if (text.equalsIgnoreCase(items.get(currentIndex).name)) {
            // Answer is correct
            new UserHelper(helper -> {
                if (helper != null) {
                    helper.updateUserRewards();
                }
            });
            playSoundEffect(true);

            // Show "Correct" dialog
            if (!isFinishing() && !isDestroyed()) {
                runOnUiThread(() -> {
                    new StylishAlertDialog(Activity_PictureCards.this, StylishAlertDialog.SUCCESS)
                            .setTitleText("Correct!")
                            .setContentText("Good job! You got it right.")
                            .setConfirmText("Next")
                            .setConfirmClickListener(dialog -> {
                                dialog.dismiss(); // Close the dialog
                                getNextLevel();   // Go to the next level
                            })
                            .setCancellable(false)
                            .show();
                });
            }
        } else {
            // Answer is incorrect
            playSoundEffect(false);

            // Show "Incorrect" dialog
            if (!isFinishing() && !isDestroyed()) {
                runOnUiThread(() -> {
                    new StylishAlertDialog(Activity_PictureCards.this, StylishAlertDialog.WARNING)
                            .setTitleText("Oops! Try again.")
                            .setContentText("Keep going, you're almost there!")
                            .setConfirmText("OK")
                            .setConfirmClickListener(dialog -> dialog.dismiss())
                            .setCancellable(false)
                            .show();
                });
            }
        }
    }

    private void getNextLevel() {
        if (currentIndex < items.size() - 1) {
            currentIndex++;
            updateUI();
        } else {
            String nextLetter = getNextLetter(getIntent().getStringExtra("Letter"));
            if(nextLetter.equals("END")){
                new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                        .setTitleText("Congratulations!")
                        .setContentText("You have successfully completed all levels.")
                        .setConfirmText("Finish")
                        .setConfirmClickListener(dialog-> finish())
                        .setCancellable(false)
                        .show();
            }else{
                new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                        .setTitleText("Congratulations!")
                        .setContentText("You have successfully completed this level.")
                        .setConfirmText("Next Level")
                        .setConfirmClickListener(dialog-> {
                            Intent intent = new Intent(this, Activity_PictureCards.class);
                            intent.putExtra("Letter", nextLetter);
                            startActivity(intent);
                            finish();
                        })
                        .setCancellable(false)
                        .show();
            }
        }
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

    // Check if there is an active internet connection
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
