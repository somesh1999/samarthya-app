package org.samarthya.collect.android.formentry;

import android.Manifest;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.actions.recordaudio.RecordAudioActions;
import org.javarosa.core.model.instance.TreeReference;
import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.analytics.AnalyticsEvents;
import org.samarthya.collect.android.formentry.RequiresFormController;
import org.samarthya.collect.android.formentry.audit.AuditEvent;
import org.samarthya.collect.android.formentry.audit.AuditEventLogger;
import org.samarthya.collect.android.javarosawrapper.FormController;
import org.samarthya.collect.android.permissions.PermissionsChecker;
import org.samarthya.collect.androidshared.livedata.MutableNonNullLiveData;
import org.samarthya.collect.androidshared.livedata.NonNullLiveData;
import org.samarthya.collect.audiorecorder.recorder.Output;
import org.samarthya.collect.audiorecorder.recording.AudioRecorder;
import org.samarthya.collect.audiorecorder.recording.RecordingSession;
import org.samarthya.collect.shared.Settings;
import org.samarthya.collect.utilities.Clock;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_BACKGROUND_RECORDING;

public class BackgroundAudioViewModel extends ViewModel implements RequiresFormController {

    private final AudioRecorder audioRecorder;
    private final Settings generalSettings;
    private final RecordAudioActionRegistry recordAudioActionRegistry;
    private final PermissionsChecker permissionsChecker;
    private final Clock clock;
    private final Analytics analytics;

    private final MutableNonNullLiveData<Boolean> isPermissionRequired = new MutableNonNullLiveData<>(false);
    private final MutableNonNullLiveData<Boolean> isBackgroundRecordingEnabled;

    // These fields handle storing record action details while we're granting permissions
    private final HashSet<TreeReference> tempTreeReferences = new HashSet<>();
    private String tempQuality;

    @Nullable
    private AuditEventLogger auditEventLogger;
    private FormController formController;

    public BackgroundAudioViewModel(AudioRecorder audioRecorder, Settings generalSettings, RecordAudioActionRegistry recordAudioActionRegistry, PermissionsChecker permissionsChecker, Clock clock, Analytics analytics) {
        this.audioRecorder = audioRecorder;
        this.generalSettings = generalSettings;
        this.recordAudioActionRegistry = recordAudioActionRegistry;
        this.permissionsChecker = permissionsChecker;
        this.clock = clock;
        this.analytics = analytics;

        this.recordAudioActionRegistry.register((treeReference, quality) -> {
            new Handler(Looper.getMainLooper()).post(() -> handleRecordAction(treeReference, quality));
        });

        isBackgroundRecordingEnabled = new MutableNonNullLiveData<>(generalSettings.getBoolean(KEY_BACKGROUND_RECORDING));
    }

    @Override
    public void formLoaded(@NonNull FormController formController) {
        this.formController = formController;
        this.auditEventLogger = formController.getAuditEventLogger();
    }

    @Override
    protected void onCleared() {
        recordAudioActionRegistry.unregister();
    }

    public LiveData<Boolean> isPermissionRequired() {
        return isPermissionRequired;
    }

    public NonNullLiveData<Boolean> isBackgroundRecordingEnabled() {
        return isBackgroundRecordingEnabled;
    }

    public void setBackgroundRecordingEnabled(boolean enabled) {
        if (enabled) {
            if (auditEventLogger != null) {
                auditEventLogger.logEvent(AuditEvent.AuditEventType.BACKGROUND_AUDIO_ENABLED, true, clock.getCurrentTime());
            }

            if (formController != null) {
                analytics.logFormEvent(AnalyticsEvents.BACKGROUND_AUDIO_ENABLED, formController.getCurrentFormIdentifierHash());
            }
        } else {
            audioRecorder.cleanUp();

            if (auditEventLogger != null) {
                auditEventLogger.logEvent(AuditEvent.AuditEventType.BACKGROUND_AUDIO_DISABLED, true, clock.getCurrentTime());
            }

            if (formController != null) {
                analytics.logFormEvent(AnalyticsEvents.BACKGROUND_AUDIO_DISABLED, formController.getCurrentFormIdentifierHash());
            }
        }

        generalSettings.save(KEY_BACKGROUND_RECORDING, enabled);
        isBackgroundRecordingEnabled.postValue(enabled);
    }

    public boolean isBackgroundRecording() {
        return audioRecorder.isRecording() && audioRecorder.getCurrentSession().getValue().getId() instanceof Set;
    }

    public void grantAudioPermission() {
        if (tempTreeReferences.isEmpty()) {
            throw new IllegalStateException("No TreeReferences to start recording with!");
        }

        isPermissionRequired.setValue(false);
        startBackgroundRecording(tempQuality, new HashSet<>(tempTreeReferences));

        tempTreeReferences.clear();
        tempQuality = null;
    }

    private void handleRecordAction(TreeReference treeReference, String quality) {
        if (isBackgroundRecordingEnabled.getValue()) {
            if (permissionsChecker.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                if (isBackgroundRecording()) {
                    RecordingSession session = audioRecorder.getCurrentSession().getValue();
                    HashSet<TreeReference> treeReferences = (HashSet<TreeReference>) session.getId();
                    treeReferences.add(treeReference);
                } else {
                    HashSet<TreeReference> treeReferences = new HashSet<>();
                    treeReferences.add(treeReference);

                    startBackgroundRecording(quality, treeReferences);
                }
            } else {
                isPermissionRequired.setValue(true);

                tempTreeReferences.add(treeReference);
                if (tempQuality == null) {
                    tempQuality = quality;
                }
            }
        }
    }

    private void startBackgroundRecording(String quality, HashSet<TreeReference> treeReferences) {
        Output output = Output.AMR;
        if ("low".equals(quality)) {
            output = Output.AAC_LOW;
        } else if ("normal".equals(quality)) {
            output = Output.AAC;
        }

        audioRecorder.start(treeReferences, output);
    }

    public interface RecordAudioActionRegistry {

        void register(BiConsumer<TreeReference, String> listener);

        void unregister();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final AudioRecorder audioRecorder;
        private final Settings generalSettings;
        private final PermissionsChecker permissionsChecker;
        private final Clock clock;
        private final Analytics analytics;

        @Inject
        public Factory(AudioRecorder audioRecorder, Settings generalSettings, PermissionsChecker permissionsChecker, Clock clock, Analytics analytics) {
            this.audioRecorder = audioRecorder;
            this.generalSettings = generalSettings;
            this.permissionsChecker = permissionsChecker;
            this.clock = clock;
            this.analytics = analytics;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            RecordAudioActionRegistry recordAudioActionRegistry = new RecordAudioActionRegistry() {
                @Override
                public void register(BiConsumer<TreeReference, String> listener) {
                    RecordAudioActions.setRecordAudioListener(listener::accept);
                }

                @Override
                public void unregister() {
                    RecordAudioActions.setRecordAudioListener(null);
                }
            };

            return (T) new BackgroundAudioViewModel(audioRecorder, generalSettings, recordAudioActionRegistry, permissionsChecker, clock, analytics);
        }
    }
}
