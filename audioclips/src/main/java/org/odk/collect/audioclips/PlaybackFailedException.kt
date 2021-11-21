package org.samarthya.collect.audioclips

data class PlaybackFailedException(val uRI: String, val exceptionMsg: Int) : Exception()
