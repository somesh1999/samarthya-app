package org.samarthya.collect.android.preferences.dialogs

import android.content.Context
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.samarthya.collect.android.injection.config.AppDependencyModule
import org.samarthya.collect.android.preferences.ProjectPreferencesViewModel
import org.samarthya.collect.android.preferences.keys.ProtectedProjectKeys
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.android.support.CollectHelpers
import org.samarthya.collect.android.support.InMemSettingsProvider
import org.samarthya.collect.android.utilities.AdminPasswordProvider
import org.samarthya.collect.fragmentstest.DialogFragmentTest
import org.samarthya.collect.testshared.RobolectricHelpers
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ChangeAdminPasswordDialogTest {

    private val settingsProvider = InMemSettingsProvider()
    private val projectPreferencesViewModel = mock<ProjectPreferencesViewModel>()

    @Inject
    lateinit var factory: ProjectPreferencesViewModel.Factory

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesSettingsProvider(context: Context?): SettingsProvider {
                return settingsProvider
            }

            override fun providesProjectPreferencesViewModel(adminPasswordProvider: AdminPasswordProvider): ProjectPreferencesViewModel.Factory {
                return object : ProjectPreferencesViewModel.Factory(adminPasswordProvider) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return projectPreferencesViewModel as T
                    }
                }
            }
        })
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on 'OK'`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on 'CANCEL'`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, Matchers.`is`(true))
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `Setting password and accepting updates the password in settings`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            settingsProvider.getAdminSettings().save(ProtectedProjectKeys.KEY_ADMIN_PW, "")
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(settingsProvider.getAdminSettings().getString(ProtectedProjectKeys.KEY_ADMIN_PW), `is`("password"))
        }
    }

    @Test
    fun `Setting password and canceling does not update the password in settings`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            settingsProvider.getAdminSettings().save(ProtectedProjectKeys.KEY_ADMIN_PW, "")
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(settingsProvider.getAdminSettings().getString(ProtectedProjectKeys.KEY_ADMIN_PW), `is`(""))
        }
    }

    @Test
    fun `Setting password sets Unlocked state in view model`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verify(projectPreferencesViewModel).setStateUnlocked()
            verifyNoMoreInteractions(projectPreferencesViewModel)
        }
    }

    @Test
    fun `Removing password sets NotProtected state in view model`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verify(projectPreferencesViewModel).setStateNotProtected()
            verifyNoMoreInteractions(projectPreferencesViewModel)
        }
    }

    @Test
    fun `When screen is rotated password and checkbox value is retained`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("password")
            it.binding.checkBox2.performClick()
            scenario.recreate()
            assertThat(it.binding.pwdField.text.toString(), `is`("password"))
            assertThat(it.binding.checkBox2.isChecked, `is`(true))
        }
    }

    @Test
    fun `'Show password' displays and hides password`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.checkBox2.performClick()
            assertThat(it.binding.pwdField.inputType, `is`(InputType.TYPE_TEXT_VARIATION_PASSWORD))
            it.binding.checkBox2.performClick()
            assertThat(it.binding.pwdField.inputType, `is`(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        }
    }
}
