package org.samarthya.collect.android.application;

import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.analytics.AnalyticsEvents;
import org.samarthya.collect.android.backgroundwork.FormUpdateScheduler;
import org.samarthya.collect.android.configure.SettingsChangeHandler;
import org.samarthya.collect.android.logic.PropertyManager;
import org.samarthya.collect.android.preferences.source.SettingsProvider;

import static org.samarthya.collect.android.analytics.AnalyticsUtils.getServerHash;
import static org.samarthya.collect.android.analytics.AnalyticsUtils.logServerConfiguration;
import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_EXTERNAL_APP_RECORDING;
import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_FORM_UPDATE_MODE;
import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_PROTOCOL;
import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_SERVER_URL;

public class CollectSettingsChangeHandler implements SettingsChangeHandler {

    private final PropertyManager propertyManager;
    private final FormUpdateScheduler formUpdateScheduler;
    private final Analytics analytics;
    private final SettingsProvider settingsProvider;

    public CollectSettingsChangeHandler(PropertyManager propertyManager, FormUpdateScheduler formUpdateScheduler, Analytics analytics, SettingsProvider settingsProvider) {
        this.propertyManager = propertyManager;
        this.formUpdateScheduler = formUpdateScheduler;
        this.analytics = analytics;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void onSettingChanged(String projectId, Object newValue, String changedKey) {
        propertyManager.reload();

        if (changedKey.equals(KEY_FORM_UPDATE_MODE) || changedKey.equals(KEY_PERIODIC_FORM_UPDATES_CHECK) || changedKey.equals(KEY_PROTOCOL)) {
            formUpdateScheduler.scheduleUpdates(projectId);
        }

        if (changedKey.equals(KEY_EXTERNAL_APP_RECORDING) && !((Boolean) newValue)) {
            String serverHash = getServerHash(settingsProvider.getGeneralSettings(projectId));
            analytics.logServerEvent(AnalyticsEvents.INTERNAL_RECORDING_OPT_IN, serverHash);
        }

        if (changedKey.equals(KEY_SERVER_URL)) {
            logServerConfiguration(analytics, newValue.toString());
        }
    }
}
