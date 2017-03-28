package com.e2esp.nestlemythbusting.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.Toast;

import com.e2esp.nestlemythbusting.R;

/**
 * Created by Zain on 3/22/2017.
 */

public class Utility {

    public static boolean isInternetConnected(Context context, boolean showErrorToast) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!isConnected && showErrorToast) {
            showToast(context, R.string.connect_to_internet);
        }
        return isConnected;
    }

    public static int dpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static Typeface getArialFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "arial.ttf");
    }

    public static void showToast(Context context, int stringRes) {
        showToast(context, context.getString(stringRes));
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static String bytesString(long bytes) {
        if (bytes < 1024) return bytes + "B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        float value = (float)(bytes / Math.pow(1024, exp));
        String unit = "KMGT".charAt(exp-1)+"B";
        return String.format("%.1f", value)+unit;
    }

    public static class Prefs {

        public static void setPref(Context context, String key, boolean value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(key, value).commit();
        }

        public static void setPref(Context context, String key, int value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putInt(key, value).commit();
        }

        public static void setPref(Context context, String key, String value) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putString(key, value).commit();
        }

        public static boolean getPrefBool(Context context, String key, boolean def) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(key, def);
        }

        public static int getPrefInt(Context context, String key, int def) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt(key, def);
        }

        public static String getPrefString(Context context, String key, String def) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(key, def);
        }

    }

}
