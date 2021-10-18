package org.odk.collect.android.activities

import android.os.Bundle
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.databinding.FirstLaunchLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.projects.ManualProjectCreatorDialog
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.projects.QrCodeProjectCreatorDialog
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.projects.Project
import javax.inject.Inject
import android.content.Intent
import android.os.Handler
import org.odk.collect.android.utilities.ToastUtils


class FirstLaunchActivity : CollectAbstractActivity() {

    @Inject
    lateinit var projectImporter: ProjectImporter

    @Inject
    lateinit var versionInformation: VersionInformation

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    private lateinit var binding: FirstLaunchLayoutBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Collect_Light)

        binding = FirstLaunchLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DaggerUtils.getComponent(this).inject(this)

        Handler().postDelayed(Runnable { // This method will be executed once the timer is over
            Analytics.log(AnalyticsEvents.TRY_DEMO)
            projectImporter.importNewProject(Project.DEMO_PROJECT)
            currentProjectProvider.setCurrentProject(Project.DEMO_PROJECT_ID)
            //ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
            ActivityUtils.startActivityAndCloseAllOthers(this, LaunchActivity::class.java)
        }, 1000)

        /*binding.configureViaQrButton.setOnClickListener {
            DialogUtils.showIfNotShowing(QrCodeProjectCreatorDialog::class.java, supportFragmentManager)
        }

        binding.configureManuallyButton.setOnClickListener {
            DialogUtils.showIfNotShowing(ManualProjectCreatorDialog::class.java, supportFragmentManager)
        }*/

        /*binding.appName.text = String.format(
            "%s %s",
            getString(R.string.collect_app_name),
            versionInformation.versionToDisplay
        )*/

        /*binding.configureLater.setOnClickListener {
            Analytics.log(AnalyticsEvents.TRY_DEMO)

            projectImporter.importNewProject(Project.DEMO_PROJECT)
            currentProjectProvider.setCurrentProject(Project.DEMO_PROJECT_ID)

            //ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
            ActivityUtils.startActivityAndCloseAllOthers(this, LaunchActivity::class.java)
        }*/
    }
}
