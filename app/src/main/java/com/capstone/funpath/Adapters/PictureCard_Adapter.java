package com.capstone.funpath.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.capstone.funpath.PlayAndLearn.Activity_PictureCards;
import com.capstone.funpath.R;

public class PictureCard_Adapter extends BaseAdapter {

    private final Context context;
    private final String[] items; // This will be the data source (e.g., card names)

    public PictureCard_Adapter(Context context, String[] items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.gridview_items_picture_cards, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.text);
        // Set the text to be the letter sequence
        textView.setText(items[position]);

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Activity_PictureCards.class);
            intent.putExtra("Letter", textView.getText());
            context.startActivity(intent);
        });

        return convertView;
    }
}
