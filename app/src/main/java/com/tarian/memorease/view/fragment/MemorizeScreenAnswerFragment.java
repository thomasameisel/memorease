package com.tarian.memorease.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tarian.memorease.R;
import com.tarian.memorease.presenter.MemorizeScreenPresenter;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

/**
 * Fragment that shows the mAnswer and prompts the user to select Got it or Wrong<br>
 * Must have an Extra String mAnswer
 */
@RequiresPresenter(MemorizeScreenPresenter.class)
public class MemorizeScreenAnswerFragment extends NucleusSupportFragment<MemorizeScreenPresenter> {

    private String mAnswer;

    public MemorizeScreenAnswerFragment() {
    }

    @SuppressLint("ValidFragment")
    public MemorizeScreenAnswerFragment(String answer) {
        mAnswer = answer;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorize_screen_answer, container, false);
        ((TextView)view.findViewById(R.id.text_view_answer)).setText(mAnswer);
        return view;
    }
}
