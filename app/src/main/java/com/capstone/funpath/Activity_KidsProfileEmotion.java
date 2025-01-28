package com.capstone.funpath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.capstone.funpath.Models.EmotionItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Activity_KidsProfileEmotion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_kids_profile_emotion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView iv_first_result = findViewById(R.id.iv_first_result);
        ImageView iv_second_result = findViewById(R.id.iv_second_result);
        ImageView iv_third_result = findViewById(R.id.iv_third_result);
        TextView tv_first_result = findViewById(R.id.tv_first_result);
        TextView tv_second_result = findViewById(R.id.tv_second_result);
        TextView tv_third_result = findViewById(R.id.tv_third_result);
        ShapeableImageView iv_profile_iamge = findViewById(R.id.iv_profile_image);
        TextView tv_profile_name = findViewById(R.id.tv_profile_name);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String selectedKidKey = sharedPreferences.getString("selectedKidKey", null);

        if (selectedKidKey != null) {
            DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(selectedKidKey);
            kidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve data from the snapshot
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String imageURL = dataSnapshot.child("imageURL").getValue(String.class);
                        // Create a GenericTypeIndicator for the specific list type
                        GenericTypeIndicator<List<EmotionItem>> indicator = new GenericTypeIndicator<List<EmotionItem>>() {};
                        // Retrieve the list using the indicator
                        List<EmotionItem> emotions = dataSnapshot.child("emotions").getValue(indicator);

                        if(emotions == null) {
                            return;
                        }
                        tv_profile_name.setText(name);
                        if(!imageURL.isEmpty()){
                            Glide.with(Activity_KidsProfileEmotion.this).load(imageURL).into(iv_profile_iamge);
                        }
                        String firstEmotion = emotions.get(0).getText();
                        String secondEmotion = emotions.get(1).getText();
                        String thirdEmotion = emotions.get(2).getText();
                        tv_first_result.setText(firstEmotion);
                        tv_second_result.setText(secondEmotion);
                        tv_third_result.setText(thirdEmotion);
                        iv_first_result.setImageResource(getImageRes(firstEmotion));
                        iv_second_result.setImageResource(getImageRes(secondEmotion));
                        iv_third_result.setImageResource(getImageRes(thirdEmotion));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }


            });
        }

        findViewById(R.id.ib_back).setOnClickListener(v -> finish());

    }

    private int getImageRes(String text) {
        switch (text) {
            case "Sad":
                return R.drawable.vector_emoji_sad;
            case "Happy":
                return R.drawable.vector_emoji_happy;
            case "HAPPY":
                return R.drawable.vector_emotion_happy;
            case "Angry":
                return R.drawable.vector_emoji_angry;
            case "Scared":
                return R.drawable.vector_emoji_sacred;
            case "Silly":
                return R.drawable.vector_emoji_silly;
            case "Surprised":
                return R.drawable.vector_emoji_surprised;
            case "SURPRISED":
                return R.drawable.vector_emotion_surprised;
            case "Tired":
                return R.drawable.vector_emotion_tired;
            case "Worried":
                return R.drawable.vector_emotion_worried;
            case "Shy":
                return R.drawable.vector_emotion_shy;
            case "Sick":
                return R.drawable.vector_emotion_sick;
            case "Excited":
                return R.drawable.vector_emotion_excited;
            case "Mad":
                return R.drawable.vector_emotion_mad;
            case "Very easy!":
                return R.drawable.vector_emotion_very_easy;
            case "I find it hard sometimes":
                return R.drawable.vector_emotion_i_did_it_hard_sometimes;
            case "Very Hard":
                return R.drawable.vector_emotion_very_hard;
            default:
                return R.drawable.vector_emoji_happy;
        }
    }
}