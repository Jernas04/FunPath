package com.capstone.funpath;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.capstone.funpath.Adapters.OnBoarding_Adapter;

public class Activity_OnBoarding extends AppCompatActivity {
    // Declare your SharedPreferences key
    private static final String PREFS_NAME = "MyPrefs";
    private static final String ONBOARDING_COMPLETED_KEY = "onboardingCompleted";
    // Variable Declarations
    ViewPager slideViewPager;
    LinearLayout dotIndicator;
    Button btn_skip;
    TextView[] dots;
    TextView tv_back, tv_next;
    OnBoarding_Adapter onBoardingAdapter;
    ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onPageSelected(int position) {
            setDotIndicator(position);
            if (position > 0) {
                btn_skip.setVisibility(View.VISIBLE);
            } else {
                tv_back.setVisibility(View.INVISIBLE);
            }
            if (position == 3) {
                tv_next.setText("Finish");
                btn_skip.setVisibility(View.INVISIBLE);
            } else {
                tv_next.setText("Next");
                tv_back.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);

        tv_back = findViewById(R.id.tv_back);
        tv_next = findViewById(R.id.tv_next);
        btn_skip = findViewById(R.id.btn_skip);

        if (isOnboardingCompleted()) {
            navigateToLogInActivity();
            return;
        }

        tv_back.setOnClickListener(v -> {
            if (getItem(0) > 0) {
                slideViewPager.setCurrentItem(getItem(-1), true);
            }
        });

        tv_next.setOnClickListener(v -> {
            if (getItem(0) < 3)
                slideViewPager.setCurrentItem(getItem(1), true);
            else {
                navigateToLogInActivity();
                setOnboardingCompleted();
                finish();
            }
        });

        btn_skip.setOnClickListener(v -> {
            navigateToLogInActivity();
            setOnboardingCompleted();
            finish();
        });

        slideViewPager = findViewById(R.id.slideViewPager);
        dotIndicator = findViewById(R.id.dotIndicator);
        onBoardingAdapter = new OnBoarding_Adapter(this);
        slideViewPager.setAdapter(onBoardingAdapter);
        setDotIndicator(0);
        slideViewPager.addOnPageChangeListener(viewPagerListener);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void setDotIndicator(int position) {
        dots = new TextView[4];
        dotIndicator.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226", Html.FROM_HTML_MODE_LEGACY));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.light_gray, getApplicationContext().getTheme()));
            dotIndicator.addView(dots[i]);
        }
        dots[position].setTextColor(getResources().getColor(R.color.emerald_green, getApplicationContext().getTheme()));
    }

    // Method to check if onboarding has been completed before
    private boolean isOnboardingCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(ONBOARDING_COMPLETED_KEY, false);
    }

    // Method to mark onboarding as completed
    private void setOnboardingCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ONBOARDING_COMPLETED_KEY, true);
        editor.apply();
    }

    private int getItem(int i) {
        return slideViewPager.getCurrentItem() + i;
    }

    private void navigateToLogInActivity() {
        Intent i = new Intent(getApplicationContext(), Activity_SplashScreen.class);
        startActivity(i);
        finish();
    }
}