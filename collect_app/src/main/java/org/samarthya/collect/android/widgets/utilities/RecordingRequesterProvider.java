package org.samarthya.collect.android.widgets.utilities;

import org.javarosa.form.api.FormEntryPrompt;
import org.samarthya.collect.android.utilities.FormEntryPromptUtils;
import org.samarthya.collect.android.widgets.utilities.ExternalAppRecordingRequester;
import org.samarthya.collect.android.widgets.utilities.InternalRecordingRequester;
import org.samarthya.collect.android.widgets.utilities.RecordingRequester;

public class RecordingRequesterProvider {

    private final org.samarthya.collect.android.widgets.utilities.InternalRecordingRequester internalRecordingRequester;
    private final ExternalAppRecordingRequester externalAppRecordingRequester;

    public RecordingRequesterProvider(InternalRecordingRequester internalRecordingRequester, ExternalAppRecordingRequester externalAppRecordingRequester) {
        this.internalRecordingRequester = internalRecordingRequester;
        this.externalAppRecordingRequester = externalAppRecordingRequester;
    }

    public RecordingRequester create(FormEntryPrompt prompt, boolean externalRecorderPreferred) {
        String audioQuality = FormEntryPromptUtils.getAttributeValue(prompt, "quality");

        if (audioQuality != null && (audioQuality.equals("normal") || audioQuality.equals("voice-only") || audioQuality.equals("low"))) {
            return internalRecordingRequester;
        } else if (audioQuality != null && audioQuality.equals("external")) {
            return externalAppRecordingRequester;
        } else if (externalRecorderPreferred) {
            return externalAppRecordingRequester;
        } else {
            return internalRecordingRequester;
        }
    }
}
