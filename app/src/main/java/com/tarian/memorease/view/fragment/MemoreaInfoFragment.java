package com.tarian.memorease.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tarian.memorease.R;
import com.tarian.memorease.MemoreaSharedPreferences;
import com.tarian.memorease.presenter.MemoreaListPresenter;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;


/**
 * Fragment that displays the memorea information in a grid
 */
@RequiresPresenter(MemoreaListPresenter.class)
public class MemoreaInfoFragment extends NucleusSupportFragment<MemoreaListPresenter> {

    private static final String TITLE = "title";
    private static final String QUESTION = "question";
    private static final String ANSWER = "answer";
    private static final String HINT = "hint";

    private String mTitle, mQuestion, mAnswer, mHint;

    public MemoreaInfoFragment() {}

    @SuppressLint("ValidFragment")
    public MemoreaInfoFragment(String title, String question, String answer, String hint) {
        mTitle = title;
        mQuestion = question;
        mAnswer = answer;
        mHint = hint;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle != null) {
            mTitle = bundle.getString(TITLE);
            mQuestion = bundle.getString(QUESTION);
            mAnswer = bundle.getString(ANSWER);
            mHint = bundle.getString(HINT);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorea_info, container, false);
        updateFields(view, mTitle, mQuestion, mAnswer, mHint);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(TITLE, mTitle);
        savedInstanceState.putString(QUESTION, mQuestion);
        savedInstanceState.putString(ANSWER, mAnswer);
        savedInstanceState.putString(HINT, mHint);
    }

    /**
     * Updates the memorea information after an edit
     */
    public void updateFields(String title, String question, String answer, String hint) {
        mTitle = title;
        mQuestion = question;
        mAnswer = answer;
        mHint = hint;

        updateFields(getView(), mTitle, mQuestion, mAnswer, mHint);
    }

    private void updateFields(final View view, final String title, final String question,
                              final String answer, final String hint) {

        if (view.findViewById(R.id.text_view_memorea_info_title) != null) {
            ((TextView)view.findViewById(R.id.text_view_memorea_info_title)).setText(title);
        } else {
            final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                    getActivity().findViewById(R.id.collapsing_toolbar);
            if (collapsingToolbarLayout != null) {
                collapsingToolbarLayout.setTitle(title);
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
                collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
            }
        }
        ((TextView)view.findViewById(R.id.text_view_memorea_info_question)).setText(question);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_answer)).setText(answer);
        if (hint.matches("")) {
            view.findViewById(R.id.text_view_memorea_info_hint_label).setVisibility(View.GONE);
            view.findViewById(R.id.text_view_memorea_info_hint).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.text_view_memorea_info_hint_label).setVisibility(View.VISIBLE);
            view.findViewById(R.id.text_view_memorea_info_hint).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.text_view_memorea_info_hint)).setText(hint);
        }
    }
}