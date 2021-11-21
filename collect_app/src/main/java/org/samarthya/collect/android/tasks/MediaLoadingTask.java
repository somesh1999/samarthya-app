package org.samarthya.collect.android.tasks;

import android.net.Uri;
import android.os.AsyncTask;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import org.samarthya.collect.android.activities.FormEntryActivity;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.fragments.dialogs.ProgressDialogFragment;
import org.samarthya.collect.android.injection.DaggerUtils;
import org.samarthya.collect.android.javarosawrapper.FormController;
import org.samarthya.collect.android.preferences.source.SettingsProvider;
import org.samarthya.collect.android.utilities.ContentUriHelper;
import org.samarthya.collect.android.utilities.FileUtils;
import org.samarthya.collect.android.utilities.ImageConverter;
import org.samarthya.collect.android.widgets.BaseImageWidget;
import org.samarthya.collect.android.widgets.QuestionWidget;

import java.io.File;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_IMAGE_SIZE;

public class MediaLoadingTask extends AsyncTask<Uri, Void, File> {

    @Inject
    SettingsProvider settingsProvider;

    private WeakReference<FormEntryActivity> formEntryActivity;

    public MediaLoadingTask(FormEntryActivity formEntryActivity) {
        onAttach(formEntryActivity);
    }

    public void onAttach(FormEntryActivity formEntryActivity) {
        this.formEntryActivity = new WeakReference<>(formEntryActivity);
        DaggerUtils.getComponent(this.formEntryActivity.get()).inject(this);
    }

    @Override
    protected File doInBackground(Uri... uris) {
        FormController formController = Collect.getInstance().getFormController();

        if (formController != null) {
            File instanceFile = formController.getInstanceFile();
            if (instanceFile != null) {
                String extension = ContentUriHelper.getFileExtensionFromUri(uris[0]);

                File newFile = FileUtils.createDestinationMediaFile(instanceFile.getParent(), extension);
                FileUtils.saveAnswerFileFromUri(uris[0], newFile, Collect.getInstance());
                QuestionWidget questionWidget = formEntryActivity.get().getWidgetWaitingForBinaryData();

                // apply image conversion if the widget is an image widget
                if (questionWidget instanceof BaseImageWidget) {
                    String imageSizeMode = settingsProvider.getGeneralSettings().getString(KEY_IMAGE_SIZE);
                    ImageConverter.execute(newFile.getPath(), questionWidget, formEntryActivity.get(), imageSizeMode);
                }
                return newFile;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        Fragment prev = formEntryActivity.get().getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.COLLECT_PROGRESS_DIALOG_TAG);
        if (prev != null && !formEntryActivity.get().isInstanceStateSaved()) {
            ((DialogFragment) prev).dismiss();
        }
        formEntryActivity.get().setWidgetData(result);
    }
}
