package com.memorease.memorease;

/**
 * Created by Tommy on 7/22/2015.
 */
public class MemoreaInfo {
    String title;
    String question;
    String answer;
    String hint;
    int nextMemorization;

    public MemoreaInfo(final String title, final String question, final String answer, final int nextMemorization) {
        this(title, question, answer, nextMemorization, null);
    }

    public MemoreaInfo(final String title, final String question, final String answer, final int nextMemorization, final String hint) {
        this.title = title;
        this.question = question;
        this.answer = answer;
        this.nextMemorization = nextMemorization;
        this.hint = hint;
    }
}
