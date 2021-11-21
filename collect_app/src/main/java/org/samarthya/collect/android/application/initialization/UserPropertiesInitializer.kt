package org.samarthya.collect.android.application.initialization

import org.samarthya.collect.analytics.Analytics
import org.samarthya.collect.projects.ProjectsRepository

class UserPropertiesInitializer(private val analytics: Analytics, private val projectsRepository: ProjectsRepository) {

    fun initialize() {
        val projectsCount = projectsRepository.getAll().size
        analytics.setUserProperty("ProjectsCount", projectsCount.toString())
    }
}
