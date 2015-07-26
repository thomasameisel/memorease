package com.memorease.memorease;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by Tommy on 7/22/2015.
 */
public class MemoreaDialog extends DialogFragment {
    public interface OnAddMemoreaListener {
        void onAddMemoreaCard(MemoreaInfo memoreaInfo);
        void onEditMemoreaCard(String[] updatedFields, int memoreaPosition);
    }

    private OnAddMemoreaListener memoreaListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            memoreaListener = (OnAddMemoreaListener)activity;
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
                        if (getArguments().getBoolean("is_editing")) {
                            editMemoreaCard();
                        } else {
                            addMemoreaCard();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MemoreaDialog.this.getDialog().cancel();
                    }
                })
                .setTitle(getArguments().getString("dialog_title"));
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }

    private void setEditTextFields(final View view, final String[] memoreaInfo) {
        ((EditText)view.findViewById(R.id.edit_text_title)).setText(memoreaInfo[1]);
        ((EditText)view.findViewById(R.id.edit_text_question)).setText(memoreaInfo[2]);
        ((EditText)view.findViewById(R.id.edit_text_answer)).setText(memoreaInfo[3]);
        ((EditText)view.findViewById(R.id.edit_text_hint)).setText(memoreaInfo[4]);
    }

    private void addMemoreaCard() {
        MemoreaInfo newMemorea = new MemoreaInfo(((EditText) getDialog().findViewById(R.id.edit_text_title)).getText().toString(),
                ((EditText) getDialog().findViewById(R.id.edit_text_question)).getText().toString(),
                ((EditText) getDialog().findViewById(R.id.edit_text_answer)).getText().toString(),
                ((EditText) getDialog().findViewById(R.id.edit_text_hint)).getText().toString(), 0);
        newMemorea.createNewUUID();
        memoreaListener.onAddMemoreaCard(newMemorea);
    }

    private void editMemoreaCard() {
        final String[] updatedFields = new String[5];
        updatedFields[0] = getArguments().getStringArray("edit_memorea_info")[0];
        updatedFields[1] = ((EditText) getDialog().findViewById(R.id.edit_text_title)).getText().toString();
        updatedFields[2] = ((EditText) getDialog().findViewById(R.id.edit_text_question)).getText().toString();
        updatedFields[3] = ((EditText) getDialog().findViewById(R.id.edit_text_answer)).getText().toString();
        updatedFields[4] = ((EditText) getDialog().findViewById(R.id.edit_text_hint)).getText().toString();
        memoreaListener.onEditMemoreaCard(updatedFields, getArguments().getInt("memorea_position"));
    }
}
