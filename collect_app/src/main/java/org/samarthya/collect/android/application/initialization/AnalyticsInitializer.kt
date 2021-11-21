package org.samarthya.collect.android.application.initialization

import org.samarthya.collect.analytics.Analytics
import org.samarthya.collect.android.preferences.keys.ProjectKeys
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.android.version.VersionInformation

class AnalyticsInitializer(
    private val analytics: Analytics,
    private val versionInformation: VersionInformation,
    private val settingsProvider: SettingsProvider
) {

    fun initialize() {
        if (versionInformation.isBeta) {
            analytics.setAnalyticsCollectionEnabled(true)
        } else {
            val analyticsEnabled = settingsProvider.getGeneralSettings().getBoolean(ProjectKeys.KEY_ANALYTICS)
            analytics.setAnalyticsCollectionEnabled(analyticsEnabled)
        }

        Analytics.setInstance(analytics)
    }
}
