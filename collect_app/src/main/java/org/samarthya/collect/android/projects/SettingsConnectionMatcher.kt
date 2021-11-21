package org.samarthya.collect.android.projects

import org.json.JSONException
import org.json.JSONObject
import org.samarthya.collect.android.configure.qr.AppConfigurationKeys
import org.samarthya.collect.android.preferences.keys.ProjectKeys
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.projects.ProjectsRepository

class SettingsConnectionMatcher(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider
) {

    fun getProjectWithMatchingConnection(settingsJson: String): String? {
        try {
            val jsonObject = JSONObject(settingsJson)
            val jsonSettings = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL)
            val jsonProtocol = try { jsonSettings.get(ProjectKeys.KEY_PROTOCOL) } catch (e: JSONException) { ProjectKeys.PROTOCOL_SERVER }

            val jsonUrl = try { jsonSettings.get(ProjectKeys.KEY_SERVER_URL) } catch (e: JSONException) { "" }
            val jsonUsername = try { jsonSettings.get(ProjectKeys.KEY_USERNAME) } catch (e: JSONException) { "" }

            val jsonGoogleAccount = try { jsonSettings.get(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT) } catch (e: JSONException) { "" }

            projectsRepository.getAll().forEach {
                val projectSettings = settingsProvider.getGeneralSettings(it.uuid)
                val projectProtocol = projectSettings.getString(ProjectKeys.KEY_PROTOCOL)
                val projectUrl = projectSettings.getString(ProjectKeys.KEY_SERVER_URL)
                val projectUsername = projectSettings.getString(ProjectKeys.KEY_USERNAME)
                val projectGoogleAccount = projectSettings.getString(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT)

                if (jsonProtocol.equals(projectProtocol) && jsonProtocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
                    if (jsonGoogleAccount.equals(projectGoogleAccount)) {
                        return it.uuid
                    }
                } else {
                    if (jsonUrl.equals(projectUrl) && jsonUsername.equals(projectUsername)) {
                        return it.uuid
                    }
                }
            }
        } catch (e: JSONException) {
            return null
        }
        return null
    }
}
