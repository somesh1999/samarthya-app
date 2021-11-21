package org.samarthya.collect.android.activities.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.samarthya.collect.android.preferences.keys.ProjectKeys
import org.samarthya.collect.android.utilities.FileUtils
import org.samarthya.collect.android.utilities.ScreenUtils
import org.samarthya.collect.projects.ProjectsRepository
import org.samarthya.collect.shared.Settings
import java.io.File

class SplashScreenViewModel(
    private val generalSettings: Settings,
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    val shouldDisplaySplashScreen
        get() = generalSettings.getBoolean(ProjectKeys.KEY_SHOW_SPLASH)

    val splashScreenLogoFile
        get() = File(generalSettings.getString(ProjectKeys.KEY_SPLASH_PATH) ?: "")

    val scaledSplashScreenLogoBitmap: Bitmap
        get() = FileUtils.getBitmapScaledToDisplay(splashScreenLogoFile, ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth())

    val doesLogoFileExist
        get() = splashScreenLogoFile.exists()

    val shouldFirstLaunchScreenBeDisplayed
        get() = projectsRepository.getAll().isEmpty()

    open class Factory(
        private val generalSettings: Settings,
        private val projectsRepository: ProjectsRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SplashScreenViewModel(generalSettings, projectsRepository) as T
        }
    }
}
