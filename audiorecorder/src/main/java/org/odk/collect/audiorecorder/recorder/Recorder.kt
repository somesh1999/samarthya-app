package org.samarthya.collect.audiorecorder.recorder

import org.samarthya.collect.audiorecorder.recording.MicInUseException
import org.samarthya.collect.audiorecorder.recording.SetupException
import java.io.File

interface Recorder {

    @Throws(SetupException::class, MicInUseException::class)
    fun start(output: Output)
    fun pause()
    fun resume()
    fun stop(): File
    fun cancel()

    val amplitude: Int
    fun isRecording(): Boolean
}

enum class Output {
    AMR,
    AAC,
    AAC_LOW
}
