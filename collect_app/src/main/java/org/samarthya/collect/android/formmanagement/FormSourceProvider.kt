package org.samarthya.collect.android.formmanagement

import org.samarthya.collect.android.openrosa.OpenRosaFormSource
import org.samarthya.collect.android.openrosa.OpenRosaHttpInterface
import org.samarthya.collect.android.openrosa.OpenRosaResponseParserImpl
import org.samarthya.collect.android.preferences.keys.ProjectKeys
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.android.utilities.WebCredentialsUtils
import org.samarthya.collect.forms.FormSource

class FormSourceProvider(
    private val settingsProvider: SettingsProvider,
    private val openRosaHttpInterface: OpenRosaHttpInterface
) {

    @JvmOverloads
    fun get(projectId: String? = null): FormSource {
        val generalSettings = settingsProvider.getGeneralSettings(projectId)

        val serverURL = generalSettings.getString(ProjectKeys.KEY_SERVER_URL)
        val formListPath = generalSettings.getString(ProjectKeys.KEY_FORMLIST_URL)

        return OpenRosaFormSource(
            serverURL,
            formListPath,
            openRosaHttpInterface,
            WebCredentialsUtils(generalSettings),
            OpenRosaResponseParserImpl()
        )
    }
}
