package org.samarthya.collect.android.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.samarthya.collect.analytics.Analytics
import org.samarthya.collect.android.analytics.AnalyticsEvents
import org.samarthya.collect.android.application.initialization.AnalyticsInitializer
import org.samarthya.collect.android.projects.CurrentProjectProvider
import org.samarthya.collect.androidshared.livedata.MutableNonNullLiveData
import org.samarthya.collect.androidshared.livedata.NonNullLiveData
import org.samarthya.collect.projects.Project

class CurrentProjectViewModel(
    private val currentProjectProvider: CurrentProjectProvider,
    private val analyticsInitializer: AnalyticsInitializer
) :
    ViewModel() {

    private val _currentProject = MutableNonNullLiveData(currentProjectProvider.getCurrentProject())
    val currentProject: NonNullLiveData<Project.Saved> = _currentProject

    fun setCurrentProject(project: Project.Saved) {
        currentProjectProvider.setCurrentProject(project.uuid)
        Analytics.log(AnalyticsEvents.SWITCH_PROJECT)
        analyticsInitializer.initialize()
        refresh()
    }

    fun refresh() {
        if (currentProject.value != currentProjectProvider.getCurrentProject()) {
            _currentProject.postValue(currentProjectProvider.getCurrentProject())
        }
    }

    open class Factory constructor(private val currentProjectProvider: CurrentProjectProvider, private val analyticsInitializer: AnalyticsInitializer) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CurrentProjectViewModel(currentProjectProvider, analyticsInitializer) as T
        }
    }
}
