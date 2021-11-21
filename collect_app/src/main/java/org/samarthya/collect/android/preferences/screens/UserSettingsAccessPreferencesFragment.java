package org.samarthya.collect.android.preferences.screens;

import android.os.Bundle;

import org.samarthya.collect.android.R;
import org.samarthya.collect.android.preferences.screens.BaseAdminPreferencesFragment;

public class UserSettingsAccessPreferencesFragment extends BaseAdminPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.user_settings_access_preferences, rootKey);
    }
}
