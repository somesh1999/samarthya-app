package org.samarthya.collect.audiorecorder.support

import org.samarthya.collect.audiorecorder.recorder.Output
import org.samarthya.collect.audiorecorder.recorder.Recorder
import org.samarthya.collect.audiorecorder.recording.MicInUseException
import org.samarthya.collect.audiorecorder.recording.SetupException
import java.io.File

class FakeRecorder : Recorder {

    override var amplitude: Int = 0

    private var _paused = false
    val paused: Boolean
        get() {
            return _paused
        }

    var file: File? = null
        private set

    lateinit var output: Output

    private val _recordings = mutableListOf<Unit>()
    val recordings: List<Unit> = _recordings

    private var recording = false
    private var cancelled = false
    private var exception: Exception? = null

    override fun isRecording(): Boolean {
        return recording
    }

    fun wasCancelled(): Boolean {
        return cancelled
    }

    @Throws(SetupException::class, MicInUseException::class)
    override fun start(output: Output) {
        exception.let {
            if (it == null) {
                recording = true
                cancelled = false
                this.output = output
                _recordings.add(Unit)
                val newFile = File.createTempFile("recording", ".mp3")
                file = newFile
            } else {
                throw it
            }
        }
    }

    override fun pause() {
        _paused = true
    }

    override fun resume() {
        _paused = false
    }

    override fun stop(): File {
        recording = false
        return file!!
    }

    override fun cancel() {
        recording = false
        cancelled = true
    }

    fun failOnStart(exception: Exception?) {
        this.exception = exception
    }
}
