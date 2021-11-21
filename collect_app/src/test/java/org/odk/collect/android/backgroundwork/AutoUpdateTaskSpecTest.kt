package org.samarthya.collect.android.backgroundwork

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.samarthya.collect.analytics.Analytics
import org.samarthya.collect.android.backgroundwork.AutoUpdateTaskSpec.DATA_PROJECT_ID
import org.samarthya.collect.android.formmanagement.FormSourceProvider
import org.samarthya.collect.android.formmanagement.FormsUpdater
import org.samarthya.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.samarthya.collect.android.injection.config.AppDependencyModule
import org.samarthya.collect.android.notifications.Notifier
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.android.storage.StoragePathProvider
import org.samarthya.collect.android.support.CollectHelpers
import org.samarthya.collect.android.utilities.ChangeLockProvider
import org.samarthya.collect.android.utilities.FormsRepositoryProvider
import org.samarthya.collect.android.utilities.InstancesRepositoryProvider

@RunWith(AndroidJUnit4::class)
class AutoUpdateTaskSpecTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val formUpdateChecker = mock<FormsUpdater>()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesFormUpdateChecker(
                context: Context,
                notifier: Notifier,
                analytics: Analytics,
                storagePathProvider: StoragePathProvider,
                settingsProvider: SettingsProvider,
                formsRepositoryProvider: FormsRepositoryProvider,
                formSourceProvider: FormSourceProvider,
                syncStatusAppState: SyncStatusAppState,
                instancesRepositoryProvider: InstancesRepositoryProvider,
                changeLockProvider: ChangeLockProvider
            ): FormsUpdater? {
                return formUpdateChecker
            }
        })
    }

    @Test
    fun `calls checkForUpdates with project from tag`() {
        val autoUpdateTaskSpec = AutoUpdateTaskSpec()
        val task = autoUpdateTaskSpec.getTask(context, mapOf(DATA_PROJECT_ID to "projectId"))

        task.get()
        verify(formUpdateChecker).downloadUpdates("projectId")
    }
}
