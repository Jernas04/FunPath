package com.capstone.funpath.Adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.capstone.funpath.Models.AudioItem;
import com.capstone.funpath.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AudioList_Adapter extends RecyclerView.Adapter<AudioList_Adapter.AudioListViewHolder> {

    private final List<AudioItem> audioItems;
    private final Context context;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;
    private Handler handler;
    private Runnable stopAudioRunnable;

    public AudioList_Adapter(Context context, List<AudioItem> audioItems) {
        this.context = context;
        this.audioItems = audioItems;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public AudioList_Adapter.AudioListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_voice_record, parent, false);
        return new AudioList_Adapter.AudioListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioList_Adapter.AudioListViewHolder holder, int position) {
        AudioItem audioItem = audioItems.get(position);
        holder.bind(audioItem, position, this);
    }

    @Override
    public int getItemCount() {
        return audioItems.size();
    }

    public void playAudioFromFile(File audioFile, int position, AudioListViewHolder holder) {
        // Stop the currently playing audio if it's not the same item
        if (currentlyPlayingPosition != position && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            notifyItemChanged(currentlyPlayingPosition);
            cancelStopAudioTask(); // Cancel any existing stop audio task
        }

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        try {
            mediaPlayer.setDataSource(audioFile.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentlyPlayingPosition = position;
            holder.ibPlay.setSelected(true);

            mediaPlayer.setOnCompletionListener(mp -> {
                currentlyPlayingPosition = -1;
                holder.ibPlay.setSelected(false);
                mediaPlayer.release();
                mediaPlayer = null;
                cancelStopAudioTask(); // Cancel the stop audio task
            });

            // Stop audio after 10 seconds
            stopAudioRunnable = () -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    currentlyPlayingPosition = -1;
                    holder.ibPlay.setSelected(false);
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            };
            handler.postDelayed(stopAudioRunnable, 10000); // 10 seconds

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelStopAudioTask() {
        if (stopAudioRunnable != null) {
            handler.removeCallbacks(stopAudioRunnable);
            stopAudioRunnable = null;
        }
    }

    class AudioListViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAudioName;
        private final TextView tvTimeStamp;
        private final ImageButton ibPlay;

        public AudioListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAudioName = itemView.findViewById(R.id.textView29);
            tvTimeStamp = itemView.findViewById(R.id.textView25);
            ibPlay = itemView.findViewById(R.id.imageButton2);
        }

        public void bind(AudioItem audioItem, int position, AudioList_Adapter adapter) {
            tvAudioName.setText("Item " + (position + 1));
            tvTimeStamp.setText(String.valueOf(audioItem.getFormattedTimestamp()));

            ibPlay.setSelected(currentlyPlayingPosition == position);

            ibPlay.setOnClickListener(v -> {
                String audioUrl = audioItem.getUrl();
                File audioFile = getAudioCacheFile(audioUrl);

                // If the audio is already playing for this position, stop it
                if (currentlyPlayingPosition == position) {
                    mediaPlayer.stop();
                    currentlyPlayingPosition = -1;
                    ibPlay.setSelected(false);
                    mediaPlayer.release();
                    mediaPlayer = null;
                    cancelStopAudioTask(); // Cancel the stop audio task
                } else {
                    // Play the audio
                    if (audioFile.exists()) {
                        adapter.playAudioFromFile(audioFile, position, this);
                    } else {
                        // Download and cache, then play
                        downloadAndCacheAudio(audioUrl, audioFile, position, adapter);
                    }
                }
            });
        }

        private File getAudioCacheFile(String audioUrl) {
            String fileName = audioUrl.substring(audioUrl.lastIndexOf('/') + 1);
            return new File(context.getCacheDir(), fileName);
        }

        private void downloadAndCacheAudio(String audioUrl, File audioFile, int position, AudioList_Adapter adapter) {
            new Thread(() -> {
                try {
                    URL url = new URL(audioUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        try (InputStream input = connection.getInputStream();
                             FileOutputStream output = new FileOutputStream(audioFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = input.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }

                            // Play the downloaded audio on the main thread
                            new Handler(Looper.getMainLooper()).post(() -> adapter.playAudioFromFile(audioFile, position, this));
                        }
                    }
                } catch (Exception e) {
                    Log.e("AudioCache", "Error caching audio", e);
                }
            }).start();
        }

    }

    public void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentlyPlayingPosition = -1;
        notifyItemChanged(currentlyPlayingPosition);
        cancelStopAudioTask();
    }

}
