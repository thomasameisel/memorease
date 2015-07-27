package com.memorease.memorease;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Tommy on 7/22/2015.
 */
public class MemoreaInfo {
    String title;
    String question;
    String answer;
    String hint;
    int memorizationLevel;
    int position;
    UUID id;
    boolean completed=false;
    MemoreaListAdapter.MemoreaViewHolder holder;

    private int[] memorizationTimes = {2, 10, 60, 300, 1440, 7200, 36000, 172800};

    public MemoreaInfo(final String title, final String question, final String answer, final String hint, final int memorizationLevel) {
        this.title = title;
        this.question = question;
        this.answer = answer;
        this.hint = hint;
        this.memorizationLevel = memorizationLevel;
    }

    public void updateFields(String[] updatedFields) {
        this.title = updatedFields[1];
        this.question = updatedFields[2];
        this.answer = updatedFields[3];
        this.hint = updatedFields[4];
    }

    public void createNewUUID() {
        id = UUID.randomUUID();
    }

    public String[] getFields() {
        String[] fields = new String[6];
        fields[0] = title;
        fields[1] = question;
        fields[2] = answer;
        fields[3] = hint;
        fields[4] = Integer.toString(memorizationLevel);
        fields[5] = Integer.toString(position);
        return fields;
    }

    public int getTotalMemorizationLevels() {
        return memorizationTimes.length;
    }

    public long getTimeUntilNextAlarm() {
        return getTimeNextAlarm()-Calendar.getInstance().getTimeInMillis();
    }

    private long getTimeNextAlarm() {
        return MemoreaListActivity.sharedPreferences.getLong(id.toString()+"_notification_time", 0);
    }
}
