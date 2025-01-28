package com.capstone.funpath.Assessment;

import static android.content.ContentValues.TAG;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Activity_KidsProfile;
import com.capstone.funpath.Activity_Points;
import com.capstone.funpath.Helpers.AudioRecorder;
import com.capstone.funpath.Helpers.JsonHelper;
import com.capstone.funpath.Helpers.SpeechRecognitionListener;
import com.capstone.funpath.Helpers.StutteringClassificationHelper;
import com.capstone.funpath.Helpers.TTSAnimatedTextView;
import com.capstone.funpath.Helpers.TextClassifierHelper;
import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.Permissions.MicPermission;
import com.capstone.funpath.R;
import com.capstone.funpath.asr.IRecorderListener;
import com.capstone.funpath.asr.IWhisperListener;
import com.capstone.funpath.asr.Recorder;
import com.capstone.funpath.asr.Whisper;
import com.capstone.funpath.utils.WaveUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mediapipe.tasks.text.textclassifier.TextClassifierResult;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public class Activity_BeginnerToAdvanced extends AppCompatActivity {

    private List<JsonHelper.Item> items;
    private int currentIndex = 0;
    private int userIndex = 0;
    private int lastVisitedIndex = 0;
    private TTSAnimatedTextView tv_itemname;
    private TextView tv_level;
    private ImageView iv_itemImage;
    private ImageButton nextButton, micButton;
    private String itemText;
    String uid = "";
    TextClassifierHelper textClassifierHelper;
    private Whisper mWhisper = null;
    private Recorder mRecorder = null;
    private ConstraintLayout progressLayout;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 30000; // 30 seconds
    private ProgressBar timerProgressBar;
    private boolean isGameOver = false;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beginner_to_advanced);

        final Handler handler = new Handler(Looper.getMainLooper());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check for internet connection
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }

        initializeComponents();
        loadItems();

        micButton.setOnTouchListener((v, event) -> handleMicButtonTouch(event));

        textClassifierHelper = new TextClassifierHelper("bert_classifier.tflite", this,
                new TextClassifierHelper.TextResultsListener() {
                    @Override
                    public void onError(String error) {
                        // Handle error case
                        Toast.makeText(Activity_BeginnerToAdvanced.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onResult(TextClassifierResult results, long inferenceTime) {
                        // Handle results
                        String label = results.classificationResult().classifications().get(0).categories().get(0).categoryName();
                        float score = results.classificationResult().classifications().get(0).categories().get(0).score();
                        if (label != null) {
                            Log.d(TAG, "c: " + label);
                            Log.d(TAG, "onResult: " + score);
                            long timestamp = System.currentTimeMillis(); // Keep it as long
                            double computedScore = StutteringClassificationHelper.computeStutteringScore(label, score);

                            try {
                                new UserHelper(helper -> {
                                    Log.d(TAG, "onResult: " + helper.getName());
                                    HashMap<String, Object> chartsData = new HashMap<>();
                                    chartsData.put("timestamp", timestamp);
                                    chartsData.put("score", computedScore);
                                    helper.updateChartField(chartsData);
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "Error creating UserHelper: ", e);
                            }

                        }
                    }
                }
        );

        String[] extensionsToCopy = {"pcm", "bin", "wav", "tflite"};
        copyAssetsWithExtensionsToDataFolder(this, extensionsToCopy);

        String modelPath;
        String vocabPath;
        modelPath = getFilePath("whisper-tiny-en.tflite");
        vocabPath = getFilePath("filters_vocab_en.bin");
        boolean useMultilingual = false;

        mWhisper = new Whisper(this);
        mWhisper.loadModel(modelPath, vocabPath, useMultilingual);
        mWhisper.setListener(new IWhisperListener() {
            @Override
            public void onUpdateReceived(String message) {
                Log.d(TAG, "Update is received, Message: " + message);
                //handler.post(() -> tvResult.setText(message));

                if (message.equals(Whisper.MSG_PROCESSING)) {
                    //handler.post(() -> tvResult.setText(""));
                } else if (message.equals(Whisper.MSG_FILE_NOT_FOUND)) {
                    // write code as per need to handled this error
                    Log.d(TAG, "File not found error...!");
                }
            }

            @Override
            public void onResultReceived(String result) {
                Log.d(TAG, "Result: " + result);
                handler.post(() -> {
                    processTranscription(result);
                });
            }
        });

        mRecorder = new Recorder(this);
        mRecorder.setListener(new IRecorderListener() {
            @Override
            public void onUpdateReceived(String message) {
                Log.d(TAG, "Update is received, Message: " + message);
                //handler.post(() -> tvStatus.setText(message));

                if (message.equals(Recorder.MSG_RECORDING)) {
                    //handler.post(() -> tvResult.setText(""));
                    //handler.post(() -> btnMicRec.setText(Recorder.ACTION_STOP));
                } else if (message.equals(Recorder.MSG_RECORDING_DONE)) {
                    //handler.post(() -> btnMicRec.setText(Recorder.ACTION_RECORD));
                }
            }

            @Override
            public void onDataReceived(float[] samples) {
                mWhisper.writeBuffer(samples);
            }
        });

        setupNavigationButtons();
        setupSpeakerButton();
        setupCloseButton();

        findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            new UserHelper(helper -> {
                if (helper != null) {
                    Intent intent = new Intent(Activity_BeginnerToAdvanced.this, Activity_KidsProfile.class);
                    intent.putExtra("userUID", helper.UserUID());
                    startActivity(intent);
                }
            });
        });

        setZoomEffectOnHold(findViewById(R.id.ib_menu));
        setZoomEffectOnHold(findViewById(R.id.ib_close));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new StylishAlertDialog(this, StylishAlertDialog.ERROR)
                .setTitleText("No Internet Connection")
                .setContentText("Please check your internet connection and try again.")
                .setConfirmText("OK")
                .setConfirmButtonBackgroundColor(Color.RED)
                .setConfirmClickListener(dialog -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancellable(false)
                .show();
    }

    private void initializeComponents() {

        timerProgressBar = findViewById(R.id.progressBar2);
        tv_itemname = findViewById(R.id.tv_itemname);
        iv_itemImage = findViewById(R.id.imageView4);
        tv_level = findViewById(R.id.tv_level);
        nextButton = findViewById(R.id.nextButton);
        micButton = findViewById(R.id.micButton);
        itemText = getIntent().getStringExtra("Level");
        progressLayout = findViewById(R.id.progressLayout);

        TextView tv_difficulty = findViewById(R.id.tv_difficulty);
        tv_difficulty.setText(itemText);
    }

    private void loadItems() {
        JsonHelper jsonHelper = new JsonHelper(this);
        items = jsonHelper.loadItemsFromJson(itemText, R.raw.assessment);

        if (items != null && !items.isEmpty()) {
            retrieveUserIndex();
        }
    }

    private void retrieveUserIndex() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        kidRef.child("assessment").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    userIndex = dataSnapshot.child(itemText.toLowerCase()).getValue(int.class);
                    String currentLevel = dataSnapshot.child("level").getValue(String.class);
                    if (currentLevel != null && currentLevel.equals(itemText.toLowerCase())) {
                        currentIndex = userIndex;
                        lastVisitedIndex = userIndex;
                        tv_level.setText("item " + (userIndex + 1));
                        updateUI();
                    } else {
                        currentIndex = 0;
                        lastVisitedIndex = 0;
                        tv_level.setText("item " + 1);
                        updateUI();
                    }
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private boolean handleMicButtonTouch(MotionEvent event) {
        zoomInButton(micButton);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (MicPermission.checkPermission(this)) {
                micButton.setPressed(true);
                isGameOver = false;
                startRecording();
            } else {
                MicPermission.requestPermission(this);
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (MicPermission.checkPermission(this)) {
                if(isGameOver) return false;
                micButton.setPressed(false);
                stopRecording();
                startTranscription(getFilePath(WaveUtil.RECORDING_FILE));
                progressLayout.setVisibility(View.VISIBLE);
            }
            return false;
        }
        return true;
    }

    private void processTranscription(String transcription) {
        String removePunctuation = transcription.replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").trim();
        // Log the transcription for debugging
        Log.d(TAG, "processTranscription: " + removePunctuation);

        // Check for specific invalid responses
        if (removePunctuation.equals("you") || removePunctuation.isEmpty() || removePunctuation.equals("Thank you")) {
            progressLayout.setVisibility(View.GONE);
            new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                    .setTitleText("Warning")
                    .setContentText("Please try again")
                    .show();
        } else {
            if (isGameOver) return;
            countDownTimer.cancel();

            // Check if the transcription matches the correct answer
            if (isCorrectTranscription(removePunctuation)) {
                // Show success message
                new StylishAlertDialog(this, StylishAlertDialog.SUCCESS)
                        .setTitleText("Correct!")
                        .setContentText("Good job! You got it right.")
                        .setConfirmText("Next")
                        .setConfirmClickListener(dialog -> dialog.dismiss())
                        .show();
                // Proceed with classification and upload for correct answers
                textClassifierHelper.classify(removePunctuation);
                uploadRecordingToFirebase();
            } else {
                new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                        .setTitleText("Oops! Try again.")
                        .setContentText("Keep going, you're almost there!")
                        .setConfirmText("OK")
                        .setConfirmClickListener(dialog -> dialog.dismiss())
                        .show();
                // Proceed with classification and upload for incorrect answers
                textClassifierHelper.classify(removePunctuation);
                uploadRecordingToFirebase();
            }
        }
    }

    // Helper method to check if the transcription is correct
    private boolean isCorrectTranscription(String transcription) {
        // Compare transcription with the expected correct answer from the current item
        String correctAnswer = items.get(currentIndex).name; // Assuming the correct answer is the name of the current item
        return transcription.equalsIgnoreCase(correctAnswer);
    }

    private void setupNavigationButtons() {

        findViewById(R.id.prevButton).setOnClickListener(v -> {
            zoomInButton(v);
            if (currentIndex > 0) {
                countDownTimer.cancel();
                currentIndex--;
                tv_level.setText("Item " + (currentIndex + 1));
                updateUI();
            }
        });

        nextButton.setOnClickListener(v -> {
            zoomInButton(v);
            if (currentIndex < items.size() - 1) {
                countDownTimer.cancel();
                currentIndex++;
                tv_level.setText("Item " + (currentIndex + 1));
                updateUI();
            } else if (currentIndex == items.size() - 1) {
                String nextLevel = getNextLevel(itemText);
                Intent intent = new Intent(this, Activity_Passed.class);
                intent.putExtra("Level", itemText);
                intent.putExtra("Activity", "Activity_BeginnerToAdvanced");
                intent.putExtra("nextLevel", nextLevel);
                startActivity(intent);
                finish();
            }
        });

        tv_level.setText("Item " + currentIndex + 1);
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
        countDownTimer.cancel();
        isGameOver = true;
        stopRecording();

        // Check if the activity is not finishing or destroyed
        if (!isFinishing()) {
            new StylishAlertDialog(this, StylishAlertDialog.WARNING)
                    .setTitleText("Time is up!")
                    .setContentText("Please try again.")
                    .setConfirmText("Restart")
                    .setConfirmButtonBackgroundColor(Color.RED)
                    .setConfirmClickListener(dialog -> {
                        dialog.dismiss();
                        currentIndex = 0;
                        lastVisitedIndex = 0;
                        tv_level.setText("Item " + (currentIndex + 1));
                        updateUI();
                    })
                    .setCancellable(false)
                    .show();
        }

        DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("assessment");
        // Update the user index to 0
        kidRef.child(itemText.toLowerCase()).setValue(0);
    }

    private void updateTimer() {
        int seconds = (int) (timeLeftInMillis / 1000);
        // Update the progress bar
        double progress = (double) seconds / 30;
        timerProgressBar.setProgress((int) (progress * 100));
    }

    private void setupSpeakerButton() {
        ImageButton speakerButton = findViewById(R.id.speakerButton);
        speakerButton.setOnClickListener(v -> {
            zoomInButton(v); // Apply zoom-in effect
            tv_itemname.speakAndAnimate(tv_itemname.getText().toString());
        });
    }

    private void setupCloseButton() {
        ImageButton closeButton = findViewById(R.id.ib_close);
        closeButton.setOnClickListener(v -> {
            zoomInButton(v); // Apply zoom-in effect
            finish(); // Close the activity
        });
    }

    private void updateUI() {
        JsonHelper.Item currentItem = items.get(currentIndex);

        // Enable the next button based on the current index
        nextButton.setEnabled(currentIndex < lastVisitedIndex);

        tv_itemname.setText(currentItem.name);
        tv_itemname.setInitialText(tv_itemname.getText().toString());
        tv_itemname.speakAndAnimate(currentItem.name);

        @SuppressLint("DiscouragedApi") int imageResId = getResources().getIdentifier(currentItem.image, "drawable", getPackageName());
        iv_itemImage.setImageResource(imageResId);
        timeLeftInMillis = 30000;
        startTimer();
    }

    private String getNextLevel(String text) {
        switch (text) {
            case "Beginner":
                return "Intermediate";
            case "Intermediate":
                return "Advanced";
            default:
                return "Beginner";
        }
    }

    // Copy assets to data folder
    private static void copyAssetsWithExtensionsToDataFolder(Context context, String[] extensions) {
        AssetManager assetManager = context.getAssets();
        try {
            // Specify the destination directory in the app's data folder
            String destFolder = context.getFilesDir().getAbsolutePath();

            for (String extension : extensions) {
                // List all files in the assets folder with the specified extension
                String[] assetFiles = assetManager.list("");
                for (String assetFileName : assetFiles) {
                    if (assetFileName.endsWith("." + extension)) {
                        File outFile = new File(destFolder, assetFileName);
                        if (outFile.exists())
                            continue;

                        InputStream inputStream = assetManager.open(assetFileName);
                        OutputStream outputStream = new FileOutputStream(outFile);

                        // Copy the file from assets to the data folder
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        inputStream.close();
                        outputStream.flush();
                        outputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Returns file path from data folder
    private String getFilePath(String assetName) {
        File outfile = new File(getFilesDir(), assetName);
        if (!outfile.exists()) {
            Log.d(TAG, "File not found - " + outfile.getAbsolutePath());
        }

        Log.d(TAG, "Returned asset path: " + outfile.getAbsolutePath());
        return outfile.getAbsolutePath();
    }

    private void startRecording() {

        String waveFilePath = getFilePath(WaveUtil.RECORDING_FILE);
        mRecorder.setFilePath(waveFilePath);
        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
    }

    // Transcription calls
    private void startTranscription(String waveFilePath) {
        mWhisper.setFilePath(waveFilePath);
        mWhisper.setAction(Whisper.ACTION_TRANSCRIBE);
        mWhisper.start();
    }

    private void uploadRecordingToFirebase() {
        String waveFilePath = getFilePath(WaveUtil.RECORDING_FILE);
        String filename = String.valueOf(currentIndex + 1);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String itemTextLower = itemText.toLowerCase();

        // Get a reference to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the file in storage
        StorageReference recordingRef = storageRef.child("users/" + uid + "/" + itemTextLower + "/" + filename + ".wav");

        // Upload the file
        recordingRef.putFile(Uri.fromFile(new File(waveFilePath)))
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    recordingRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String audioDownloadUrl = uri.toString();
                        saveRecordingUrlToRealtimeDatabase(uid, itemTextLower, currentIndex, audioDownloadUrl); // Updated method call
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL: ", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Upload failed: ", e);
                });
    }

    private void saveRecordingUrlToRealtimeDatabase(String uid, String itemTextLower, int index, String audioDownloadUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("recordings").child(itemTextLower);

        // Use setValue to create or update the recording entry at the specified index
        HashMap<String, Object> audioData = new HashMap<>();
        audioData.put("url", audioDownloadUrl);
        audioData.put("timestamp", System.currentTimeMillis());

        dbRef.child(String.valueOf(index)).setValue(audioData)
                .addOnSuccessListener(aVoid -> {
                    DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("assessment");
                    progressLayout.setVisibility(View.GONE);
                    if (currentIndex == 16) {
                        String nextLevel = getNextLevel(itemText);
                        kidRef.child("level").get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DataSnapshot dataSnapshot = task.getResult();
                                if (dataSnapshot.exists()) {
                                    String currentLevel = dataSnapshot.getValue(String.class);
                                    if (currentLevel.equals("beginner") || currentLevel.equals("intermediate")) {
                                        kidRef.child("level").setValue(nextLevel.toLowerCase());
                                    }
                                }
                            }
                        });

                        Intent intent = new Intent(this, Activity_Passed.class);
                        intent.putExtra("Level", itemText);
                        intent.putExtra("Activity", "Activity_BeginnerToAdvanced");
                        intent.putExtra("nextLevel", nextLevel);
                        startActivity(intent);
                        finish();
                    } else {
                        currentIndex++;
                        if (lastVisitedIndex < currentIndex) {
                            lastVisitedIndex = currentIndex;
                        }
                        if (lastVisitedIndex <= currentIndex) {
                            kidRef.child(itemText.toLowerCase()).setValue(currentIndex);
                        }
                        tv_level.setText("Item " + (currentIndex + 1));
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save recording URL: ", e);
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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
