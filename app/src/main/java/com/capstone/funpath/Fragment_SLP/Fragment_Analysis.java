package com.capstone.funpath.Fragment_SLP;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.capstone.funpath.Manuals.Activity_Manual_SLP;
import com.capstone.funpath.Helpers.StutteringClassificationHelper;
import com.capstone.funpath.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Analysis#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Analysis extends Fragment {

    // Parameter arguments
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Parameters
    private String mParam1;
    private String mParam2;

    public Fragment_Analysis() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Analysis.
     */
    public static Fragment_Analysis newInstance(String param1, String param2) {
        Fragment_Analysis fragment = new Fragment_Analysis();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    // LineChart instance
    private LineChart lineChart;
    private TextView tv_cutoff_score, tv_weighted_sld_score, tv_percentage, tv_date_range;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve parameters if any
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__analysis, container, false);

        // Check for internet connection
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return view; // Return early if no internet
        }

        // Handle window insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_cutoff_score = view.findViewById(R.id.tv_cutoff_score);
        tv_weighted_sld_score = view.findViewById(R.id.tv_weighted_sld_score);
        tv_percentage = view.findViewById(R.id.tv_percentage);
        tv_date_range = view.findViewById(R.id.tv_date_range);

        // Initialize back button
        ImageButton ib_back = view.findViewById(R.id.ib_back);
        ib_back.setOnClickListener(v -> {
            zoomInButton(v);
            requireActivity().finish();
        });

        // Initialize LineChart
        lineChart = view.findViewById(R.id.lineChart);

        // Retrieve the selected kid's key from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String selectedKidKey = sharedPreferences.getString("selectedKidKey", null);

        if (selectedKidKey != null) {
            // Reference to the specific kid's chart data
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("charts")
                    .child(selectedKidKey);

            // Retrieve data from Firebase
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Entry> entries = new ArrayList<>();
                    List<WeekData> weekDataList = new ArrayList<>();
                    List<Entry> rawEntries = new ArrayList<>();

                    if (dataSnapshot.exists()) {
                        // Use TreeMap to sort weeks chronologically
                        TreeMap<Long, List<Integer>> weekScoresMap = new TreeMap<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                            Integer score = snapshot.child("score").getValue(Integer.class);
                            Log.d("DataCheck", "Timestamp: " + timestamp + ", Score: " + score);

                            if (timestamp == null || score == null) {
                                Log.e("DataError", "Null timestamp or score in snapshot: " + snapshot.getKey());
                                continue;  // Skip invalid data
                            }

                            // Get the start of the week (Monday)
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(timestamp);
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                            // Reset time to start of day
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            long weekStart = calendar.getTimeInMillis();

                            // Initialize list for the week if it doesn't exist
                            if (!weekScoresMap.containsKey(weekStart)) {
                                weekScoresMap.put(weekStart, new ArrayList<>());
                            }

                            // Add the score to the corresponding week
                            weekScoresMap.get(weekStart).add(score);
                            // Add raw data for score
                            rawEntries.add(new Entry(weekScoresMap.size(), score));
                        }

                        // Assign week numbers and calculate averages
                        int weekNumber = 1;
                        for (Map.Entry<Long, List<Integer>> entry : weekScoresMap.entrySet()) {
                            long weekStart = entry.getKey();
                            List<Integer> scores = entry.getValue();

                            // Calculate average
                            double averageScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0);

                            // Add to entries with x-value as week number
                            entries.add(new Entry(weekNumber, (float) averageScore));

                            // Store week data for date range retrieval
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(weekStart);
                            String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

                            // Calculate end of the week (Sunday)
                            calendar.add(Calendar.DAY_OF_WEEK, 6);
                            long weekEndTimestamp = calendar.getTimeInMillis();
                            String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(weekEndTimestamp));

                            weekDataList.add(new WeekData(weekNumber, startDate, endDate));

                            weekNumber++;
                        }

                        if (!entries.isEmpty()) {
                            createChart(entries, weekDataList, rawEntries, view);
                        } else {
                            Toast.makeText(getContext(), "No valid data available for the selected kid.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No data found for the selected kid.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle potential errors
                    Log.e("FirebaseError", "Database error: " + error.getMessage());
                    Toast.makeText(getContext(), "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
                }
            });

            DatabaseReference kidRef = FirebaseDatabase.getInstance().getReference("users").child(selectedKidKey);
            kidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String kidName = snapshot.child("name").getValue(String.class);
                        String imageURL = snapshot.child("imageURL").getValue(String.class);
                        TextView tv_kid_name = view.findViewById(R.id.tv_profile_name);
                        tv_kid_name.setText(kidName);

                        if(!imageURL.isEmpty()){
                            ShapeableImageView iv_profile_image = view.findViewById(R.id.iv_profile_image);
                            Glide.with(getContext()).load(imageURL).into(iv_profile_image);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle potential errors
                    Log.e("FirebaseError", "Database error: " + error.getMessage());
                    Toast.makeText(getContext(), "Failed to retrieve kid data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "No kid selected.", Toast.LENGTH_SHORT).show();
        }

        view.findViewById(R.id.ib_menu).setOnClickListener(v -> {
            zoomInButton(v);
            Intent intent = new Intent(requireContext(), Activity_Manual_SLP.class);
            startActivity(intent);
        });
        view.findViewById(R.id.btn_legend).setOnClickListener(v->{
            zoomInButton(v);
            view.findViewById(R.id.cl_weighted_sld_score_legend).setVisibility(View.VISIBLE);
        });
        return view;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        requireActivity().finish(); // Optionally finish the activity
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void createChart(List<Entry> entries, List<WeekData> weekDataList, List<Entry> rawEntries, View view) {
        // Create dataset with cubic lines
        LineDataSet dataSet = new LineDataSet(entries, "Per Week");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Set cubic bezier for smooth curved lines
        dataSet.setDrawFilled(true); // Fill the area under the line
        dataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.emerald_green));
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.emerald_green)); // Line color
        dataSet.setValueTextColor(Color.BLACK); // Value text color
        dataSet.setLineWidth(2f); // Thickness of the line
        dataSet.setCircleRadius(4f); // Radius of the data point circles
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.emerald_green)); // Color of the data point circles
        dataSet.setDrawValues(false); // Disable value text for better performance

        // Dataset for raw scores
        LineDataSet rawDataSet = new LineDataSet(rawEntries, "Per Item");
        rawDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        rawDataSet.setDrawFilled(true);
        rawDataSet.setColor(ContextCompat.getColor(getContext(), R.color.light_blue));
        rawDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.light_blue));
        dataSet.setValueTextColor(Color.BLACK); // Value text color
        rawDataSet.setLineWidth(2f);
        rawDataSet.setCircleRadius(4f);
        rawDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.light_blue));
        rawDataSet.setDrawValues(false);

        // Create LineData object
        LineData lineData = new LineData(dataSet, rawDataSet);

        // Set data to the chart
        lineChart.setData(lineData);

        // Customize X-axis to show week numbers
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // One week interval
        xAxis.setLabelCount(weekDataList.size(), true); // Set label count to number of weeks
        xAxis.setValueFormatter(new WeekValueFormatter(weekDataList)); // Formatter to display week labels

        // Customize Y-axis
        lineChart.getAxisRight().setEnabled(false); // Disable right Y-axis
        lineChart.getDescription().setEnabled(false); // Disable chart description
        lineChart.getLegend().setEnabled(true); // Disable legend

        // Add animations for a smooth transition
        lineChart.animateXY(1000, 1000);

        // Handle click listener for toggling legends dynamically
        view.findViewById(R.id.switch_feature).setOnClickListener(v -> {
            if (dataSet.isVisible()) {
                // Switch legend to "Raw Scores"
                dataSet.setVisible(false);
                rawDataSet.setVisible(true);
            } else {
                // Switch back to "Average Scores"
                rawDataSet.setVisible(false);
                dataSet.setVisible(true);
            }
            // Refresh the chart

            lineChart.invalidate();
        });

        // Refresh the chart
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        // Initialize TextViews for the first week
        if (!weekDataList.isEmpty() && !entries.isEmpty()) {
            // Assuming the first week is represented by index 0
            WeekData firstWeekData = weekDataList.get(0); // Get the first week
            Entry firstEntry = entries.get(0); // Get the corresponding entry for the first week

            // Set initial values in TextViews
            String initialStutterLevel = StutteringClassificationHelper.getLabelByScore(firstEntry.getY());
            String initialPercentage = String.format(Locale.getDefault(), "%.2f%%",
                    StutteringClassificationHelper.getPercentageOfFinalScore(initialStutterLevel, firstEntry.getY()));

            tv_cutoff_score.setText(initialStutterLevel);
            tv_weighted_sld_score.setText(String.format(Locale.getDefault(), "%.2f", firstEntry.getY()));
            tv_percentage.setText(initialPercentage);
            tv_date_range.setText(String.valueOf(firstWeekData.getWeekNumber())); // Assuming week numbers start from 1
        }

        // Set listener for value selection
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                view.findViewById(R.id.cl_weighted_sld_score_legend).setVisibility(View.GONE);
                float xValue = e.getX();
                float yValue = e.getY();

