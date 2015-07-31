package com.tarian.memorease;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * Fragment that displays the memorea information in a grid
 */
public class MemoreaInfoFragment extends Fragment {
    private static final String MEMOREA_FIELDS = "memoreaFields";

    private String[] mMemoreaFields;

    public MemoreaInfoFragment() {}

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorea_info, container, false);
        if (savedInstanceState != null && savedInstanceState.getStringArray(MEMOREA_FIELDS) != null && savedInstanceState.getStringArray(MEMOREA_FIELDS).length >= 4) {
            mMemoreaFields = savedInstanceState.getStringArray(MEMOREA_FIELDS);
            updateFields(view, savedInstanceState.getStringArray(MEMOREA_FIELDS));
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putStringArray(MEMOREA_FIELDS, mMemoreaFields);
    }

    /**
     * Updates the memorea information after an edit
     * @param updatedFields String array of length 4
     */
    public void updateFields(final String[] updatedFields) {
        mMemoreaFields = updatedFields;

        updateFields(getView(), updatedFields);
    }

    private void updateFields(final View view, final String[] updatedFields) {
        if (view.findViewById(R.id.text_view_memorea_info_title) != null) {
            ((TextView)view.findViewById(R.id.text_view_memorea_info_title)).setText(updatedFields[0]);
        } else {
            final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.collapsing_toolbar);
            collapsingToolbarLayout.setTitle(updatedFields[0]);
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        }
        ((TextView)view.findViewById(R.id.text_view_memorea_info_question)).setText(updatedFields[1]);
        ((TextView)view.findViewById(R.id.text_view_memorea_info_answer)).setText(updatedFields[2]);
        if (updatedFields[3].matches("")) {
            view.findViewById(R.id.text_view_memorea_info_hint_label).setVisibility(View.GONE);
            view.findViewById(R.id.text_view_memorea_info_hint).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.text_view_memorea_info_hint_label).setVisibility(View.VISIBLE);
            view.findViewById(R.id.text_view_memorea_info_hint).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.text_view_memorea_info_hint)).setText(updatedFields[3]);
        }
    }
}