package org.samarthya.collect.android.projects

import org.samarthya.collect.android.storage.StoragePathProvider
import org.samarthya.collect.android.utilities.FileUtils
import org.samarthya.collect.projects.Project
import org.samarthya.collect.projects.Project.Saved
import org.samarthya.collect.projects.ProjectsRepository

class ProjectImporter(
    private val storagePathProvider: StoragePathProvider,
    private val projectsRepository: ProjectsRepository
) {
    fun importNewProject(): Saved {
        val savedProject = projectsRepository.save(Project.New("", "", ""))
        createProjectDirs(savedProject)
        return savedProject
    }

    fun importNewProject(project: Project): Saved {
        val savedProject = projectsRepository.save(project)
        createProjectDirs(savedProject)
        return savedProject
    }

    private fun createProjectDirs(project: Saved) {
        storagePathProvider.getProjectDirPaths(project.uuid).forEach { FileUtils.createDir(it) }
    }
}
