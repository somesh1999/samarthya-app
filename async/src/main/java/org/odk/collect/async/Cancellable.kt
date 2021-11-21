package org.samarthya.collect.async

interface Cancellable {
    fun cancel(): Boolean
}
