package org.samarthya.collect.android.application.initialization

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.samarthya.collect.android.backgroundwork.FormUpdateScheduler
import org.samarthya.collect.async.Scheduler
import org.samarthya.collect.projects.InMemProjectsRepository
import org.samarthya.collect.projects.Project

class FormUpdatesUpgradeTest {

    @Test
    fun `cancels all existing background jobs`() {
        val scheduler = mock<Scheduler>()
        val formUpdatesUpgrade = FormUpdatesUpgrade(scheduler, InMemProjectsRepository(), mock())

        formUpdatesUpgrade.run()
        verify(scheduler).cancelAllDeferred()
    }

    @Test
    fun `schedules updates for all projects`() {
        val projectsRepository = InMemProjectsRepository()
        val project1 = projectsRepository.save(Project.New("1", "1", "#ffffff"))
        val project2 = projectsRepository.save(Project.New("2", "2", "#ffffff"))

        val formUpdateScheduler = mock<FormUpdateScheduler>()
        val formUpdatesUpgrade = FormUpdatesUpgrade(mock(), projectsRepository, formUpdateScheduler)

        formUpdatesUpgrade.run()
        verify(formUpdateScheduler).scheduleUpdates(project1.uuid)
        verify(formUpdateScheduler).scheduleUpdates(project2.uuid)
    }
}
