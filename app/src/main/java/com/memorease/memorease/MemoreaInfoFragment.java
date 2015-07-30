package com.memorease.memorease;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * Fragment that displays the memorea information in a grid
 */
public class MemoreaInfoFragment extends Fragment {
    public MemoreaInfoFragment() {}

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memorea_info, container, false);
    }

    /**
     * Updates the memorea information after an edit
     * @param updatedFields String array of length 4
     */
    public void updateFields(final String[] updatedFields) {
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)getActivity().findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(updatedFields[0]);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        ((TextView)getView().findViewById(R.id.text_view_memorea_info_question)).setText(updatedFields[1]);
        ((TextView)getView().findViewById(R.id.text_view_memorea_info_answer)).setText(updatedFields[2]);
        if (updatedFields[3].matches("")) {
            getView().findViewById(R.id.text_view_memorea_info_hint_label).setVisibility(View.GONE);
            getView().findViewById(R.id.text_view_memorea_info_hint).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.text_view_memorea_info_hint_label).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.text_view_memorea_info_hint).setVisibility(View.VISIBLE);
            ((TextView)getView().findViewById(R.id.text_view_memorea_info_hint)).setText(updatedFields[3]);
        }
    }
}