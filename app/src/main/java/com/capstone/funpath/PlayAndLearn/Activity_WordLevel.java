package com.capstone.funpath.PlayAndLearn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Activity_Points;
import com.capstone.funpath.Assessment.Activity_Passed;
import com.capstone.funpath.Helpers.JsonHelper;
import com.capstone.funpath.Helpers.TTSAnimatedTextView;
import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.R;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint("UseCompatLoadingForDrawables")
public class Activity_WordLevel extends AppCompatActivity {

    private List<JsonHelper.Item> items;
    private JsonHelper.Item currentItem;
    private int currentIndex = 0;
    private ImageView iv_itemImage;
    private LinearLayout buttonContainerLayout, buttonChoicesContainerLayout;
    private List<Button> answerButtons;
    private TTSAnimatedTextView tv_itemname;
    String itemText;
    int fixedWidth;
    int marginInDp;
    int marginInPx;
    int buttonChoicesWidth;
    int buttonWidth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_word_level);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iv_itemImage = findViewById(R.id.imageView4);
        tv_itemname = findViewById(R.id.tv_itemname);
        buttonContainerLayout = findViewById(R.id.buttonContainerLayout);
        buttonChoicesContainerLayout = findViewById(R.id.buttonChoicesContainerLayout);
        ImageButton redoButton = findViewById(R.id.redoButton); // Add redo button here
        ImageButton speakerButton = findViewById(R.id.speakerButton);
        ImageButton ib_close = findViewById(R.id.ib_close);
        TextView tv_picturecards = findViewById(R.id.tv_picturecards);
        tv_picturecards.setText("Word Level");

        itemText = getIntent().getStringExtra("level");
        JsonHelper jsonHelper = new JsonHelper(this);

        items = jsonHelper.loadItemsFromJson(itemText, R.raw.puzzle_challenge);
        if (items != null && !items.isEmpty()) {
            updateUI();
        }

        speakerButton.setOnClickListener(v -> {
            zoomInButton(v);
            tv_itemname.speakAndAnimate(tv_itemname.getText().toString());
        });

        ib_close.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });
        findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            if (isNetworkAvailable()) {
                new UserHelper(helper -> {
                    if (helper != null) {
                        Intent intent = new Intent(Activity_WordLevel.this, Activity_Points.class);
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

        findViewById(R.id.nextButton).setOnClickListener(v -> {
            zoomInButton(v);
            currentIndex++;
            getNextLevel();
        });

        // Redo button functionality
        redoButton.setOnClickListener(v -> {
            zoomInButton(v);
            resetCurrentWord();
        });

        findViewById(R.id.prevButton).setOnClickListener(v -> {
            zoomInButton(v);
            if (currentIndex > 0) {
                currentIndex--;
                updateUI();
            }
        });

        setZoomEffectOnHold(findViewById(R.id.ib_menu));
        setZoomEffectOnHold(findViewById(R.id.prevButton));
        setZoomEffectOnHold(findViewById(R.id.nextButton));
        setZoomEffectOnHold(speakerButton);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void updateUI() {
        currentItem = items.get(currentIndex);
        tv_itemname.setText(currentItem.name);
        tv_itemname.setInitialText(tv_itemname.getText().toString());
        tv_itemname.speakAndAnimate(currentItem.name);

        setImage(currentItem.image);
        clearExistingButtons();

        int[] buttonDimensions = getButtonDimensions(currentItem.name.length());
        int buttonWidth = buttonDimensions[0];
        int buttonChoicesWidth = buttonDimensions[1];
        int marginInPx = buttonDimensions[2];

        answerButtons = createAnswerButtons(currentItem.name, buttonWidth, marginInPx);
        List<Button> choiceButtons = createChoiceButtons(currentItem.name, buttonChoicesWidth, marginInPx);

        setButtonListeners(answerButtons, choiceButtons, currentItem.name);
        buttonContainerLayout.setGravity(Gravity.CENTER);
        buttonChoicesContainerLayout.setGravity(Gravity.CENTER);
    }

    private void setImage(String imageName) {
        @SuppressLint("DiscouragedApi")
        int imageResId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        iv_itemImage.setImageResource(imageResId);
    }

    private void clearExistingButtons() {
        buttonContainerLayout.removeAllViews();
        buttonChoicesContainerLayout.removeAllViews();
    }

    private int[] getButtonDimensions(int nameLength) {


        if (nameLength > 8) {
            fixedWidth = 34;
            marginInDp = 1;
        } else if (nameLength > 5) {
            fixedWidth = 38;
            marginInDp = 2;
        }else {
            fixedWidth = 58;
            marginInDp = 2;
        }

        buttonWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fixedWidth, getResources().getDisplayMetrics());
        buttonChoicesWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fixedWidth + 4, getResources().getDisplayMetrics());
        marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginInDp, getResources().getDisplayMetrics());

        return new int[]{buttonWidth, buttonChoicesWidth, marginInPx};
    }

    private List<Button> createAnswerButtons(String name, int buttonWidth, int marginInPx) {
        List<Button> answerButtons = new ArrayList<>();
        for (char ignored : name.toCharArray()) {
            Button button = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
            button.setLayoutParams(params);
            button.setBackground(getDrawable(R.drawable.design_lightgray_rounded_black_stroke));
            button.setTextColor(getColor(R.color.white));
            button.setTextSize(32);
            button.setText(""); // Initially set the text to empty

            buttonContainerLayout.addView(button);
            answerButtons.add(button);
        }
        return answerButtons;
    }

    private List<Button> createChoiceButtons(String name, int buttonChoicesWidth, int marginInPx) {
        List<Button> choiceButtons = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < name.length(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices); // Shuffle the list for random positions

        for (int i = 0; i < name.length(); i++) {
            Button button = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonChoicesWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
            button.setLayoutParams(params);
            button.setBackground(getDrawable(R.drawable.design_orange_rounded));
            button.setTextColor(getColor(R.color.white));
            button.setTextSize(32);

            // Set letters to buttons in random positions in buttonChoicesContainerLayout
            char letter = name.charAt(indices.get(i));
            button.setText(String.valueOf(letter));
            buttonChoicesContainerLayout.addView(button);
            choiceButtons.add(button);
        }
        return choiceButtons;
    }

    private void setButtonListeners(List<Button> answerButtons, List<Button> choiceButtons, String correctAnswer) {
        for (Button choiceButton : choiceButtons) {
            choiceButton.setOnClickListener(v -> {
                handleChoiceButtonClick(choiceButton, answerButtons, correctAnswer);
            });
        }

        for (Button answerButton : answerButtons) {
            answerButton.setOnClickListener(v -> {
                handleAnswerButtonClick(answerButton);
            });
        }
    }

    private void handleChoiceButtonClick(Button choiceButton, List<Button> answerButtons, String correctAnswer) {
        // Create the zoom-in animation for the choiceButton
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(choiceButton, "scaleX", 1f, 1.2f, 1f); // Scale in X direction
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(choiceButton, "scaleY", 1f, 1.2f, 1f); // Scale in Y direction
        scaleX.setDuration(200); // Duration of the zoom-in animation
        scaleY.setDuration(200); // Duration of the zoom-in animation

        // Start the zoom-in animation before proceeding with the rest of the logic
        scaleX.start();
        scaleY.start();

        // Delay the actual handling of the button click until after the animation finishes
        scaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Once the zoom-in animation ends, execute the original logic
                for (Button answerButton : answerButtons) {
                    if (answerButton.getText().toString().isEmpty()) {
                        answerButton.setText(choiceButton.getText().toString());
                        answerButton.setTextColor(getColor(R.color.white));
                        answerButton.setBackground(getDrawable(R.drawable.design_orange_rounded));
                        buttonChoicesContainerLayout.removeView(choiceButton);
                        checkAnswer(answerButtons, correctAnswer);
                        break;
                    }
                }
            }
        });
    }


    private void handleAnswerButtonClick(Button answerButton) {
        if (!answerButton.getText().toString().isEmpty()) {
            String letter = answerButton.getText().toString();
            Button choiceButton = createReturnChoiceButton(letter);
            buttonChoicesContainerLayout.addView(choiceButton);
            choiceButton.setOnClickListener(v -> {
                handleReturnChoiceButtonClick(choiceButton);
            });
            answerButton.setText("");
            answerButton.setBackground(getDrawable(R.drawable.design_lightgray_rounded_black_stroke));
        }
    }

    private Button createReturnChoiceButton(String letter) {
        Button choiceButton = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                buttonChoicesWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
        choiceButton.setLayoutParams(params);
        choiceButton.setBackground(getDrawable(R.drawable.design_orange_rounded));
        choiceButton.setTextColor(getColor(R.color.white));
        choiceButton.setTextSize(32);
        choiceButton.setText(letter);
        return choiceButton;
    }

    private void handleReturnChoiceButtonClick(Button choiceButton) {
        // Create the zoom-in animation for the choiceButton
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(choiceButton, "scaleX", 1f, 1.2f, 1f); // Scale in X direction
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(choiceButton, "scaleY", 1f, 1.2f, 1f); // Scale in Y direction
        scaleX.setDuration(200); // Duration of the zoom-in animation
        scaleY.setDuration(200); // Duration of the zoom-in animation

        // Start the zoom-in animation before proceeding with the rest of the logic
        scaleX.start();
        scaleY.start();

        // Delay the actual handling of the button click until after the animation finishes
        scaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Once the zoom-in animation ends, execute the original logic
                for (Button answerButtonInner : answerButtons) {
                    if (answerButtonInner.getText().toString().isEmpty()) {
                        answerButtonInner.setText(choiceButton.getText().toString());
                        answerButtonInner.setBackground(getDrawable(R.drawable.design_orange_rounded));
                        buttonChoicesContainerLayout.removeView(choiceButton);
                        checkAnswer(answerButtons, currentItem.name); // Ensure currentItem is available in scope
                        break;
                    }
                }
            }
        });
    }

    // Method to check if the current answer is correct
    private void checkAnswer(List<Button> answerButtons, String correctAnswer) {
        StringBuilder currentAnswer = new StringBuilder();

        // Collect the current answer from buttonContainerLayout
        for (Button answerButton : answerButtons) {
            currentAnswer.append(answerButton.getText().toString());
        }

        // Compare with the expected answer
        if (currentAnswer.toString().equals(correctAnswer)) {
            new UserHelper(helper -> {
                if(helper != null){
                    helper.updateUserRewards();
                }
            });
            playSoundEffect(true);
            currentIndex++; // Move to the next item if needed
            getNextLevel();
        } else if (currentAnswer.length() == correctAnswer.length()) {
            playSoundEffect(false);
        }
    }

    private void resetCurrentWord() {
        // Clear existing buttons
        clearExistingButtons();

        // Recreate answer and choice buttons
        int[] buttonDimensions = getButtonDimensions(currentItem.name.length());
        int buttonWidth = buttonDimensions[0];
        int buttonChoicesWidth = buttonDimensions[1];
        int marginInPx = buttonDimensions[2];

        answerButtons = createAnswerButtons(currentItem.name, buttonWidth, marginInPx);
        List<Button> choiceButtons = createChoiceButtons(currentItem.name, buttonChoicesWidth, marginInPx);

        setButtonListeners(answerButtons, choiceButtons, currentItem.name);

        // Reset item name animation
        tv_itemname.setInitialText(tv_itemname.getText().toString());
        tv_itemname.speakAndAnimate(currentItem.name);
    }

    private void getNextLevel(){
        if (currentIndex < items.size()) {
            updateUI(); // Update UI for the next item
        } else {
            new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                    .setTitleText("Congratulations!")
                    .setContentText("You have successfully completed this level.")
                    .setConfirmText("Next Level")
                    .setConfirmClickListener(dialog-> {
                        Intent intent = new Intent(this, Activity_SentenceLevel.class);
                        intent.putExtra("level", "sentence");
                        startActivity(intent);
                        finish();
                    })
                    .setCancellable(false)
                    .show();
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


}
