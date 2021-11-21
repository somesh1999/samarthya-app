package org.samarthya.collect.location

import android.app.Application
import org.samarthya.collect.androidshared.data.AppState
import org.samarthya.collect.androidshared.data.StateStore

class RobolectricApplication : Application(), StateStore {

    private val appState = AppState()

    override fun getState(): AppState {
        return appState
    }
}
