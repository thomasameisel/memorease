package com.tarian.memorease;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for when the memorea memorization is ready<br>
 * Requires an Extra boolean multiple_notifications<br>
 * If multiple_notifications is false, also requires String mTitle, String mQuestion, String mAnswer, String mHint, and String mId
 */
public class AlarmReceiver extends BroadcastReceiver {
    /**
     * Creates a notification for either a single or multiple memoreas
     * @param context MemoreaListActivity activity
     * @param intent requires a boolean extra with the name multiple_notifications<br>
     *               if multiple_notifications is false, also requires String mTitle, String mQuestion, String mAnswer, String mHint, and String mId
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Intent memoreaIntent = new Intent(MemoreaListActivity.NOTIFICATION_READY);
        context.sendBroadcast(memoreaIntent);

        final NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(context);
        final Intent alarmIntent = new Intent (context, MemoreaListActivity.class);;

        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_ready))
                .setSmallIcon(R.drawable.app_icon)
                .setAutoCancel(true);

        manager.notify(0, builder.build());
    }
}
