package org.samarthya.collect.android.configure;

public interface SettingsChangeHandler {
    void onSettingChanged(String projectId, Object newValue, String changedKey);
}
