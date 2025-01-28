package com.capstone.funpath.Models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioItem {
    private String url;
    private Long timestamp;

    public AudioItem(String url, Long timestamp) {
        this.url = url;
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    // Method to get formatted timestamp
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.ENGLISH);
        return sdf.format(new Date(timestamp));
    }
}
