package com.memorease.memorease;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for when the memorea memorization is ready<br>
 * Requires an Extra boolean multiple_notifications<br>
 * If multiple_notifications is false, also requires String title, String question, String answer, String hint, and String id
 */
public class AlarmReceiver extends BroadcastReceiver {
    /**
     * Creates a notification for either a single or multiple memoreas
     * @param context MemoreaListActivity activity
     * @param intent requires a boolean extra with the name multiple_notifications<br>
     *               if multiple_notifications is false, also requires String title, String question, String answer, String hint, and String id
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String title = intent.getStringExtra("title");

        final NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(context);
        final Intent alarmIntent;
        final String notificationTitle = "Memorease";
        final String notificationText;
        if (intent.getBooleanExtra("multiple_notifications", true)) {
            alarmIntent = new Intent (context, MemoreaListActivity.class);
            notificationText = "Multiple memorizations ready";
        } else {
            alarmIntent = new Intent (context, MemorizeScreenActivity.class);
            alarmIntent.putExtras(intent);
            notificationText = String.format("%s memorea is ready for memorization", title);
        }

        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);
        manager.notify(0, builder.build());
    }
}
