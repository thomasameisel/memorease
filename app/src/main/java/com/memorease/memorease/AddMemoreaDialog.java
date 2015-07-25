package com.memorease.memorease;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by Tommy on 7/22/2015.
 */
public class AddMemoreaDialog extends DialogFragment {
    public interface OnAddMemoreaListener {
        void addMemoreaCard(MemoreaInfo memoreaInfo);
    }

    private OnAddMemoreaListener onAddMemoreaListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onAddMemoreaListener = (OnAddMemoreaListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnAddMemoreaListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View addView = inflater.inflate(R.layout.fragment_add_memorea, null);
        if (getArguments() != null && getArguments().getStringArray("edit_memorea_info") != null) {
            setEditTextFields(addView, getArguments().getStringArray("edit_memorea_info"));
        }

        builder.setView(addView)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        addMemoreaCard();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddMemoreaDialog.this.getDialog().cancel();
                    }
                })
                .setTitle(R.string.add_memorea_title);
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }

    private void setEditTextFields(final View view, final String[] memoreaInfo) {
        ((EditText)view.findViewById(R.id.edit_text_title)).setText(memoreaInfo[0]);
        ((EditText)view.findViewById(R.id.edit_text_question)).setText(memoreaInfo[1]);
        ((EditText)view.findViewById(R.id.edit_text_answer)).setText(memoreaInfo[2]);
        ((EditText)view.findViewById(R.id.edit_text_hint)).setText(memoreaInfo[3]);
    }

    private void addMemoreaCard() {
        onAddMemoreaListener.addMemoreaCard(new MemoreaInfo(((EditText)getDialog().findViewById(R.id.edit_text_title)).getText().toString(),
                ((EditText)getDialog().findViewById(R.id.edit_text_question)).getText().toString(),
                ((EditText)getDialog().findViewById(R.id.edit_text_answer)).getText().toString(),
                15,
                ((EditText)getDialog().findViewById(R.id.edit_text_hint)).getText().toString()));
    }
}
