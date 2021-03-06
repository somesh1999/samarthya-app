package org.samarthya.collect.android.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.samarthya.collect.android.R
import org.samarthya.collect.android.application.Collect
import org.samarthya.collect.android.injection.config.AppDependencyModule
import org.samarthya.collect.android.projects.ManualProjectCreatorDialog
import org.samarthya.collect.android.projects.QrCodeProjectCreatorDialog
import org.samarthya.collect.android.support.CollectHelpers
import org.samarthya.collect.android.utilities.TranslationHandler.getString
import org.samarthya.collect.android.version.VersionInformation

@RunWith(AndroidJUnit4::class)
class FirstLaunchActivityTest {

    @Test
    fun `The QrCodeProjectCreatorDialog should be displayed after clicking on the 'Configure with QR code' button`() {
        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(R.string.configure_with_qr_code)).perform(click())
            assertThat(
                it.supportFragmentManager.findFragmentByTag(QrCodeProjectCreatorDialog::class.java.name),
                `is`(notNullValue())
            )
        }
    }

    @Test
    fun `The ManualProjectCreatorDialog should be displayed after clicking on the 'Configure manually' button`() {
        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(R.string.configure_manually)).perform(click())
            assertThat(
                it.supportFragmentManager.findFragmentByTag(ManualProjectCreatorDialog::class.java.name),
                `is`(notNullValue())
            )
        }
    }

    @Test
    fun `The ODK logo should be displayed`() {
        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withId(R.id.logo)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `The app name with its version should be displayed`() {
        val versionInformation = mock(VersionInformation::class.java)
        whenever(versionInformation.versionToDisplay).thenReturn("vfake")
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesVersionInformation(): VersionInformation {
                return versionInformation
            }
        })

        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            verify(versionInformation).versionToDisplay
            onView(
                withText(
                    getString(
                        ApplicationProvider.getApplicationContext<Collect>(),
                        R.string.collect_app_name
                    ) + " vfake"
                )
            ).check(matches(isDisplayed()))
        }
    }
}
