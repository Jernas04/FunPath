package com.capstone.funpath.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.funpath.Models.KidItem;
import com.capstone.funpath.R;

import java.util.List;

public class KidList_Adapter extends RecyclerView.Adapter<KidList_Adapter.KidListViewHolder> {

    private List<KidItem> kidProfiles;
    private int selectedPosition = -1; // Track the selected position

    public KidList_Adapter(List<KidItem> kidProfiles) {
        this.kidProfiles = kidProfiles;
    }

    @NonNull
    @Override
    public KidListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_of_kids, parent, false); // Replace with your CardView layout name
        return new KidListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KidListViewHolder holder, int position) {
        KidItem kidProfile = kidProfiles.get(position);
        holder.bind(kidProfile, position);
    }

    @Override
    public int getItemCount() {
        return kidProfiles.size();
    }

    class KidListViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;

        public KidListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_image);  // Change to your ImageView ID
            textView = itemView.findViewById(R.id.tv_name);    // Change to your TextView ID

            // Retrieve the selected position from SharedPreferences
            SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String selectedKidKey = sharedPreferences.getString("selectedKidKey", null);
            if(selectedKidKey == null) {
                selectedPosition = 0;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("selectedKidKey", getSelectedKey());
                editor.apply();
            }else {
                selectedPosition = getPositionKey(selectedKidKey); // Default to -1 if no value is found
            }

            // Update background color based on the selected position
            updateBackgroundColor();

            itemView.setOnClickListener(v -> {
                // Update the selected position
                int previousPosition = selectedPosition;
                selectedPosition = getAdapterPosition();

                // Save the selected position in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("selectedKidKey", getSelectedKey());
                editor.apply(); // Save changes

                // Refresh the previously selected and newly selected items
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);

                // Update background color based on the selected position
                updateBackgroundColor();
            });
        }

        public void bind(KidItem kidProfile, int position) {
            // Reset the image view to avoid incorrect image transfer

            // Load the new image
            if (!kidProfile.getAvatarResource().isEmpty()) {
                Glide.with(itemView.getContext()).load(kidProfile.getAvatarResource()).circleCrop().into(imageView);
            }else {
                imageView.setImageResource(R.drawable.vector_avatar_gamer_boy);
            }

            // Set the kid's name
            textView.setText(kidProfile.getName());

            // Update background color based on selection state
            updateBackgroundColor();
        }


        private void updateBackgroundColor() {
            if (getAdapterPosition() == selectedPosition) {
                itemView.setBackgroundColor(0xFFE0E0E0); // Light grey if selected
            } else {
                itemView.setBackgroundColor(0xFFFFFFFF); // White if not selected
            }
        }
    }

    // Optional: A method to get the selected item's key
    public String getSelectedKey() {
        if (selectedPosition != -1) {
            return kidProfiles.get(selectedPosition).getKey(); // Assuming getKey() returns the key
        }
        return null; // No item is selected
    }

    public int getPositionKey(String key) {
        for (int i = 0; i < kidProfiles.size(); i++) {
            if (kidProfiles.get(i).getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }
}
