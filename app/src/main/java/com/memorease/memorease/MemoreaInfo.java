package com.memorease.memorease;

import java.util.UUID;

/**
 * Created by Tommy on 7/22/2015.
 */
public class MemoreaInfo {
    String title;
    String question;
    String answer;
    String hint;
    int nextMemorization;
    UUID uuid;

    public MemoreaInfo(final String title, final String question, final String answer, final int nextMemorization) {
        this(title, question, answer, nextMemorization, null);
    }

    public MemoreaInfo(final String title, final String question, final String answer, final int nextMemorization, final String hint) {
        this.title = title;
        this.question = question;
        this.answer = answer;
        this.nextMemorization = nextMemorization;
        this.hint = hint;
        this.uuid = UUID.randomUUID();
    }

    public void updateFields(String[] updatedFields) {
        this.title = updatedFields[1];
        this.question = updatedFields[2];
        this.answer = updatedFields[3];
    }
}
