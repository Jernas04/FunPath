package com.capstone.funpath.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.funpath.Models.EmotionItem;
import com.capstone.funpath.R;

import java.util.List;

public class EmotionCheckIn_Adapter extends BaseAdapter {
    private final Context context;
    private final List<EmotionItem> items; // Create an EmotionItem class for the data

    public EmotionCheckIn_Adapter(Context context, List<EmotionItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.gridview_items_emotion, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageViewEmotion);
        TextView textView = convertView.findViewById(R.id.textViewEmotion);

        EmotionItem item = items.get(position);
        imageView.setImageResource(item.getImageResId());
        textView.setText(item.getText());

        return convertView;
    }
}