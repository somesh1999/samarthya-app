package org.samarthya.collect.android.application.initialization

import org.samarthya.collect.android.application.initialization.upgrade.Upgrade
import org.samarthya.collect.android.backgroundwork.FormUpdateScheduler
import org.samarthya.collect.async.Scheduler
import org.samarthya.collect.projects.ProjectsRepository

class FormUpdatesUpgrade(
    private val scheduler: Scheduler,
    private val projectsRepository: ProjectsRepository,
    private val formUpdateScheduler: FormUpdateScheduler
) : Upgrade {

    override fun key(): String? {
        return null
    }

    override fun run() {
        scheduler.cancelAllDeferred()

        projectsRepository.getAll().forEach {
            formUpdateScheduler.scheduleUpdates(it.uuid)
        }
    }
}
