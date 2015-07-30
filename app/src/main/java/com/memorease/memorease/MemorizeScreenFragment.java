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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.memorease.memorease.views.Circle;
import com.memorease.memorease.views.CircleAngleAnimation;


/**
 * Fragment that shows the question and prompts the user to ask for a hint or go to the answer
 */
public class MemorizeScreenFragment extends Fragment {
    private final String CIRCLE_VISIBLE = "buttonsVisible";
    private final String MEMOREA_QUESTION = "memoreaQuestion";
    private final String MEMOREA_HINT = "memoreaHint";

    private int spaceBetweenButtons;
    private boolean circleVisible;

    public MemorizeScreenFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        spaceBetweenButtons = (int)dpToPx(50);
        final View view = inflater.inflate(R.layout.fragment_memorize_screen, container, false);
        ((TextView)view.findViewById(R.id.text_view_question)).setText(getActivity().getIntent().getStringExtra("question"));
        ((TextView)view.findViewById(R.id.text_view_hint)).setText(getActivity().getIntent().getStringExtra("hint"));

        if (savedInstanceState == null || savedInstanceState.getBoolean(CIRCLE_VISIBLE, true)) {
            circleVisible = true;
            final Circle circle = (Circle) view.findViewById(R.id.circle);
            final CircleAngleAnimation animation = new CircleAngleAnimation(circle, 360);
            animation.setDuration(10000);
            animation.setAnimationListener(createAnimationListener(view));
            circle.startAnimation(animation);
        } else {
            circleVisible = false;
            view.findViewById(R.id.circle).setAlpha(0f);
            if (!savedInstanceState.getString(MEMOREA_HINT, "").matches("")) {
                view.findViewById(R.id.button_give_hint).setAlpha(1f);
            } else {
                view.findViewById(R.id.button_give_hint).setVisibility(View.GONE);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)view.findViewById(R.id.button_answer).getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
            }
            view.findViewById(R.id.button_answer).setAlpha(1f);
            setFields(view, savedInstanceState.getString(MEMOREA_QUESTION, ""), savedInstanceState.getString(MEMOREA_HINT, ""));
        }
        return view;
    }

    private Animation.AnimationListener createAnimationListener(final View view) {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                circleVisible = false;
                final AnimatorSet animationSet = new AnimatorSet();

                if (((TextView) view.findViewById(R.id.text_view_hint)).getText().toString().matches("")) {
                    animationSet.playTogether(createFadeAnimator(view.findViewById(R.id.circle), 1, 0),
                            createFadeAnimator(view.findViewById(R.id.button_answer), 0, 1),
                            createTranslationXAnimator(view.findViewById(R.id.button_answer), 0 - spaceBetweenButtons, 0 - spaceBetweenButtons));
                } else {
                    animationSet.playTogether(createFadeAnimator(view.findViewById(R.id.circle), 1, 0),
                            createFadeAnimator(view.findViewById(R.id.button_answer), 0, 1),
                            createFadeAnimator(view.findViewById(R.id.button_give_hint), 0, 1),
                            createTranslationXAnimator(view.findViewById(R.id.button_answer), 0 - spaceBetweenButtons, 0),
                            createTranslationXAnimator(view.findViewById(R.id.button_give_hint), spaceBetweenButtons, 0));
                }
                animationSet.start();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
            }
        };
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putBoolean(CIRCLE_VISIBLE, circleVisible);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Sets the question and hint fields, must be called after onCreateView
     */
    public void setFields(final View view, final String question, final String hint) {
        ((TextView)view.findViewById(R.id.text_view_question)).setText(question);
        ((TextView)view.findViewById(R.id.text_view_hint)).setText(hint);
    }

    /**
     * Shows the user the hint
     */
    public void giveHint() {
        final AnimatorSet animationSet = new AnimatorSet();
        animationSet.playTogether(createTranslationYAnimator(getView().findViewById(R.id.text_view_question), 0, 0 - getView().findViewById(R.id.text_view_hint).getHeight()),
                createFadeAnimator(getView().findViewById(R.id.text_view_hint), 0, 1),
                createFadeAnimator(getView().findViewById(R.id.button_give_hint), 1, 0),
                createTranslationXAnimator(getView().findViewById(R.id.button_answer), 0, 0-spaceBetweenButtons));
        animationSet.start();
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
