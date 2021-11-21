package org.samarthya.collect.android.formentry;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.activities.FormHierarchyActivity;
import org.samarthya.collect.android.formentry.BackgroundAudioViewModel;
import org.samarthya.collect.android.formentry.FormEntryViewModel;
import org.samarthya.collect.android.formentry.FormIndexAnimationHandler;
import org.samarthya.collect.android.formentry.RecordingWarningDialogFragment;
import org.samarthya.collect.android.formentry.RequiresFormController;
import org.samarthya.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel;
import org.samarthya.collect.android.formentry.questions.AnswersProvider;
import org.samarthya.collect.android.formentry.saving.FormSaveViewModel;
import org.samarthya.collect.android.javarosawrapper.FormController;
import org.samarthya.collect.android.preferences.keys.ProtectedProjectKeys;
import org.samarthya.collect.android.preferences.screens.ProjectPreferencesActivity;
import org.samarthya.collect.android.preferences.source.SettingsProvider;
import org.samarthya.collect.android.utilities.ApplicationConstants;
import org.samarthya.collect.android.utilities.DialogUtils;
import org.samarthya.collect.android.utilities.MenuDelegate;
import org.samarthya.collect.android.utilities.PlayServicesChecker;
import org.samarthya.collect.audiorecorder.recording.AudioRecorder;

import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_BACKGROUND_LOCATION;

public class FormEntryMenuDelegate implements MenuDelegate, RequiresFormController {

    private final AppCompatActivity activity;
    private final AnswersProvider answersProvider;
    private final FormIndexAnimationHandler formIndexAnimationHandler;
    private final org.samarthya.collect.android.formentry.FormEntryViewModel formEntryViewModel;
    private final FormSaveViewModel formSaveViewModel;
    private final BackgroundLocationViewModel backgroundLocationViewModel;
    private final BackgroundAudioViewModel backgroundAudioViewModel;

    @Nullable
    private FormController formController;
    private final AudioRecorder audioRecorder;
    private final SettingsProvider settingsProvider;

    public FormEntryMenuDelegate(AppCompatActivity activity, AnswersProvider answersProvider,
                                 FormIndexAnimationHandler formIndexAnimationHandler, FormSaveViewModel formSaveViewModel,
                                 FormEntryViewModel formEntryViewModel, AudioRecorder audioRecorder,
                                 BackgroundLocationViewModel backgroundLocationViewModel,
                                 BackgroundAudioViewModel backgroundAudioViewModel, SettingsProvider settingsProvider) {
        this.activity = activity;
        this.answersProvider = answersProvider;
        this.formIndexAnimationHandler = formIndexAnimationHandler;

        this.audioRecorder = audioRecorder;
        this.formEntryViewModel = formEntryViewModel;
        this.formSaveViewModel = formSaveViewModel;
        this.backgroundLocationViewModel = backgroundLocationViewModel;
        this.backgroundAudioViewModel = backgroundAudioViewModel;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void formLoaded(@NotNull FormController formController) {
        this.formController = formController;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.form_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean useability;

        useability = settingsProvider.getAdminSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_MID);

        menu.findItem(R.id.menu_save).setVisible(useability).setEnabled(useability);

        useability = settingsProvider.getAdminSettings().getBoolean(ProtectedProjectKeys.KEY_JUMP_TO);

        menu.findItem(R.id.menu_goto).setVisible(useability)
                .setEnabled(useability);

        useability = settingsProvider.getAdminSettings().getBoolean(ProtectedProjectKeys.KEY_CHANGE_LANGUAGE)
                && (formController != null)
                && formController.getLanguages() != null
                && formController.getLanguages().length > 1;

        menu.findItem(R.id.menu_languages).setVisible(useability)
                .setEnabled(useability);

        useability = settingsProvider.getAdminSettings().getBoolean(ProtectedProjectKeys.KEY_ACCESS_SETTINGS);

        menu.findItem(R.id.menu_preferences).setVisible(false)
                .setEnabled(false);

        if (formController != null && formController.currentFormCollectsBackgroundLocation()
                && new PlayServicesChecker().isGooglePlayServicesAvailable(activity)) {
            MenuItem backgroundLocation = menu.findItem(R.id.track_location);
            backgroundLocation.setVisible(true);
            backgroundLocation.setChecked(settingsProvider.getGeneralSettings().getBoolean(KEY_BACKGROUND_LOCATION));
        }

        menu.findItem(R.id.menu_add_repeat).setVisible(formEntryViewModel.canAddRepeat());
        menu.findItem(R.id.menu_record_audio).setVisible(formEntryViewModel.hasBackgroundRecording().getValue());
        menu.findItem(R.id.menu_record_audio).setChecked(backgroundAudioViewModel.isBackgroundRecordingEnabled().getValue());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_repeat) {
            if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording()) {
                DialogUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                formSaveViewModel.saveAnswersForScreen(answersProvider.getAnswers());
                formEntryViewModel.promptForNewRepeat();
                formIndexAnimationHandler.handle(formEntryViewModel.getCurrentIndex());
            }

            return true;
        } else if (item.getItemId() == R.id.menu_preferences) {
            if (audioRecorder.isRecording()) {
                DialogUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                Intent pref = new Intent(activity, ProjectPreferencesActivity.class);
                activity.startActivityForResult(pref, ApplicationConstants.RequestCodes.CHANGE_SETTINGS);
            }

            return true;
        } else if (item.getItemId() == R.id.track_location) {
            backgroundLocationViewModel.backgroundLocationPreferenceToggled(settingsProvider.getGeneralSettings());
            return true;
        } else if (item.getItemId() == R.id.menu_goto) {
            if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording()) {
                DialogUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                formSaveViewModel.saveAnswersForScreen(answersProvider.getAnswers());

                formEntryViewModel.openHierarchy();
                Intent i = new Intent(activity, FormHierarchyActivity.class);
                activity.startActivityForResult(i, ApplicationConstants.RequestCodes.HIERARCHY_ACTIVITY);
            }

            return true;
        } else if (item.getItemId() == R.id.menu_record_audio) {
            boolean enabled = !item.isChecked();

            if (!enabled) {
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.stop_recording_confirmation)
                        .setPositiveButton(R.string.disable_recording, (dialog, which) -> backgroundAudioViewModel.setBackgroundRecordingEnabled(false))
                        .setNegativeButton(R.string.cancel, null)
                        .create()
                        .show();
            } else {
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.background_audio_recording_enabled_explanation)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();

                backgroundAudioViewModel.setBackgroundRecordingEnabled(true);
            }

            return true;
        } else {
            return false;
        }
    }
}
