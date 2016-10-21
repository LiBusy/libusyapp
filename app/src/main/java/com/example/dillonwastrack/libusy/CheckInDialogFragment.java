package com.example.dillonwastrack.libusy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Fragment for the dialog box that
 * prompts the user to check in.
 *
 */
public class CheckInDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final CheckInDialogListener mListener = (CheckInDialogListener) this.getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You seem to be near "+ getArguments().get("library") + " library, would you like to check in?")
                .setPositiveButton("check-in", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(CheckInDialogFragment.this);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(CheckInDialogFragment.this);
                    }
                });
        return builder.create();
    }

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface CheckInDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }



}
