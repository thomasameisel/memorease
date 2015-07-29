package com.memorease.memorease;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Fragment that shows the answer and prompts the user to select Got it or Wrong<br>
 * Must have an Extra String answer
 */
public class MemorizeScreenAnswerFragment extends Fragment {

    public MemorizeScreenAnswerFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorize_screen_answer, container, false);
        setAnswer(view, getArguments().getString("answer"));
        return view;
    }

    private void setAnswer(final View view, final String answer) {
        ((TextView)view.findViewById(R.id.text_view_answer)).setText(answer);
    }
}
