package com.capstone.funpath.Manuals;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.R;

public class Activity_Manual extends AppCompatActivity {
    ImageButton ib_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manual);

        ib_back = findViewById(R.id.ib_back);

        ib_back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up toggle functionality
        setupToggle(R.id.section_parents_guide, R.id.content_parents_guide);
        setupToggle(R.id.section_games_instructions, R.id.content_games_instructions);
        setupToggle(R.id.section_tips_for_success, R.id.content_tips_for_success);
        setupToggle(R.id.section_terms_conditions, R.id.content_terms_conditions);
    }

    private void setupToggle(int sectionId, int contentId) {
        View sectionView = findViewById(sectionId);
        final TextView contentView = findViewById(contentId);

        sectionView.setOnClickListener(view -> {
            // Toggle the visibility of the content
            if (contentView.getVisibility() == View.GONE) {
                contentView.setVisibility(View.VISIBLE);
            } else {
                contentView.setVisibility(View.GONE);
            }
        });
    }
}
