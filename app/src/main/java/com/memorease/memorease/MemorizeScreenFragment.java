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
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.TextView;

import com.memorease.memorease.views.Circle;
import com.memorease.memorease.views.CircleAngleAnimation;


/**
 * A placeholder fragment containing a simple view.
 */
public class MemorizeScreenFragment extends Fragment {
    private boolean gaveHint = false;

    public MemorizeScreenFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorize_screen, container, false);
        final Circle circle = (Circle) view.findViewById(R.id.circle);

        final CircleAngleAnimation animation = new CircleAngleAnimation(circle, 360);
        animation.setDuration(10000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
            }
            @Override
            public void onAnimationEnd(final Animation animation) {
                final AnimatorSet animationSet = new AnimatorSet();
                animationSet.playTogether(createFadeAnimator(view.findViewById(R.id.circle), 1, 0),
                        createFadeAnimator(view.findViewById(R.id.button_answer), 0, 1),
                        createFadeAnimator(view.findViewById(R.id.button_give_hint), 0, 1),
                        createTranslationXAnimator(view.findViewById(R.id.button_answer), 0-(int)dpToPx(50), 0),
                        createTranslationXAnimator(view.findViewById(R.id.button_give_hint), (int)dpToPx(50), 0));
                animationSet.start();
            }
            @Override
            public void onAnimationRepeat(final Animation animation) {
            }
        });
        circle.startAnimation(animation);
        return view;
    }

    public void setFields(final Bundle extras) {
        ((TextView)getView().findViewById(R.id.text_view_question)).setText(extras.getString("question"));
        ((TextView)getView().findViewById(R.id.text_view_hint)).setText(extras.getString("hint"));
    }

    public void giveHint() {
        if (!gaveHint) {
            final AnimatorSet animationSet = new AnimatorSet();
            animationSet.playTogether(createTranslationYAnimator(getView().findViewById(R.id.text_view_question), 0, 0 - getView().findViewById(R.id.text_view_hint).getHeight()),
                    createFadeAnimator(getView().findViewById(R.id.text_view_hint), 0, 1));
            animationSet.start();
            gaveHint = true;
        }
    }

    private float dpToPx(final int dp) {
        final Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    private static final String TRANSLATION_X = "translationX";
    private static final String TRANSLATION_Y = "translationY";
    private static final String ALPHA = "alpha";

    private Animator createTranslationXAnimator(final View view, final int start, final int end) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_X, start, end)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createTranslationYAnimator(final View view, final int start, final int end) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, start, end)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createFadeAnimator(final View view, final int from, final int to) {
        return ObjectAnimator.ofFloat(view, ALPHA, from, to)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }
}
