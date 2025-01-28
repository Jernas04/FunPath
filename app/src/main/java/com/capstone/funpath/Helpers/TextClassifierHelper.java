package com.capstone.funpath.Helpers;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.text.textclassifier.TextClassifier;
import com.google.mediapipe.tasks.text.textclassifier.TextClassifierResult;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TextClassifierHelper {
    private static final String TAG = "TextClassifierHelper";
    private static final String WORD_VEC = "wordvec.tflite";
    private static final String MOBILEBERT = "mobilebert.tflite";

    private String currentModel;
    private Context context;
    private TextResultsListener listener;
    private TextClassifier textClassifier;
    private ScheduledExecutorService executor;

    public TextClassifierHelper(String currentModel, Context context, TextResultsListener listener) {
        this.currentModel = currentModel;
        this.context = context;
        this.listener = listener;
        initClassifier();
    }

    private void initClassifier() {
        BaseOptions baseOptions = BaseOptions.builder()
                .setModelAssetPath(currentModel)
                .build();

        try {
            TextClassifier.TextClassifierOptions options = TextClassifier.TextClassifierOptions.builder()
                    .setBaseOptions(baseOptions)
                    .build();
            textClassifier = TextClassifier.createFromOptions(context, options);
        } catch (IllegalStateException e) {
            listener.onError("Text classifier failed to initialize. See error logs for details");
            Log.e(TAG, "Text classifier failed to load the task with error: " + e.getMessage());
        }
    }

    // Run text classification using MediaPipe Text Classifier API
    public void classify(final String text) {
        executor = Executors.newSingleThreadScheduledExecutor();

        executor.execute(() -> {
            // inferenceTime is the amount of time, in milliseconds, that it takes to classify the input text.
            long inferenceTime = SystemClock.uptimeMillis();

            TextClassifierResult results = textClassifier.classify(text);

            inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

            listener.onResult(results, inferenceTime);
        });
    }

    public interface TextResultsListener {
        void onError(String error);
        void onResult(TextClassifierResult results, long inferenceTime);
    }
}
