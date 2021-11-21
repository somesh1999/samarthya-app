package org.samarthya.collect.audiorecorder.recording

import android.app.Application
import org.samarthya.collect.audiorecorder.AudioRecorderDependencyComponentProvider
import org.samarthya.collect.audiorecorder.recording.internal.ForegroundServiceAudioRecorder
import org.samarthya.collect.audiorecorder.recording.internal.RecordingRepository
import javax.inject.Inject

open class AudioRecorderFactory(private val application: Application) {

    @Inject
    internal lateinit var recordingRepository: RecordingRepository

    open fun create(): AudioRecorder {
        val provider = application.applicationContext as AudioRecorderDependencyComponentProvider
        provider.audioRecorderDependencyComponent.inject(this)

        return ForegroundServiceAudioRecorder(application, recordingRepository)
    }
}
