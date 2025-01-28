package com.capstone.funpath.PlayAndLearn;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.funpath.Activity_Points;
import com.capstone.funpath.Adapters.PictureCard_Adapter;
import com.capstone.funpath.Helpers.UserHelper;
import com.capstone.funpath.R;
import com.marsad.stylishdialogs.StylishAlertDialog;

public class Activity_PictureCardsChoice extends AppCompatActivity {

    private final String[] cardItems = new String[] {
            "Aa", "Bb", "Cc", "Dd", "Ee", "Ff", "Gg", "Hh", "Ii", "Jj",
            "Kk", "Ll", "Mm", "Nn", "Oo", "Pp", "Qq", "Rr", "Ss", "Tt",
            "Uu", "Vv", "Ww", "Xx", "Yy", "Zz"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_picture_cards_choice);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton ib_close = findViewById(R.id.ib_close);
        GridView gridView = findViewById(R.id.grid_view);
        PictureCard_Adapter adapter = new PictureCard_Adapter(this, cardItems);
        gridView.setAdapter(adapter);

        // Apply zoom animation when ImageButton is clicked
        ib_close.setOnClickListener(v -> {
            zoomInButton(v);
            getOnBackPressedDispatcher().onBackPressed();
        });

        findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            if (isInternetAvailable()) {
                new UserHelper(helper -> {
                    if (helper != null) {
                        Intent intent = new Intent(Activity_PictureCardsChoice.this, Activity_Points.class);
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
                        .setTitleText("No Internet Connection")
                        .setContentText("Please check your internet connection and try again.")
                        .show();
            }
        });

        // Set touch listener to hold down zoom effect on ImageButton and Grid items
        setZoomEffectOnHold(ib_close);
        setZoomEffectOnHold(findViewById(R.id.ib_menu)); // Assuming there is an ib_menu in the layout

        // Apply zoom effect on individual items of the GridView
        gridView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
                int position = gridView.pointToPosition((int) event.getX(), (int) event.getY());
                View item = gridView.getChildAt(position - gridView.getFirstVisiblePosition());
                if (item != null) {
                    zoomInButton(item); // Zoom effect on individual GridView item
                }
            }
            return false;
        });
    }

    // Method to check internet connectivity
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Zoom-in animation for the button or view
    private void zoomInButton(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f, 1f); // Scale in X direction
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f, 1f); // Scale in Y direction
        scaleX.setDuration(200); // Duration of animation
        scaleY.setDuration(200); // Duration of animation
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
