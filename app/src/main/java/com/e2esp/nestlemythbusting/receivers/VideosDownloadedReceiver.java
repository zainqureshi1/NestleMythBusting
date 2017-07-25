package com.e2esp.nestlemythbusting.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.activities.WelcomeActivity;
import com.e2esp.nestlemythbusting.utils.Consts;

public class VideosDownloadedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {Bundle extras = intent.getExtras();
        if (extras != null) {
            int downloaded = extras.getInt(Consts.Extras.DOWNLOADED, 0);
            int total = extras.getInt(Consts.Extras.TOTAL, 0);
            showDownloadCompleteNotification(context, downloaded, total);
        }
    }

    private void showDownloadCompleteNotification(Context context, int downloaded, int total) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

        String title = context.getString(R.string.successfully_downloaded_videos, downloaded, total);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);

        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

}
