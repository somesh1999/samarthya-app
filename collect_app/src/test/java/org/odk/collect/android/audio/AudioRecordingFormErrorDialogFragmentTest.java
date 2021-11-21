package org.samarthya.collect.android.audio;

import android.app.Application;
import android.content.DialogInterface;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.injection.config.AppDependencyModule;
import org.samarthya.collect.android.support.CollectHelpers;
import org.samarthya.collect.testshared.RobolectricHelpers;
import org.samarthya.collect.audiorecorder.recording.AudioRecorder;
import org.samarthya.collect.audiorecorder.testsupport.StubAudioRecorder;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class AudioRecordingFormErrorDialogFragmentTest {

    private StubAudioRecorder audioRecorder;

    @Before
    public void setup() throws Exception {
        File stubRecording = File.createTempFile("test", ".m4a");
        stubRecording.deleteOnExit();
        audioRecorder = new StubAudioRecorder(stubRecording.getAbsolutePath());
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public AudioRecorder providesAudioRecorder(Application application) {
                return audioRecorder;
            }
        });
    }

    @Test
    public void clickingOK_dismissesDialog() {
        FragmentScenario<AudioRecordingErrorDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(AudioRecordingErrorDialogFragment.class, R.style.Theme_Collect_Light);
        scenario.onFragment(f -> {
            AlertDialog dialog = (AlertDialog) f.getDialog();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            assertThat(button.getText(), is(f.getString(R.string.ok)));

            button.performClick();
            RobolectricHelpers.runLooper();
            assertThat(dialog.isShowing(), is(false));
        });
    }

    @Test
    public void onDismiss_consumesConsumable() {
        FragmentScenario<AudioRecordingErrorDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(AudioRecordingErrorDialogFragment.class, R.style.Theme_Collect_Light);
        scenario.onFragment(DialogFragment::dismiss);
        assertThat(audioRecorder.failedToStart().getValue().isConsumed(), is(true));
    }
}
