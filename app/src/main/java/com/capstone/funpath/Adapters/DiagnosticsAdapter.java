package com.capstone.funpath.Adapters;

import android.graphics.Typeface;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.funpath.R;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticsAdapter extends RecyclerView.Adapter<DiagnosticsAdapter.DiagnosticsViewHolder> {
    private List<DiagnosticsRecord> diagnosticEntries;

    // Constructor that accepts the list of DiagnosticsRecord
    public DiagnosticsAdapter(List<DiagnosticsRecord> diagnosticEntries) {
        this.diagnosticEntries = diagnosticEntries;
    }
    public interface OnNoteAddedListener {
        void onNoteAdded(String note);
    }

    // Update data in the adapter
    public void updateData(List<DiagnosticsRecord> newEntries) {
        this.diagnosticEntries.clear();
        this.diagnosticEntries.addAll(newEntries);
        notifyDataSetChanged();  // Notify the adapter that data has changed
    }

    @NonNull
    @Override
    public DiagnosticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for the diagnostic record
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diagnostic_record, parent, false);
        return new DiagnosticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiagnosticsViewHolder holder, int position) {
        DiagnosticsRecord entry = diagnosticEntries.get(position);

        // Create a SpannableString to apply bold formatting
        SpannableString dateText = new SpannableString("Date: " + entry.getDate());
        dateText.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // Bold "Date:"

        SpannableString notesText = new SpannableString("Note:\n" + entry.getNotes());
        notesText.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // Bold "Note:"

        // Set the formatted text to the TextViews
        holder.tvDate.setText(dateText);
        holder.tvNotes.setText(notesText);

        // Justify the text if API level is >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tvNotes.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        // Set the week as before
        holder.tvWeek.setText(entry.getWeek());

        // Log the data being bound
        Log.d("DiagnosticsAdapter", "Binding entry: " + entry.toString());
    }


    @Override
    public int getItemCount() {
        return diagnosticEntries.size();
    }

    // ViewHolder to hold the views for each item
    public static class DiagnosticsViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeek;
        TextView tvDate;
        TextView tvNotes;

        public DiagnosticsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeek = itemView.findViewById(R.id.tv_week);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvNotes = itemView.findViewById(R.id.tv_notes);
        }
    }
}



