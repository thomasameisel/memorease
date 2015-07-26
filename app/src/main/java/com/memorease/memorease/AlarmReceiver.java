package com.memorease.memorease;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Tommy on 7/26/2015.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String title = intent.getStringExtra("title");

        Intent notIntent = new Intent (context, MemorizeScreenActivity.class);
        notIntent.putExtras(intent);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        //Generate a notification with just short text and small icon
        NotificationCompat.Builder builder = new NotificationCompat.Builder (context)
                .setContentIntent(contentIntent)
                .setContentTitle("New memorization ready!")
                .setContentText(title + " memorea is ready for memorization")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        Notification notification = builder.build();
        manager.notify((int)Calendar.getInstance().getTimeInMillis(), notification);
    }
}
