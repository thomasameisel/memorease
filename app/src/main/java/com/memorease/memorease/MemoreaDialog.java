package com.memorease.memorease;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * Popup dialog fragment for either adding or editing a memorea<br>
 * Requires an Extra String dialog_title and boolean is_editing<br>
 * If is_editing is true, also requires String array of the memorea's information to pre-fill in the form
 */
public class MemoreaDialog extends DialogFragment {
    /**
     * Listener for the result of either adding or editing a memorea
     */
    public interface OnSaveMemoreaDialog {
        /**
         * Called when the Save button is pressed in the Memorea Dialog
         * @param fields String array of length 4 with the title, question, answer, and hint
         */
        void onSaveMemoreaDialog(String[] fields);
    }

    private EditText title, question, answer, hint;
    private EditText[] requiredFields;
    private OnSaveMemoreaDialog memoreaListener;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            memoreaListener = (OnSaveMemoreaDialog)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnSaveMemoreaDialog");
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
                        final String[] updatedFields = new String[4];
                        updatedFields[0] = ((EditText) getDialog().findViewById(R.id.edit_text_title)).getText().toString();
                        updatedFields[1] = ((EditText) getDialog().findViewById(R.id.edit_text_question)).getText().toString();
                        updatedFields[2] = ((EditText) getDialog().findViewById(R.id.edit_text_answer)).getText().toString();
                        updatedFields[3] = ((EditText) getDialog().findViewById(R.id.edit_text_hint)).getText().toString();
                        memoreaListener.onSaveMemoreaDialog(updatedFields);
                        d.dismiss();
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
        title.setText(memoreaInfo[0]);
        question.setText(memoreaInfo[1]);
        answer.setText(memoreaInfo[2]);
        hint.setText(memoreaInfo[3]);
    }
}
