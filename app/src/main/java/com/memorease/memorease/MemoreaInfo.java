package com.memorease.memorease;

import android.app.NotificationManager;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.UUID;

/**
 * Information for a memorea
 */
public class MemoreaInfo {
    String title;
    String question;
    String answer;
    String hint;
    int memorizationLevel;
    UUID id;
    int notificationGeneratorId;
    boolean completed=false;

    private long[] memorizationTimes = {120000L, 600000L, 3600000L, 18000000L, 86400000L, 432000000L, 2160000000L, 13046400000L};

    /**
     * Creates a new memorea
     * @param title Title of the memorea
     * @param question Question of the memorea
     * @param hint Hint of the memorea. This is not required
     * @param memorizationLevel Index of the memorization, milliseconds in memorizationTimes
     */
    public MemoreaInfo(final String title, final String question, final String answer, final String hint, final int memorizationLevel) {
        this.title = title;
        this.question = question;
        this.answer = answer;
        this.hint = hint;
        this.memorizationLevel = memorizationLevel;
        this.completed = memorizationLevel >= memorizationTimes.length;
    }

    /**
     * Updates memorea fields on edit
     * @param updatedFields String array of length 5 consisting of the id, title, question, answer, and hint
     */
    public void updateFields(String[] updatedFields) {
        this.title = updatedFields[1];
        this.question = updatedFields[2];
        this.answer = updatedFields[3];
        this.hint = updatedFields[4];
    }

    /**
     * Generates a new id for the memorea
     */
    public void createNewUUID() {
        id = UUID.randomUUID();
    }

    /**
     * Returns a String array with the memorea's title, question, answer, hint, memorizationLevel, and notificationGeneratorId
     */
    public String[] getFields() {
        String[] fields = new String[6];
        fields[0] = title;
        fields[1] = question;
        fields[2] = answer;
        fields[3] = hint;
        fields[4] = Integer.toString(memorizationLevel);
        fields[5] = Integer.toString(notificationGeneratorId);
        return fields;
    }

    /**
     * Returns the number of total memorization levels
     */
    public int getTotalMemorizationLevels() {
        return memorizationTimes.length;
    }

    /**
     * Gets the milliseconds until the next memorization time
     */
    public long getTimeUntilNextAlarm() {
        return getTimeNextAlarm()- SystemClock.elapsedRealtime();
    }

    /**
     * Gets the millseconds of the next memorization time since the epoch
     */
    public long getTimeNextAlarm() {
        return MemoreaListActivity.sharedPreferences.getLong(id.toString()+"_notification_time", 0);
    }

    /**
     * Gets the current memorization time span in milliseconds
     */
    public long getCurMemorization() {
        return memorizationTimes[memorizationLevel];
    }
}
