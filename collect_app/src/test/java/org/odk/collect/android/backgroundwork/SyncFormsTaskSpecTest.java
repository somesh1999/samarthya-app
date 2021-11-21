package org.samarthya.collect.android.backgroundwork;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.formmanagement.FormSourceProvider;
import org.samarthya.collect.android.formmanagement.FormsUpdater;
import org.samarthya.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.samarthya.collect.android.injection.config.AppDependencyModule;
import org.samarthya.collect.android.notifications.Notifier;
import org.samarthya.collect.android.preferences.source.SettingsProvider;
import org.samarthya.collect.android.storage.StoragePathProvider;
import org.samarthya.collect.android.support.CollectHelpers;
import org.samarthya.collect.android.utilities.ChangeLockProvider;
import org.samarthya.collect.android.utilities.FormsRepositoryProvider;
import org.samarthya.collect.android.utilities.InstancesRepositoryProvider;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class SyncFormsTaskSpecTest {

    private final FormsUpdater formsUpdater = mock(FormsUpdater.class);

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public FormsUpdater providesFormUpdateChecker(Context context, Notifier notifier, Analytics analytics, StoragePathProvider storagePathProvider, SettingsProvider settingsProvider, FormsRepositoryProvider formsRepositoryProvider, FormSourceProvider formSourceProvider, SyncStatusAppState syncStatusAppState, InstancesRepositoryProvider instancesRepositoryProvider, ChangeLockProvider changeLockProvider) {
                return formsUpdater;
            }
        });
    }

    @Test
    public void callsSynchronizeWithProjectId() {
        HashMap<String, String> inputData = new HashMap<>();
        inputData.put(SyncFormsTaskSpec.DATA_PROJECT_ID, "projectId");

        new SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData).get();
        verify(formsUpdater).matchFormsWithServer("projectId");
    }
}
