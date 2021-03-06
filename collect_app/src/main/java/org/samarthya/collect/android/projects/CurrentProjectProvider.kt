package org.samarthya.collect.android.projects

import org.samarthya.collect.android.preferences.keys.MetaKeys
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.projects.Project
import org.samarthya.collect.projects.ProjectsRepository
import java.lang.IllegalStateException

class CurrentProjectProvider(private val settingsProvider: SettingsProvider, private val projectsRepository: ProjectsRepository) {

    fun getCurrentProject(): Project.Saved {
        val currentProjectId = getCurrentProjectId()

        if (currentProjectId != null) {
            val currentProject = projectsRepository.get(currentProjectId)

            if (currentProject != null) {
                return currentProject
            } else {
                throw IllegalStateException("Current project does not exist!")
            }
        } else {
            throw IllegalStateException("No current project!")
        }
    }

    fun setCurrentProject(projectId: String) {
        settingsProvider.getMetaSettings().save(MetaKeys.CURRENT_PROJECT_ID, projectId)
    }

    private fun getCurrentProjectId(): String? {
        return settingsProvider.getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID)
    }
}
