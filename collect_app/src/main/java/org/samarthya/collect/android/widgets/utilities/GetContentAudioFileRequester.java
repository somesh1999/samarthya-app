package org.samarthya.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.javarosa.form.api.FormEntryPrompt;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.analytics.AnalyticsEvents;
import org.samarthya.collect.android.formentry.FormEntryViewModel;
import org.samarthya.collect.android.utilities.ActivityAvailability;
import org.samarthya.collect.android.utilities.ApplicationConstants;
import org.samarthya.collect.android.widgets.utilities.AudioFileRequester;
import org.samarthya.collect.android.widgets.utilities.WaitingForDataRegistry;

public class GetContentAudioFileRequester implements AudioFileRequester {

    private final Activity activity;
    private final ActivityAvailability activityAvailability;
    private final org.samarthya.collect.android.widgets.utilities.WaitingForDataRegistry waitingForDataRegistry;
    private final FormEntryViewModel formEntryViewModel;

    public GetContentAudioFileRequester(Activity activity, ActivityAvailability activityAvailability, WaitingForDataRegistry waitingForDataRegistry, FormEntryViewModel formEntryViewModel) {
        this.activity = activity;
        this.activityAvailability = activityAvailability;
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.formEntryViewModel = formEntryViewModel;
    }

    @Override
    public void requestFile(FormEntryPrompt prompt) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");

        if (activityAvailability.isActivityAvailable(intent)) {
            waitingForDataRegistry.waitForData(prompt.getIndex());
            activity.startActivityForResult(intent, ApplicationConstants.RequestCodes.AUDIO_CHOOSER);
        } else {
            Toast.makeText(activity, activity.getString(R.string.activity_not_found, activity.getString(R.string.choose_sound)), Toast.LENGTH_SHORT).show();
            waitingForDataRegistry.cancelWaitingForData();
        }

        formEntryViewModel.logFormEvent(AnalyticsEvents.AUDIO_RECORDING_CHOOSE);
    }
}
