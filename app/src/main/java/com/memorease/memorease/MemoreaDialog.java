package com.memorease.memorease;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.TextView;

/**
 * Created by Tommy on 7/22/2015.
 */
public class MemoreaDialog extends DialogFragment {
    public interface OnAddMemoreaListener {
        void onAddMemoreaCard(MemoreaInfo memoreaInfo);
        void onEditMemoreaCard(String[] updatedFields);
    }

    private EditText title, question, answer, hint;
    private EditText[] requiredFields;
    private OnAddMemoreaListener memoreaListener;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            memoreaListener = (OnAddMemoreaListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnAddMemoreaListener");
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View addView = inflater.inflate(R.layout.fragment_add_memorea, null);
        initEditText(addView);
        if (getArguments() != null && getArguments().getStringArray("edit_memorea_info") != null) {
            setEditTextFields(addView, getArguments().getStringArray("edit_memorea_info"));
        }

        builder.setView(addView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(getArguments().getString("dialog_title"));
        final Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null) {
            final Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (requiredFieldsSet()) {
                        if (getArguments().getBoolean("is_editing")) {
                            editMemoreaCard();
                            d.dismiss();
                        } else {
                            addMemoreaCard();
                            d.dismiss();
                        }
                    } else {
                        setFocusToMissingFields();
                    }
                }
            });
        }
    }

    private void initEditText(final View dialogView) {
        title = (EditText)dialogView.findViewById(R.id.edit_text_title);
        question = (EditText)dialogView.findViewById(R.id.edit_text_question);
        answer = (EditText)dialogView.findViewById(R.id.edit_text_answer);
        hint = (EditText)dialogView.findViewById(R.id.edit_text_hint);
        requiredFields = new EditText[3];
        requiredFields[0] = title;
        requiredFields[1] = question;
        requiredFields[2] = answer;
    }

    private boolean requiredFieldsSet() {
        for (EditText field : requiredFields) {
            if (field.getText().toString().matches("")) {
                return false;
            }
        }

        return true;
    }

    private void setFocusToMissingFields() {
        boolean setFocus = false;
        for (EditText field : requiredFields) {
            if (field.getText().toString().matches("")) {
                if (!setFocus) {
                    field.requestFocus();
                    setFocus = true;
                }
                final Drawable originalDrawable = field.getBackground();
                final Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
                DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(Color.RED));
                field.setBackground(wrappedDrawable);
            }
        }
    }

    private void setEditTextFields(final View view, final String[] memoreaInfo) {
        title.setText(memoreaInfo[1]);
        question.setText(memoreaInfo[2]);
        answer.setText(memoreaInfo[3]);
        hint.setText(memoreaInfo[4]);
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
        memoreaListener.onEditMemoreaCard(updatedFields);
    }
}
