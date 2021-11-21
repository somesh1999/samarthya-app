package org.samarthya.collect.android.utilities;

import org.junit.Test;
import org.samarthya.collect.shared.Settings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.samarthya.collect.android.preferences.keys.ProtectedProjectKeys.KEY_ADMIN_PW;

public class AdminPasswordProviderTest {

    @Test
    public void when_adminPassHasEmptyValue_should_isAdminPasswordSetReturnFalse() {
        Settings adminSharedPreferences = mock(Settings.class);
        when(adminSharedPreferences.getString(KEY_ADMIN_PW)).thenReturn("");

        AdminPasswordProvider adminPasswordProvider = new AdminPasswordProvider(adminSharedPreferences);
        assertThat(adminPasswordProvider.isAdminPasswordSet(), is(false));
    }

    @Test
    public void when_adminPassHasNullValue_should_isAdminPasswordSetReturnFalse() {
        Settings adminSharedPreferences = mock(Settings.class);
        when(adminSharedPreferences.getString(KEY_ADMIN_PW)).thenReturn(null);

        AdminPasswordProvider adminPasswordProvider = new AdminPasswordProvider(adminSharedPreferences);
        assertThat(adminPasswordProvider.isAdminPasswordSet(), is(false));
    }

    @Test
    public void when_adminPassIsSetProperly_should_isAdminPasswordSetReturnTrue() {
        Settings adminSharedPreferences = mock(Settings.class);
        when(adminSharedPreferences.getString(KEY_ADMIN_PW)).thenReturn("123");

        AdminPasswordProvider adminPasswordProvider = new AdminPasswordProvider(adminSharedPreferences);
        assertThat(adminPasswordProvider.isAdminPasswordSet(), is(true));
    }

    @Test
    public void when_adminPassHasEmptyValue_should_getAdminPasswordReturnEmptyString() {
        Settings adminSharedPreferences = mock(Settings.class);
        when(adminSharedPreferences.getString(KEY_ADMIN_PW)).thenReturn("");

        AdminPasswordProvider adminPasswordProvider = new AdminPasswordProvider(adminSharedPreferences);
        assertThat(adminPasswordProvider.getAdminPassword(), is(""));
    }

    @Test
    public void when_adminPassHasNullValue_should_getAdminPasswordReturnNull() {
        Settings adminSharedPreferences = mock(Settings.class);
        when(adminSharedPreferences.getString(KEY_ADMIN_PW)).thenReturn(null);

        AdminPasswordProvider adminPasswordProvider = new AdminPasswordProvider(adminSharedPreferences);
        assertThat(adminPasswordProvider.getAdminPassword(), is(nullValue()));
    }

    @Test
    public void when_adminPassIsSetProperly_should_getAdminPasswordReturnCorrectValue() {
        Settings adminSharedPreferences = mock(Settings.class);
        when(adminSharedPreferences.getString(KEY_ADMIN_PW)).thenReturn("123");

        AdminPasswordProvider adminPasswordProvider = new AdminPasswordProvider(adminSharedPreferences);
        assertThat(adminPasswordProvider.getAdminPassword(), is("123"));
    }
}
