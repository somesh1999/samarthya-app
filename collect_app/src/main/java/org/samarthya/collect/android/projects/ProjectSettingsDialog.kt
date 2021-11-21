package org.samarthya.collect.android.projects

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import org.samarthya.collect.android.R
import org.samarthya.collect.android.activities.AboutActivity
import org.samarthya.collect.android.activities.ActivityUtils
import org.samarthya.collect.android.activities.MainMenuActivity
import org.samarthya.collect.android.activities.viewmodels.CurrentProjectViewModel
import org.samarthya.collect.android.databinding.ProjectSettingsDialogLayoutBinding
import org.samarthya.collect.android.injection.DaggerUtils
import org.samarthya.collect.android.preferences.screens.ProjectPreferencesActivity
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.android.utilities.DialogUtils
import org.samarthya.collect.android.utilities.ToastUtils
import org.samarthya.collect.projects.Project
import org.samarthya.collect.projects.ProjectsRepository
import javax.inject.Inject

class ProjectSettingsDialog : DialogFragment() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var currentProjectViewModelFactory: CurrentProjectViewModel.Factory

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var binding: ProjectSettingsDialogLayoutBinding

    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        currentProjectViewModel = ViewModelProvider(
            requireActivity(),
            currentProjectViewModelFactory
        )[CurrentProjectViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = ProjectSettingsDialogLayoutBinding.inflate(LayoutInflater.from(context))

        currentProjectViewModel.currentProject.observe(this) { project ->
            binding.currentProject.setupView(project, settingsProvider.getGeneralSettings())
            binding.currentProject.contentDescription =
                getString(R.string.using_project, project.name)
            inflateListOfInActiveProjects(requireContext(), project)
        }

        binding.closeIcon.setOnClickListener {
            dismiss()
        }

        binding.generalSettingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), ProjectPreferencesActivity::class.java))
            dismiss()
        }

        binding.addProjectButton.setOnClickListener {
            DialogUtils.showIfNotShowing(
                QrCodeProjectCreatorDialog::class.java,
                requireActivity().supportFragmentManager
            )
            dismiss()
        }

        binding.aboutButton.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun inflateListOfInActiveProjects(context: Context, currentProject: Project.Saved) {
        if (projectsRepository.getAll().none { it.uuid != currentProject.uuid }) {
            binding.topDivider.visibility = INVISIBLE
        } else {
            binding.topDivider.visibility = VISIBLE
        }

        projectsRepository.getAll().filter {
            it.uuid != currentProject.uuid
        }.forEach { project ->
            val projectView = ProjectListItemView(context)

            projectView.setOnClickListener {
                switchProject(project)
            }

            projectView.setupView(project, settingsProvider.getGeneralSettings(project.uuid))
            projectView.contentDescription = getString(R.string.switch_to_project, project.name)
            binding.projectList.addView(projectView)
        }
    }

    private fun switchProject(project: Project.Saved) {
        currentProjectViewModel.setCurrentProject(project)

        ActivityUtils.startActivityAndCloseAllOthers(requireActivity(), MainMenuActivity::class.java)
        ToastUtils.showLongToast(getString(R.string.switched_project, project.name))
        dismiss()
    }
}
