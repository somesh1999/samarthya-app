package org.samarthya.collect.android.backgroundwork

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.samarthya.collect.analytics.Analytics
import org.samarthya.collect.android.TestSettingsProvider
import org.samarthya.collect.android.formmanagement.InstancesAppState
import org.samarthya.collect.android.gdrive.GoogleAccountsManager
import org.samarthya.collect.android.gdrive.GoogleApiProvider
import org.samarthya.collect.android.injection.config.AppDependencyModule
import org.samarthya.collect.android.instancemanagement.InstanceAutoSender
import org.samarthya.collect.android.notifications.Notifier
import org.samarthya.collect.android.permissions.PermissionsProvider
import org.samarthya.collect.android.preferences.keys.ProjectKeys
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.android.support.CollectHelpers
import org.samarthya.collect.android.utilities.ChangeLockProvider
import org.samarthya.collect.android.utilities.FormsRepositoryProvider
import org.samarthya.collect.android.utilities.InstancesRepositoryProvider
import org.samarthya.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AutoSendTaskSpecTest {

    private val instanceAutoSender = mock<InstanceAutoSender>()

    private lateinit var projectId: String

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesInstanceAutoSender(
                context: Context,
                changeLockProvider: ChangeLockProvider?,
                notifier: Notifier,
                analytics: Analytics,
                formsRepositoryProvider: FormsRepositoryProvider,
                instancesRepositoryProvider: InstancesRepositoryProvider,
                googleAccountsManager: GoogleAccountsManager,
                googleApiProvider: GoogleApiProvider,
                permissionsProvider: PermissionsProvider,
                settingsProvider: SettingsProvider,
                instancesAppState: InstancesAppState
            ): InstanceAutoSender {
                return instanceAutoSender
            }
        })

        RobolectricHelpers.mountExternalStorage()
        projectId = CollectHelpers.setupDemoProject()
        TestSettingsProvider.getGeneralSettings(projectId)
            .save(ProjectKeys.KEY_AUTOSEND, "wifi_and_cellular")
    }

    @Test
    fun `passes project id`() {
        val inputData = mapOf(AutoSendTaskSpec.DATA_PROJECT_ID to projectId)
        AutoSendTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData).get()
        verify(instanceAutoSender).autoSendInstances(projectId)
    }
}
