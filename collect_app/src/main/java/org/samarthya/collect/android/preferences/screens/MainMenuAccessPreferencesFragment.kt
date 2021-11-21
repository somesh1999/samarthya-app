package org.samarthya.collect.android.preferences.screens

import android.os.Bundle
import androidx.preference.Preference
import org.samarthya.collect.android.R
import org.samarthya.collect.android.configure.SettingsUtils
import org.samarthya.collect.android.preferences.FormUpdateMode
import org.samarthya.collect.android.preferences.keys.ProtectedProjectKeys
import org.samarthya.collect.android.preferences.utilities.PreferencesUtils

class MainMenuAccessPreferencesFragment : BaseAdminPreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.main_menu_access_preferences, rootKey)

        findPreference<Preference>(ProtectedProjectKeys.KEY_EDIT_SAVED)!!.isEnabled =
            settingsProvider.getAdminSettings().getBoolean(ProtectedProjectKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM)

        val formUpdateMode = SettingsUtils.getFormUpdateMode(requireContext(), settingsProvider.getGeneralSettings())
        if (formUpdateMode == FormUpdateMode.MATCH_EXACTLY) {
            PreferencesUtils.displayDisabled(findPreference(ProtectedProjectKeys.KEY_GET_BLANK), false)
        }
    }
}
