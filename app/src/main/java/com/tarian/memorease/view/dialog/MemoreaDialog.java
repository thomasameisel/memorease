package com.tarian.memorease.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.tarian.memorease.R;

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
         * @param fields String array of length 4 with the mTitle, mQuestion, mAnswer, and mHint
         */
        void onSaveMemoreaDialog(String id, String[] fields);
    }

    public static Bundle getCallingArguments(String dialogTitle, boolean isEditing) {
        final Bundle callingBundle = new Bundle();
        callingBundle.putString(DIALOG_TITLE, dialogTitle);
        callingBundle.putBoolean(IS_EDITING, isEditing);
        return callingBundle;
    }

    public static Bundle getCallingArguments(String dialogTitle, boolean isEditing, Bundle bundle) {
        final Bundle callingBundle = getCallingArguments(dialogTitle, isEditing);
        callingBundle.putAll(bundle);
        return callingBundle;
    }

    private static final String DIALOG_TITLE = "dialogTitle";
    private static final String IS_EDITING = "isEditing";

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String QUESTION = "question";
    private static final String ANSWER = "answer";
    private static final String HINT = "hint";
    private static final String LEVEL = "level";
    private static final String NOTIFICATION_GENERATOR = "notificationGenerator";

    private EditText mTitle, mQuestion, mAnswer, mHint;
    private EditText[] mRequiredFields;
    private OnSaveMemoreaDialog mMemoreaListener;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mMemoreaListener = (OnSaveMemoreaDialog)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSaveMemoreaDialog");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View addView = inflater.inflate(R.layout.fragment_add_memorea, null);
        initEditText(addView);
        if (getArguments() != null && getArguments().getBoolean(IS_EDITING, false)) {
            setEditTextFields();
        }

        builder.setView(addView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .setTitle(getArguments().getString(DIALOG_TITLE));
        final Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null) {
            final Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (requiredFieldsSet()) {
                        final String[] updatedFields = new String[6];
                        updatedFields[0] = ((EditText) getDialog()
                                .findViewById(R.id.edit_text_title)).getText().toString();
                        updatedFields[1] = ((EditText) getDialog()
                                .findViewById(R.id.edit_text_question)).getText().toString();
                        updatedFields[2] = ((EditText) getDialog()
                                .findViewById(R.id.edit_text_answer)).getText().toString();
                        updatedFields[3] = ((EditText) getDialog()
                                .findViewById(R.id.edit_text_hint)).getText().toString();
                        updatedFields[4] = getArguments().getString(LEVEL);
                        updatedFields[5] = getArguments().getString(NOTIFICATION_GENERATOR);
                        d.dismiss();
                        mMemoreaListener.onSaveMemoreaDialog(getArguments().getString(ID),
                                updatedFields);
                    } else {
                        setFocusToMissingFields();
                    }
                }
            });
        }
    }

    private void initEditText(final View dialogView) {
        mTitle = (EditText)dialogView.findViewById(R.id.edit_text_title);
        mQuestion = (EditText)dialogView.findViewById(R.id.edit_text_question);
        mAnswer = (EditText)dialogView.findViewById(R.id.edit_text_answer);
        mHint = (EditText)dialogView.findViewById(R.id.edit_text_hint);
        mRequiredFields = new EditText[3];
        mRequiredFields[0] = mTitle;
        mRequiredFields[1] = mQuestion;
        mRequiredFields[2] = mAnswer;
    }

    private boolean requiredFieldsSet() {
        for (EditText field : mRequiredFields) {
            if (field.getText().toString().matches("")) {
                return false;
            }
        }
        return true;
    }

    private void setFocusToMissingFields() {
        boolean setFocus = false;
        for (EditText field : mRequiredFields) {
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

    private void setEditTextFields() {
        mTitle.setText(getArguments().getString(TITLE));
        mQuestion.setText(getArguments().getString(QUESTION));
        mAnswer.setText(getArguments().getString(ANSWER));
        mHint.setText(getArguments().getString(HINT));
    }
}
