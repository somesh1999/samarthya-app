package org.samarthya.collect.android.utilities;

import org.junit.Test;
import org.samarthya.collect.android.logic.PropertyManager;
import org.samarthya.collect.android.preferences.keys.ProjectKeys;
import org.samarthya.collect.shared.Settings;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WebCredentialsUtilsTest {

    @Test
    public void saveCredentialsPreferencesMethod_shouldSaveNewCredentialsAndReloadPropertyManager() {
        Settings generalSettings = mock(Settings.class);
        WebCredentialsUtils webCredentialsUtils = new WebCredentialsUtils(generalSettings);
        PropertyManager propertyManager = mock(PropertyManager.class);

        webCredentialsUtils.saveCredentialsPreferences("username", "password", propertyManager);

        verify(generalSettings, times(1)).save(ProjectKeys.KEY_USERNAME, "username");
        verify(generalSettings, times(1)).save(ProjectKeys.KEY_PASSWORD, "password");
        verify(propertyManager, times(1)).reload();
    }
}
