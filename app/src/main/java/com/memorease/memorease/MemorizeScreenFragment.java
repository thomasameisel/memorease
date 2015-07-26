package com.memorease.memorease;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MemorizeScreenFragment extends Fragment {
    private boolean gaveHint = false;

    public MemorizeScreenFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memorize_screen, container, false);
    }

    public void setFields(final Bundle extras) {
        ((TextView)getView().findViewById(R.id.text_view_question)).setText(extras.getString("question"));
        ((TextView)getView().findViewById(R.id.text_view_hint)).setText(extras.getString("hint"));
    }

    public void giveHint() {
        if (!gaveHint) {
            final AnimatorSet animationSet = new AnimatorSet();
            animationSet.playTogether(createTranslationYAnimator(getView().findViewById(R.id.text_view_question), 0 - getView().findViewById(R.id.text_view_hint).getHeight()),
                    createFadeAnimator(getView().findViewById(R.id.text_view_hint), 0, 1));
            animationSet.start();
            gaveHint = true;
        }
    }

    private static final String TRANSLATION_Y = "translationY";
    private static final String ALPHA = "alpha";

    private Animator createTranslationYAnimator(final View view, final int offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, offset)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createFadeAnimator(final View view, final int from, final int to) {
        return ObjectAnimator.ofFloat(view, ALPHA, from, to)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }
}
