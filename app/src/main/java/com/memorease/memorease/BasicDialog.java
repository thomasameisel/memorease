package com.memorease.memorease;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class BasicDialog extends DialogFragment {
    public static BasicDialog newInstance(final int title, final int message) {
        final BasicDialog frag = new BasicDialog();
        final Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int title = getArguments().getInt("title");
        final int message = getArguments().getInt("message");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }
}