//                 Find the corresponding week data
                for (WeekData weekData : weekDataList) {
                    if (weekData.getWeekNumber() == (int) xValue) {
                        // Display the date range and score
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        tv_date_range.setText(weekData.getStartDate());
//                        Toast.makeText(getContext(),
//                                "Week " + weekData.getWeekNumber() + ": " + weekData.getStartDate() + " to " + weekData.getEndDate() +
//                                        "\nAverage Score: " + yValue,
//                                Toast.LENGTH_LONG).show();
                        break;
                    }
                }

                // Get the stutter level and percentage
                String stutterLevel = StutteringClassificationHelper.getLabelByScore(yValue);
                String percentage = String.format(Locale.getDefault(), "%.2f%%",
                        StutteringClassificationHelper.getPercentageOfFinalScore(stutterLevel, yValue));

                // Update TextViews based on the selected week
                tv_cutoff_score.setText(stutterLevel);
                tv_weighted_sld_score.setText(String.format(Locale.getDefault(), "%.2f", yValue));
                tv_percentage.setText(percentage);
//                tv_date_range.setText(String.valueOf((int) xValue)); // Show week number
            }

            @Override
            public void onNothingSelected() {
                // Handle case where no value is selected, if needed
            }
        });

        setZoomEffectOnHold(view.findViewById(R.id.ib_menu));
        setZoomEffectOnHold(view.findViewById(R.id.speakerButton));
        setZoomEffectOnHold(view.findViewById(R.id.ib_close));
        setZoomEffectOnHold(view.findViewById(R.id.btn_legend));
    }

    // Zoom-in animation for the button
    private void zoomInButton(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f); // Scale in X direction
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f); // Scale in Y direction
        scaleX.setDuration(100); // Duration of animation
        scaleY.setDuration(100); // Duration of animation
        scaleX.start();
        scaleY.start();
    }

    // Add zoom effect on touch
    private void setZoomEffectOnHold(View view) {
        if (view != null) {
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

    // Custom ValueFormatter to display week numbers on X-axis
    private class WeekValueFormatter extends ValueFormatter {
        private final List<WeekData> weekDataList;

        public WeekValueFormatter(List<WeekData> weekDataList) {
            this.weekDataList = weekDataList;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int weekNumber = (int) value;
            for (WeekData weekData : weekDataList) {
                if (weekData.getWeekNumber() == weekNumber) {
                    return "Week " + weekNumber;
                }
            }
            return "";
        }
    }

    // Helper class to store week data
    private class WeekData {
        private final int weekNumber;
        private final String startDate;
        private final String endDate;

        public WeekData(int weekNumber, String startDate, String endDate) {
            this.weekNumber = weekNumber;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getWeekNumber() {
            return weekNumber;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }
}
