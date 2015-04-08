package com.deston.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class DeviceUtil {

    public static float getPixelsFromDip(Context context, float dip) {
        return context.getResources().getDisplayMetrics().density * dip + 0.5f;
    }

    public static int getDeviceHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getDeviceWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
}
