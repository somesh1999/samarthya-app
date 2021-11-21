package org.samarthya.collect.android.preferences.screens;

import android.os.Bundle;

import org.samarthya.collect.android.R;
import org.samarthya.collect.android.preferences.screens.BaseProjectPreferencesFragment;

public class CustomServerPathsPreferencesFragment extends BaseProjectPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.custom_server_paths_preferences, rootKey);
    }
}
