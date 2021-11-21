package org.samarthya.collect.android.projects

import org.samarthya.collect.android.configure.SettingsImporter
import org.samarthya.collect.android.storage.StoragePathProvider
import org.samarthya.collect.android.utilities.StringUtils
import org.samarthya.collect.projects.ProjectsRepository
import timber.log.Timber
import java.io.File

class ProjectCreator(
    private val projectImporter: ProjectImporter,
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: SettingsImporter,
    private val storagePathProvider: StoragePathProvider
) {

    fun createNewProject(settingsJson: String): Boolean {
        val savedProject = projectImporter.importNewProject()

        val settingsImportedSuccessfully = settingsImporter.fromJSON(settingsJson, savedProject)

        return if (settingsImportedSuccessfully) {
            currentProjectProvider.setCurrentProject(savedProject.uuid)
            try {
                File((storagePathProvider.getProjectRootDirPath() + File.separator + currentProjectProvider.getCurrentProject().name)).createNewFile()
            } catch (e: Exception) {
                Timber.e(StringUtils.getFilenameError(currentProjectProvider.getCurrentProject().name))
            }
            true
        } else {
            projectsRepository.delete(savedProject.uuid)
            false
        }
    }
}
