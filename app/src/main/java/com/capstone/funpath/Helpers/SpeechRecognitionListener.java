package com.capstone.funpath.Helpers;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class SpeechRecognitionListener implements RecognitionListener {
    private final SpeechRecognizer speechRecognizer;
    private final Intent recognizerIntent;
    private StringBuilder partialTranscription;
    private Context context;

    public SpeechRecognitionListener(Context context) {
        this.context = context;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        recognizerIntent.putExtra("android.speech.extra.GET_AUDIO", true);

        partialTranscription = new StringBuilder();
    }

    public void startListening() {
        resetTranscription();
        speechRecognizer.startListening(recognizerIntent);
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {}


    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onRmsChanged(float rmsdB) {}

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(int error) {
        String errorMessage = getErrorMessage(error);
        Log.e(TAG, "Speech recognition error: " + errorMessage);
    }

    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service is busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "Speech timeout";
            default:
                return "Unknown error";
        }
    }


    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        StringBuilder currentTranscription = new StringBuilder();

        if (matches != null) {
            // Add the most confident result (index 0)
            if (!matches.isEmpty()) {
                currentTranscription.append(matches.get(0)).append(" "); // Append the best result
            }
            // Add the second result (index 1) if it exists
            if (matches.size() > 1) {
                currentTranscription.append(matches.get(1)).append(" "); // Append the second-best result
            }
        }

        partialTranscription.append(currentTranscription.toString().trim());
        Log.d(TAG, "onResults: " + results);
    }


    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null) {
            partialTranscription.append(matches.get(0));
            Log.d(TAG, "Partial results: " + matches.get(0));
        }
    }


    @Override
    public void onEvent(int eventType, Bundle params) {}

    public String getTranscription() {
        return partialTranscription.toString();
    }

    public void resetTranscription(){
        partialTranscription = new StringBuilder();
    }
}
