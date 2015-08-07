package com.tarian.memorease;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.tarian.memorease.model.Memorea;

import java.util.Calendar;

/**
 * Created by Tommy on 8/6/2015.
 */
public class MemoreaAlarmManager {

    public static void createNotification(Context context, Memorea memorea) {
        final int notificationId = (int)(Calendar.getInstance().getTimeInMillis() & 0xfffffff);
        memorea.mNotificationId = notificationId;
        final long memorizationTime = SystemClock.elapsedRealtime() + memorea.getCurMemorization();
        MemoreaSharedPreferences.setNotificationTime(context, memorea.mId.toString(),
                memorizationTime);

        final Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)context
                .getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.ELAPSED_REALTIME, memorizationTime, pendingIntent);
    }

    public static void cancelNotification(Context context, int notifiationId) {
        final Intent intent = new Intent(context, AlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notifiationId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)context
                .getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
