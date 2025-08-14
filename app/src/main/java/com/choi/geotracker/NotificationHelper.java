package com.choi.geotracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

/**
 * NOtification helper class for geofence
 */
public class NotificationHelper extends ContextWrapper {

    private static final String TAG = "NotificationHelper";
    PendingIntent pendingIntent;

    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    private final String CHANNEL_NAME = "High priority channel";
    private final String CHANNEL_ID = "com.choi.notifications" + CHANNEL_NAME;

    /**
     * Creates notification channel
     */
    private void createChannels() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription("this is the description of the channel.");
        //notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);
    }

    /**
     * Sends notifications
     * @param title
     * @param body
     * @param activityName
     */
    public void sendHighPriorityNotification(String title, String body, Class activityName) {

        Intent intent = new Intent(this, activityName);

        pendingIntent = PendingIntent.getActivity
                (this, 267, intent, PendingIntent.FLAG_MUTABLE);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(body)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().setSummaryText("Geofence Alert").setBigContentTitle(title).bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        NotificationManagerCompat.from(this).notify(new Random().nextInt(), notification);



    }

}
