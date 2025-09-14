package com.rituraj.bhandaraapp.locations;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public class PermissionManager {
    private static PermissionManager instance = null;
    private Context context;

    public static PermissionManager getInstance(Context context) {
        if (instance == null)
            instance = new PermissionManager();
        instance.init(context);
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public boolean checkPermissions(String[] permissions) {
        int size = permissions.length;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    public void askPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public boolean handlePermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0) {
            boolean areaAllPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
                    areaAllPermissionsGranted = false;
                    break;
                }
            }
            return areaAllPermissionsGranted;
        }
        return false;
    }
}
