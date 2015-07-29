package com.memorease.memorease;

import android.app.Notification;
import android.app.NotificationManager;
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

        final NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(context);
        if (intent.getBooleanExtra("multiple_notifications", true)) {
            final Intent notIntent = new Intent (context, MemoreaListActivity.class);
            final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(contentIntent)
                    .setContentTitle("Memorease")
                    .setContentText("Muliple memorizations ready")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true);
        } else {
            final Intent notIntent = new Intent (context, MemorizeScreenActivity.class);
            notIntent.putExtras(intent);
            final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(contentIntent)
                    .setContentTitle("Memorease")
                    .setContentText(title + " memorea is ready for memorization")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true);
        }

        manager.notify(0, builder.build());
    }
}
