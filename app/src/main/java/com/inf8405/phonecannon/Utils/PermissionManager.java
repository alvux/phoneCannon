package com.inf8405.phonecannon.Utils;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionManager {

    public static boolean checkAllPermissions(Activity activity){
        if (checkPermission(Manifest.permission.ACCESS_WIFI_STATE, activity)&&
                checkPermission(Manifest.permission.CHANGE_WIFI_STATE, activity)&&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, activity)&&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, activity)){
            return true;
        }
        return false;
    }

    public static boolean checkPermission(String perm, Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, perm)
                == activity.getPackageManager().PERMISSION_GRANTED) {
            // Permission is granted
            return true;
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(activity, new String[]{perm}, 1);
            return false;
        }
    }
}
