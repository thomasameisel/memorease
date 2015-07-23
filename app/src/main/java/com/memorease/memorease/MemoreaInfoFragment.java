package com.memorease.memorease;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MemoreaInfoFragment extends Fragment {

    public MemoreaInfoFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getArguments().getStringArray("memorea_info")[0]);
        View view = inflater.inflate(R.layout.fragment_memorea_info, container, false);
        setTextViews(view, getArguments().getStringArray("memorea_info"));
        return view;
    }

    private void setTextViews(final View view, final String[] memoreaInfo) {
        ((TextView)view.findViewById(R.id.text_view_memorea_info_question)).setText(memoreaInfo[1]);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_answer)).setText(memoreaInfo[2]);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_hint)).setText(memoreaInfo[3]);
    }
}