package com.tarian.memorease;

import android.os.SystemClock;

import java.util.UUID;

/**
 * Information for a memorea
 */
public class MemoreaInfo {
    String mTitle;
    String mQuestion;
    String mAnswer;
    String mHint;
    int mMemorizationLevel;
    UUID mId;
    int mNotificationGeneratorId;
    boolean mCompleted = false;

    private long[] mMemorizationTimes = {120000L, 600000L, 3600000L, 18000000L, 86400000L, 432000000L, 2160000000L, 13046400000L};

    /**
     * Creates a new memorea
     * @param mTitle Title of the memorea
     * @param mQuestion Question of the memorea
     * @param mHint Hint of the memorea. If user does not enter this value, set it to empty string
     * @param mMemorizationLevel Index of the memorization, milliseconds in mMemorizationTimes
     */
    public MemoreaInfo(final String mTitle, final String mQuestion, final String mAnswer, final String mHint, final int mMemorizationLevel) {
        this.mTitle = mTitle;
        this.mQuestion = mQuestion;
        this.mAnswer = mAnswer;
        this.mHint = mHint;
        this.mMemorizationLevel = mMemorizationLevel;
        this.mCompleted = mMemorizationLevel >= mMemorizationTimes.length;
    }

    /**
     * Generates a new mId for the memorea
     */
    public void generateNewId() {
        mId = UUID.randomUUID();
    }

    /**
     * Returns a String array with the memorea's mTitle, mQuestion, mAnswer, mHint, mMemorizationLevel, and mNotificationGeneratorId
     */
    public String[] getFields() {
        String[] fields = new String[6];
        fields[0] = mTitle;
        fields[1] = mQuestion;
        fields[2] = mAnswer;
        fields[3] = mHint;
        fields[4] = Integer.toString(mMemorizationLevel);
        fields[5] = Integer.toString(mNotificationGeneratorId);
        return fields;
    }

    /**
     * Returns the number of total memorization levels
     */
    public int getTotalMemorizationLevels() {
        return mMemorizationTimes.length;
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
        return MemoreaListActivity.sSharedPreferences.getLong(String.format(MemoreaListActivity.NOTIFICATION_TIME, mId.toString()), 0);
    }

    /**
     * Gets the current memorization time span in milliseconds
     */
    public long getCurMemorization() {
        return mMemorizationTimes[mMemorizationLevel];
    }
}
