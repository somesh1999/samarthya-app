package org.samarthya.collect.android.gdrive;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import org.samarthya.collect.android.R;

import static org.samarthya.collect.android.utilities.DialogUtils.showDialog;

public class GoogleAccountNotSetDialog {

    private GoogleAccountNotSetDialog() {

    }

    public static void show(Activity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.missing_google_account_dialog_title)
                .setMessage(R.string.missing_google_account_dialog_desc)
                .setOnCancelListener(dialog -> {
                    dialog.dismiss();
                    if (activity != null) {
                        activity.finish();
                    }
                })
                .setPositiveButton(activity.getString(R.string.ok), (dialog, which) -> {
                    dialog.dismiss();
                    activity.finish();
                })
                .create();

        showDialog(alertDialog, activity);
    }
}
