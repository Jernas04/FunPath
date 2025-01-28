package com.capstone.funpath.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.airbnb.lottie.LottieAnimationView;
import com.capstone.funpath.R;

public class OnBoarding_Adapter extends PagerAdapter {
    Context context;
    int[] sliderAllImages = {
            R.raw.lottie_first_onboard,
            R.raw.lottie_second_onboard,
            R.raw.lottie_third_onboard,
            R.raw.lottie_fourth_onboard
    };
    int[] sliderAllTitle = {
            R.string.first_slide_title,
            R.string.second_slide_title,
            R.string.third_slide_title,
            R.string.fourth_slide_title
    };
    int[] sliderAllDesc = {
            R.string.first_slide_desc,
            R.string.second_slide_desc,
            R.string.third_slide_desc,
            R.string.fourth_slide_desc
    };

    public OnBoarding_Adapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return sliderAllTitle.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.onboarding_slider_layout, container, false);

        LottieAnimationView sliderLottie = view.findViewById(R.id.lottie_layer_name);
        TextView sliderTitle = view.findViewById(R.id.tv_title);
        TextView sliderDesc = view.findViewById(R.id.tv_desc);

        sliderLottie.setAnimation(sliderAllImages[position]);
        sliderTitle.setText(this.sliderAllTitle[position]);
        sliderDesc.setText(this.sliderAllDesc[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}
