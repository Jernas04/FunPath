package com.capstone.funpath.Helpers;

import android.content.Context;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {
    private MediaRecorder recorder;
    private MediaMuxer muxer;
    private String fileName;
    private Context context;
    private int trackIndex;
    private boolean isMuxerStarted;

    public AudioRecorder(Context context) {
        this.context = context;
        File audioFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recorded_audio.mp4");
        fileName = audioFile.getAbsolutePath();
        trackIndex = -1;
        isMuxerStarted = false;
    }

    public void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();

            // Setup MediaMuxer
            muxer = new MediaMuxer(fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
            trackIndex = muxer.addTrack(format);
            muxer.start();
            isMuxerStarted = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        if (isMuxerStarted) {
            muxer.stop();
            muxer.release();
            muxer = null;
            isMuxerStarted = false;
        }
    }

    public String getFileName() {
        return fileName;
    }
}
