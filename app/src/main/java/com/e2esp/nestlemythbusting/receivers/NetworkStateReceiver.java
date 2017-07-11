package com.e2esp.nestlemythbusting.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.e2esp.nestlemythbusting.helpers.FAQHandler;

public class NetworkStateReceiver extends BroadcastReceiver {
    private final String TAG = "NetworkStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
            Log.d(TAG, "onReceive :: Connected");
            FAQHandler.getInstance(context).updateFAQ();
        } else {
            Log.d(TAG, "onReceive :: Not connected");
        }
    }

}
