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

    String[] memoreaInfo;
    int memoreaPosition;

    public MemoreaInfoFragment() {}

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorea_info, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar)view.findViewById(R.id.toolbar));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        memoreaInfo = getArguments().getStringArray("memorea_info");
        memoreaPosition = getArguments().getInt("memorea_position");
        updateFields(view, memoreaInfo);

        return view;
    }

    public void updateFieldsFromEdit(final String[] updatedFields) {
        memoreaInfo = updatedFields;
        updateFields(getView(), memoreaInfo);
    }

    private void updateFields(final View view, final String[] updatedFields) {
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(updatedFields[1]);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_question)).setText(updatedFields[2]);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_answer)).setText(updatedFields[3]);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_hint)).setText(updatedFields[4]);
    }
}