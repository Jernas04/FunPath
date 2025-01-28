package com.capstone.funpath.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class EmotionItem implements Parcelable {
    private int imageResId;
    private String text;

    public EmotionItem() {
    }

    public EmotionItem(int imageResId, String text) {
        this.imageResId = imageResId;
        this.text = text;
    }

    protected EmotionItem(Parcel in) {
        imageResId = in.readInt();
        text = in.readString();
    }

    public static final Creator<EmotionItem> CREATOR = new Creator<EmotionItem>() {
        @Override
        public EmotionItem createFromParcel(Parcel in) {
            return new EmotionItem(in);
        }

        @Override
        public EmotionItem[] newArray(int size) {
            return new EmotionItem[size];
        }
    };

    public int getImageResId() {
        return imageResId;
    }

    public String getText() {
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageResId);
        dest.writeString(text);
    }
}
