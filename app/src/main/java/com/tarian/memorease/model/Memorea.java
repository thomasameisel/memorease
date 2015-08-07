package com.tarian.memorease.model;

import android.content.Context;
import android.os.SystemClock;

import com.tarian.memorease.MemoreaSharedPreferences;

import java.util.UUID;

/**
 * Information for a memorea
 */
public class Memorea {
    public String mTitle;
    public String mQuestion;
    public String mAnswer;
    public String mHint;
    public int mMemorizationLevel;
    public UUID mId;
    public int mNotificationId;
    public boolean mCompleted = false;

    private long[] mMemorizationTimes = {120000L, 600000L, 3600000L, 18000000L, 86400000L, 432000000L, 2160000000L, 13046400000L};

    /**
     * Creates a new memorea
     * @param mTitle Title of the memorea
     * @param mQuestion Question of the memorea
     * @param mHint Hint of the memorea. If user does not enter this value, set it to empty string
     * @param mMemorizationLevel Index of the memorization, milliseconds in mMemorizationTimes
     */
    public Memorea(final String mTitle, final String mQuestion, final String mAnswer,
                   final String mHint, final int mMemorizationLevel) {
        this.mTitle = mTitle;
        this.mQuestion = mQuestion;
        this.mAnswer = mAnswer;
        this.mHint = mHint;
        this.mMemorizationLevel = mMemorizationLevel;
        this.mCompleted = mMemorizationLevel >= mMemorizationTimes.length;
    }

    public Memorea(final String[] fields) {
        this(fields[0], fields[1], fields[2], fields[3], Integer.valueOf(fields[4]));
        this.mNotificationId = Integer.valueOf(fields[5]);
    }

    /**
     * Generates a new mId for the memorea
     */
    public void generateNewId() {
        mId = UUID.randomUUID();
    }

    /**
     * Returns a String array with the memorea's mTitle, mQuestion, mAnswer, mHint, mMemorizationLevel, and mNotificationId
     */
    public String[] getFields() {
        String[] fields = new String[6];
        fields[0] = mTitle;
        fields[1] = mQuestion;
        fields[2] = mAnswer;
        fields[3] = mHint;
        fields[4] = Integer.toString(mMemorizationLevel);
        fields[5] = Integer.toString(mNotificationId);
        return fields;
    }

    /**
     * Gets the milliseconds until the next memorization time
     */
    public long getTimeUntilNextAlarm(Context context) {
        return getTimeNextAlarm(context)- SystemClock.elapsedRealtime();
    }

    /**
     * Gets the millseconds of the next memorization time since the epoch
     */
    public long getTimeNextAlarm(Context context) {
        return MemoreaSharedPreferences.getNotificationTime(context, mId.toString());
    }

    /**
     * Gets the current memorization time span in milliseconds
     */
    public long getCurMemorization() {
        return mMemorizationTimes[mMemorizationLevel];
    }
}
