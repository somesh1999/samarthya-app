package org.samarthya.collect.android.application.initialization;

import org.samarthya.collect.shared.Settings;

public interface SettingsMigrator {

    void migrate(Settings generalSettings, Settings adminSettings);
}
