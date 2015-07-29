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
    /**
     * Listener for setting the activity's action bar as the collapsible action bar
     */
    public interface OnMemoreaInfoFragment {
        /**
         * Sets the activity's support action bar as the collapsible action bar
         * @param view Toolbar view in fragment
         */
        void setSupportActionBar(Toolbar view);
        /**
         * Gets the support action bar of the activity
         */
        ActionBar getSupportActionBar();
    }

    private OnMemoreaInfoFragment listener;

    public MemoreaInfoFragment() {}

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnMemoreaInfoFragment)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnAddMemoreaListener");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorea_info, container, false);
        listener.setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        listener.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateFields(view, getArguments().getStringArray("memorea_info"));

        return view;
    }

    /**
     * Updates the memorea information after an edit
     * @param updatedFields String array of length 5
     */
    public void updateFieldsFromEdit(final String[] updatedFields) {
        updateFields(getView(), updatedFields);
    }

    private void updateFields(final View view, final String[] updatedFields) {
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(updatedFields[1]);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_question)).setText(updatedFields[2]);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_answer)).setText(updatedFields[3]);
        if (updatedFields[4].matches("")) {
            view.findViewById(R.id.text_view_memorea_info_hint_label).setVisibility(View.GONE);
            view.findViewById(R.id.text_view_memorea_info_hint).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.text_view_memorea_info_hint_label).setVisibility(View.VISIBLE);
            view.findViewById(R.id.text_view_memorea_info_hint).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.text_view_memorea_info_hint)).setText(updatedFields[4]);
        }
    }
}