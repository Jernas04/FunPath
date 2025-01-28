package com.capstone.funpath.Permissions;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MicPermission {

    private static final int REQUEST_MIC_PERMISSION = 100;

    public static boolean checkPermission(Activity activity) {
        // Check if the microphone permission is granted
        int permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        // Request microphone permission
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_MIC_PERMISSION);
    }
}
