package com.capstone.funpath.Helpers;


import android.util.Log;

public class StutteringClassificationHelper {

    // Method to compute the range based on the classification label and score
    public static double computeStutteringScore(String label, double score) {
        double finalScore = 0;

        // Determine the final score based on the label and its range
        switch (label) {
            case "Not Observe":
                finalScore = 3.99 - (score * 3.99); // Scale the score to the range of 0-3.99
                break;
            case "Mild Stuttering":
                finalScore = 3.99 + (score * 5.99); // Scale to the range of 4-9.99
                break;
            case "Moderate Stuttering":
                finalScore = 9.99 + (score * 19.01); // Scale to the range of 10-29.00
                break;
            case "Significant Stuttering":
                finalScore = 29.00 + (score * 100); // Assuming a large scale for significant stuttering
                break;
            default:
                throw new IllegalArgumentException("Invalid label: " + label);
        }

        return finalScore; // Return the final score
    }

    // Method to get the label based on the final score
    public static String getLabelByScore(double finalScore) {
        if (finalScore >= 0 && finalScore <= 3.99) {
            return "Not Observe";
        } else if (finalScore >= 4 && finalScore <= 9.99) {
            return "Mild Stuttering";
        } else if (finalScore >= 10 && finalScore <= 29.00) {
            return "Moderate Stuttering";
        } else if (finalScore > 29.00) {
            return "Significant Stuttering";
        } else {
            throw new IllegalArgumentException("Invalid final score: " + finalScore);
        }
    }

    public static double getPercentageOfFinalScore(String label, double finalScore) {
        // Log the label to monitor which label is being processed
        Log.d("StutteringLabel", "Selected label: " + label);

        // Validate the label before proceeding
        if (!isValidLabel(label)) {
            // Handle the invalid label case, log a message, or throw an exception
            throw new IllegalArgumentException("Invalid label: " + label);
        }
        double minScore = 0;
        double maxScore = 0;
        // Trim any spaces from the label to avoid issues with unexpected spaces
        //label = label.trim();
        // Determine the range based on the label
        switch (label) {
            case "Not Observe": // Fixed the extra space
                maxScore = 3.99;
                break;
            case "Mild Stuttering":
                minScore = 4;
                maxScore = 9.99;
                break;
            case "Moderate Stuttering":
                minScore = 10;
                maxScore = 29.00;
                break;
            case "Significant Stuttering":
                minScore = 30; // Starting point for significant stuttering
                maxScore = 130; // Arbitrary high value for scaling
                break;
        }

        // Check if the finalScore is within the valid range
        if (finalScore < minScore || finalScore > maxScore) {
            throw new IllegalArgumentException("Final score out of range for label: " + label);
        }

        // Calculate percentage of finalScore within the range
        double percentage = (finalScore - minScore) / (maxScore - minScore) * 100;

        return percentage; // Return the percentage
    }

    // Helper method to validate the label
    private static boolean isValidLabel(String label) {
        return label.equals("Not Observe") ||
                label.equals("Mild Stuttering") ||
                label.equals("Moderate Stuttering") ||
                label.equals("Significant Stuttering");
    }

}
