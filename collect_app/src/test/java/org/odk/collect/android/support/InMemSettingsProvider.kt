package org.samarthya.collect.android.support

import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.shared.Settings
import org.samarthya.collect.testshared.InMemSettings

class InMemSettingsProvider : SettingsProvider {

    private val metaSettings = InMemSettings()
    private val settings = mutableMapOf<String?, InMemSettings>()

    override fun getMetaSettings(): Settings {
        return metaSettings
    }

    override fun getGeneralSettings(projectId: String?): Settings {
        return settings.getOrPut("general:$projectId") { InMemSettings() }
    }

    override fun getAdminSettings(projectId: String?): Settings {
        return settings.getOrPut("admin:$projectId") { InMemSettings() }
    }

    override fun clearAll() {
        settings.values.forEach { it.clear() }
        settings.clear()
        metaSettings.clear()
    }
}
