package com.capstone.funpath.Helpers;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.capstone.funpath.R;

import java.util.Locale;

public class TTSAnimatedTextView extends androidx.appcompat.widget.AppCompatTextView {
    private TextToSpeech TTS;
    private int colorStart;
    private int colorEnd;
    private String initialText;

    public TTSAnimatedTextView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TTSAnimatedTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TTSAnimatedTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        TTS = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                TTS.setLanguage(Locale.US);
                TTS.setSpeechRate(0.7f);
                setTTSProgressListener();
                if (initialText != null) {
                    speakAndAnimate(initialText);
                    initialText = null;
                }
            }
        });

        colorStart = ContextCompat.getColor(context, R.color.medium_light_blue);
        colorEnd = ContextCompat.getColor(context, R.color.dark_brown);
    }

    private void setTTSProgressListener() {
        TTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                resetTextColor();
            }

            @Override
            public void onDone(String utteranceId) {
                // Optional: handle when TTS finishes
            }

            @Override
            public void onError(String utteranceId) {
                // Optional: handle errors
            }

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
                // On API 26 and higher, we get the exact range of the text being spoken
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    post(() -> updateTextColor(start, end));
                }
            }
        });
    }

    private void updateTextColor(int start, int end) {
        SpannableString spannableString = new SpannableString(getText());
        // Color the spoken word
        spannableString.setSpan(new ForegroundColorSpan(colorStart), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Color the remaining text
        spannableString.setSpan(new ForegroundColorSpan(colorEnd), end, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        setText(spannableString);
    }

    public void speakAndAnimate(String text) {
        invalidate();
        if (TTS != null && !TTS.getEngines().isEmpty()) {
            resetTextColor();
            TTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");  // Utterance ID
        } else {
            resetTextColor();
            initialText = text;
        }
    }

    private void resetTextColor() {
        getPaint().setShader(null);
        setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        invalidate();
    }

    public void dismiss() {
        if (TTS != null) {
            TTS.stop();
            TTS.shutdown();
        }
    }

    public void stop() {
        if (TTS != null && TTS.isSpeaking()) {
            TTS.stop();
        }
    }

    public void setInitialText(String string) {
        initialText = string;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (TTS != null) {
            dismiss();
        }
    }

}
