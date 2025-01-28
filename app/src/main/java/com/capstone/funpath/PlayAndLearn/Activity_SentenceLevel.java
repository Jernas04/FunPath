package com.capstone.funpath.PlayAndLearn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class Activity_SentenceLevel extends AppCompatActivity {

    private List<JsonHelper.Item> items;
    private JsonHelper.Item currentItem;
    private int currentIndex = 0;
    private ImageView iv_itemImage;
    private LinearLayout buttonContainerLayout, buttonChoicesContainerLayout;
    private List<ImageButton> answerButtons;
    private TTSAnimatedTextView tv_container;
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
        setContentView(R.layout.activity_sentence_level);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iv_itemImage = findViewById(R.id.imageView4);
        tv_container = findViewById(R.id.tv_container);
        buttonContainerLayout = findViewById(R.id.buttonChoicesContainerLayout);
        buttonChoicesContainerLayout = findViewById(R.id.buttonContainerLayout);

        itemText = getIntent().getStringExtra("level");
        JsonHelper jsonHelper = new JsonHelper(this);

        ImageButton ib_close = findViewById(R.id.ib_close);
        ImageButton speakerButton = findViewById(R.id.speakerButton);

        items = jsonHelper.loadItemsFromJson(itemText, R.raw.puzzle_challenge);
        if (items != null && !items.isEmpty()) {
            updateUI();
        }

        speakerButton.setOnClickListener(v -> {
            zoomInButton(v);
            tv_container.speakAndAnimate(tv_container.getText().toString());
        });

        ib_close.setOnClickListener(v -> {
            zoomInButton(v);
            finish();
        });
        findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            if (isNetworkAvailable()) {
                // Check if the user is logged in
                new UserHelper(helper -> {
                    if (helper != null) {
                        // User is logged in, proceed to Activity_Points
                        Intent intent = new Intent(Activity_SentenceLevel.this, Activity_Points.class);
                        intent.putExtra("userUID", helper.UserUID());
                        startActivity(intent);
                    } else {
                        // User is not logged in
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

        findViewById(R.id.redoButton).setOnClickListener(v -> {
            zoomInButton(v);
            resetCurrentPuzzle();
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

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void updateUI() {
        currentItem = items.get(currentIndex);
        tv_container.setText(currentItem.name);
        tv_container.setInitialText(currentItem.name);
        tv_container.speakAndAnimate(currentItem.name);

        setImage(currentItem.image);
        clearExistingButtons();

        int[] buttonDimensions = getButtonDimensions(currentItem.choices.size());
        int buttonWidth = buttonDimensions[0];
        int buttonChoicesWidth = buttonDimensions[1];
        int marginInPx = buttonDimensions[2];

        // Shuffle the choices
        List<String> shuffledChoices = new ArrayList<>(currentItem.choices);
        Collections.shuffle(shuffledChoices);

        answerButtons = createAnswerButtons(shuffledChoices, buttonWidth, marginInPx);
        List<ImageButton> choiceButtons = createChoiceButtons(shuffledChoices, buttonChoicesWidth, marginInPx);

        setButtonListeners(answerButtons, choiceButtons, currentItem.choices);
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
        if (nameLength > 2) {
            fixedWidth = 98;
            marginInDp = 1;
        } else if (nameLength > 3) {
            fixedWidth = 108;
            marginInDp = 2;
        } else {
            fixedWidth = 98;
            marginInDp = 2;
        }

        buttonWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fixedWidth, getResources().getDisplayMetrics());
        buttonChoicesWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fixedWidth + 4, getResources().getDisplayMetrics());
        marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginInDp, getResources().getDisplayMetrics());

        return new int[]{buttonWidth, buttonChoicesWidth, marginInPx};
    }

    private List<ImageButton> createAnswerButtons(List<String> choices, int buttonWidth, int marginInPx) {
        List<ImageButton> answerButtons = new ArrayList<>();
        for (String ignored : choices) {
            ImageButton button = new ImageButton(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonWidth, buttonWidth);
            params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
            button.setLayoutParams(params);

            buttonContainerLayout.addView(button);
            answerButtons.add(button);
        }
        return answerButtons;
    }

    private List<ImageButton> createChoiceButtons(List<String> choices, int buttonChoicesWidth, int marginInPx) {
        List<ImageButton> choiceButtons = new ArrayList<>();
        for (String choice : choices) {
            ImageButton button = new ImageButton(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonChoicesWidth, buttonChoicesWidth);
            params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
            button.setLayoutParams(params);

            @SuppressLint("DiscouragedApi")
            int imageResId = getResources().getIdentifier(choice, "drawable", getPackageName());
            Drawable drawable = getResources().getDrawable(imageResId);
            button.setImageDrawable(drawable);
            button.setTag(choice);

            buttonChoicesContainerLayout.addView(button);
            choiceButtons.add(button);
        }
        return choiceButtons;
    }

    private void setButtonListeners(List<ImageButton> answerButtons, List<ImageButton> choiceButtons, List<String> correctAnswer) {
        for (ImageButton choiceButton : choiceButtons) {
            choiceButton.setOnClickListener(v -> {
                handleChoiceButtonClick(choiceButton, answerButtons, correctAnswer);
            });
        }

        for (ImageButton answerButton : answerButtons) {
            answerButton.setOnClickListener(v -> {
                handleAnswerButtonClick(answerButton);
            });
        }
    }

    private void handleChoiceButtonClick(ImageButton choiceButton, List<ImageButton> answerButtons, List<String> correctAnswer) {
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
                for (ImageButton answerButton : answerButtons) {
                    if (answerButton.getDrawable() == null) {
                        answerButton.setImageDrawable(choiceButton.getDrawable());
                        answerButton.setTag(choiceButton.getTag());
                        buttonChoicesContainerLayout.removeView(choiceButton);
                        checkAnswer(answerButtons, correctAnswer);
                        break;
                    }
                }
            }
        });
    }


    private void handleAnswerButtonClick(ImageButton answerButton) {
        if (answerButton.getDrawable() != null) {
            Drawable drawable = answerButton.getDrawable();
            String tag = (String) answerButton.getTag();
            ImageButton choiceButton = createReturnChoiceButton(drawable, tag);

            buttonChoicesContainerLayout.addView(choiceButton);
            choiceButton.setOnClickListener(v -> {
                handleReturnChoiceButtonClick(choiceButton);
            });
            answerButton.setImageDrawable(null);
            answerButton.setTag(null);
        }
    }

    private ImageButton createReturnChoiceButton(Drawable drawable, String tag) {
        ImageButton choiceButton = new ImageButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonChoicesWidth, buttonChoicesWidth);
        params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
        choiceButton.setLayoutParams(params);
        choiceButton.setImageDrawable(drawable);
        choiceButton.setTag(tag);
        return choiceButton;
    }

    private void handleReturnChoiceButtonClick(ImageButton choiceButton) {
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
                for (ImageButton answerButtonInner : answerButtons) {
                    if (answerButtonInner.getDrawable() == null) {
                        answerButtonInner.setImageDrawable(choiceButton.getDrawable());
                        answerButtonInner.setTag(choiceButton.getTag());
                        buttonChoicesContainerLayout.removeView(choiceButton);
                        checkAnswer(answerButtons, currentItem.choices); // Ensure currentItem is available in scope
                        break;
                    }
                }
            }
        });
    }


    // Method to check if the current answer is correct
    private void checkAnswer(List<ImageButton> answerButtons, List<String> correctAnswer) {
        List<String> currentAnswer = new ArrayList<>();

        // Collect the current answer from buttonContainerLayout
        for (ImageButton answerButton : answerButtons) {
            if (answerButton.getDrawable() != null) {
                String tag = (String) answerButton.getTag();
                currentAnswer.add(tag);
            }
        }

        // Compare with the expected answer
        if (currentAnswer.equals(correctAnswer)) {
            new UserHelper(helper -> {
                if(helper != null){
                    helper.updateUserRewards();
                }
            });
            playSoundEffect(true);
            currentIndex++; // Move to the next item if needed
            getNextLevel();
        }else if(currentAnswer.size() == correctAnswer.size()){
            playSoundEffect(false);
        }
    }

    // Method to reset the current puzzle to its initial state
    private void resetCurrentPuzzle() {
        // Clear existing buttons
        clearExistingButtons();

        // Reinitialize the answer buttons and choice buttons
        int[] buttonDimensions = getButtonDimensions(currentItem.choices.size());
        int buttonWidth = buttonDimensions[0];
        int buttonChoicesWidth = buttonDimensions[1];
        int marginInPx = buttonDimensions[2];

        // Shuffle the choices again
        List<String> shuffledChoices = new ArrayList<>(currentItem.choices);
        Collections.shuffle(shuffledChoices);

        answerButtons = createAnswerButtons(shuffledChoices, buttonWidth, marginInPx);
        List<ImageButton> choiceButtons = createChoiceButtons(shuffledChoices, buttonChoicesWidth, marginInPx);

        // Set listeners for the buttons
        setButtonListeners(answerButtons, choiceButtons, currentItem.choices);

        // Reset gravity for layout containers
        buttonContainerLayout.setGravity(Gravity.CENTER);
        buttonChoicesContainerLayout.setGravity(Gravity.CENTER);

        // Optionally reset any other UI elements like animations or text
        tv_container.setInitialText(currentItem.name);
        tv_container.speakAndAnimate(currentItem.name);
    }

    private void getNextLevel(){
        if (currentIndex < items.size()) {
            updateUI(); // Update UI for the next item
        } else {
            new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                    .setTitleText("Congratulations!")
                    .setContentText("You have successfully completed all levels.")
                    .setConfirmText("Finish")
                    .setConfirmClickListener(dialog-> finish())
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
