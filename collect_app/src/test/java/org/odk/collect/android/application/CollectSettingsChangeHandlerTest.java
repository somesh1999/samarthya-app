package org.samarthya.collect.android.application;

import org.junit.Test;
import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.backgroundwork.FormUpdateScheduler;
import org.samarthya.collect.android.logic.PropertyManager;
import org.samarthya.collect.android.preferences.keys.ProjectKeys;
import org.samarthya.collect.android.preferences.source.SettingsProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class CollectSettingsChangeHandlerTest {

    private final PropertyManager propertyManager = mock(PropertyManager.class);
    private final FormUpdateScheduler formUpdateScheduler = mock(FormUpdateScheduler.class);

    CollectSettingsChangeHandler handler = new CollectSettingsChangeHandler(propertyManager, formUpdateScheduler, mock(Analytics.class), mock(SettingsProvider.class));

    @Test
    public void updatesPropertyManager() {
        handler.onSettingChanged("projectId", "anything", "blah");
        verify(propertyManager).reload();
    }

    @Test
    public void doesNotDoAnythingElse() {
        handler.onSettingChanged("projectId", "anything", "blah");
        verifyNoInteractions(formUpdateScheduler);
    }

    @Test
    public void whenChangedKeyIsFormUpdateMode_schedulesUpdates() {
        handler.onSettingChanged("projectId", "anything", ProjectKeys.KEY_FORM_UPDATE_MODE);
        verify(formUpdateScheduler).scheduleUpdates("projectId");
    }

    @Test
    public void whenChangedKeyIsPeriodicUpdatesCheck_schedulesUpdates() {
        handler.onSettingChanged("projectId", "anything", ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK);
        verify(formUpdateScheduler).scheduleUpdates("projectId");
    }

    @Test
    public void whenChangedKeyIsProtocol_schedulesUpdates() {
        handler.onSettingChanged("projectId", "anything", ProjectKeys.KEY_PROTOCOL);
        verify(formUpdateScheduler).scheduleUpdates("projectId");
    }
}
