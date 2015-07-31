package com.tarian.memorease;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tarian.memorease.views.Circle;
import com.tarian.memorease.views.CircleAngleAnimation;


/**
 * Fragment that shows the mQuestion and prompts the user to ask for a mHint or go to the mAnswer
 */
public class MemorizeScreenFragment extends Fragment {
    public static final String CIRCLE_VISIBLE = "circleVisible";
    public static final String MEMOREA_QUESTION = "memoreaQuestion";
    public static final String MEMOREA_HINT = "memoreaHint";

    private int mSpaceBetweenButtons;
    private boolean mCircleVisible;

    public MemorizeScreenFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mSpaceBetweenButtons = (int)dpToPx(50);
        final View view = inflater.inflate(R.layout.fragment_memorize_screen, container, false);
        ((TextView)view.findViewById(R.id.text_view_question)).setText(getActivity().getIntent().getStringExtra("mQuestion"));
        ((TextView)view.findViewById(R.id.text_view_hint)).setText(getActivity().getIntent().getStringExtra("mHint"));

        if (savedInstanceState == null || savedInstanceState.getBoolean(CIRCLE_VISIBLE, true)) {
            mCircleVisible = true;
            final Circle circle = (Circle) view.findViewById(R.id.circle);
            final CircleAngleAnimation animation = new CircleAngleAnimation(circle, 360);
            animation.setDuration(10000);
            animation.setAnimationListener(createAnimationListener(view));
            circle.startAnimation(animation);
        } else {
            mCircleVisible = false;
            view.findViewById(R.id.circle).setAlpha(0f);
            if (!savedInstanceState.getString(MEMOREA_HINT, "").matches("")) {
                view.findViewById(R.id.button_hint).setAlpha(1f);
            } else {
                removeHintButton(view);
            }
            view.findViewById(R.id.button_answer).setAlpha(1f);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putBoolean(CIRCLE_VISIBLE, mCircleVisible);
        if (getView() != null) {
            savedInstanceState.putString(MEMOREA_QUESTION, ((TextView) getView().findViewById(R.id.text_view_question)).getText().toString());
            savedInstanceState.putString(MEMOREA_HINT, ((TextView) getView().findViewById(R.id.text_view_hint)).getText().toString());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Shows the user the mHint
     */
    public void giveHint() {
        if (getView() != null) {
            final AnimatorSet animationSet = new AnimatorSet();
            animationSet.playTogether(createTranslationYAnimator(getView().findViewById(R.id.text_view_question), 0, 0 - getView().findViewById(R.id.text_view_hint).getHeight()),
                    createFadeAnimator(getView().findViewById(R.id.text_view_hint), 0, 1),
                    createFadeAnimator(getView().findViewById(R.id.button_hint), 1, 0),
                    createTranslationXAnimator(getView().findViewById(R.id.button_answer), 0, 0 - mSpaceBetweenButtons));
            animationSet.start();
        }
    }

    private void removeHintButton(final View view) {
        view.findViewById(R.id.button_hint).setVisibility(View.GONE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)view.findViewById(R.id.button_answer).getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
    }

    private Animation.AnimationListener createAnimationListener(final View view) {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                mCircleVisible = false;
                final AnimatorSet animationSet = new AnimatorSet();

                if (((TextView) view.findViewById(R.id.text_view_hint)).getText().toString().matches("")) {
                    removeHintButton(view);
                    animationSet.playTogether(createFadeAnimator(view.findViewById(R.id.circle), 1, 0),
                            createFadeAnimator(view.findViewById(R.id.button_answer), 0, 1));
                } else {
                    animationSet.playTogether(createFadeAnimator(view.findViewById(R.id.circle), 1, 0),
                            createFadeAnimator(view.findViewById(R.id.button_answer), 0, 1),
                            createFadeAnimator(view.findViewById(R.id.button_hint), 0, 1),
                            createTranslationXAnimator(view.findViewById(R.id.button_answer), 0 - mSpaceBetweenButtons, 0),
                            createTranslationXAnimator(view.findViewById(R.id.button_hint), mSpaceBetweenButtons, 0));
                }
                animationSet.start();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
            }
        };
    }

    private float dpToPx(final int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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
