package com.memorease.memorease;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MemoreaInfoFragment extends Fragment {

    public String[] memoreaInfo;

    public MemoreaInfoFragment() {}

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorea_info, container, false);
        memoreaInfo = getArguments().getStringArray("memorea_info");
        setTextViews(view, memoreaInfo);

        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar)view.findViewById(R.id.toolbar));
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(memoreaInfo[0]);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return view;
    }

    private void setTextViews(final View view, final String[] memoreaInfo) {
        ((TextView)view.findViewById(R.id.text_view_memorea_info_question)).setText(memoreaInfo[1]);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_answer)).setText(memoreaInfo[2]);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_hint)).setText(memoreaInfo[3]);
    }
}