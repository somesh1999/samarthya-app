package org.samarthya.collect.projects.support

import android.app.Application
import org.samarthya.collect.projects.DaggerProjectsDependencyComponent
import org.samarthya.collect.projects.ProjectsDependencyComponent
import org.samarthya.collect.projects.ProjectsDependencyComponentProvider

class RobolectricApplication : Application(), ProjectsDependencyComponentProvider {

    override lateinit var projectsDependencyComponent: ProjectsDependencyComponent

    override fun onCreate() {
        super.onCreate()
        projectsDependencyComponent = DaggerProjectsDependencyComponent.builder().build()
    }
}
