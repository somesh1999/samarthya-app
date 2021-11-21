package org.samarthya.collect.android.projects

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import org.samarthya.collect.analytics.Analytics
import org.samarthya.collect.android.R
import org.samarthya.collect.android.activities.ActivityUtils
import org.samarthya.collect.android.activities.MainMenuActivity
import org.samarthya.collect.android.analytics.AnalyticsEvents
import org.samarthya.collect.android.configure.qr.AppConfigurationGenerator
import org.samarthya.collect.android.databinding.ManualProjectCreatorDialogLayoutBinding
import org.samarthya.collect.android.gdrive.GoogleAccountsManager
import org.samarthya.collect.android.injection.DaggerUtils
import org.samarthya.collect.android.listeners.PermissionListener
import org.samarthya.collect.android.permissions.PermissionsProvider
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.android.projects.DuplicateProjectConfirmationKeys.MATCHING_PROJECT
import org.samarthya.collect.android.projects.DuplicateProjectConfirmationKeys.SETTINGS_JSON
import org.samarthya.collect.android.utilities.DialogUtils
import org.samarthya.collect.android.utilities.SoftKeyboardController
import org.samarthya.collect.android.utilities.ToastUtils
import org.samarthya.collect.material.MaterialFullScreenDialogFragment
import org.samarthya.collect.projects.ProjectsRepository
import org.samarthya.collect.shared.strings.Validator
import javax.inject.Inject

class ManualProjectCreatorDialog : MaterialFullScreenDialogFragment(), DuplicateProjectConfirmationDialog.DuplicateProjectConfirmationListener {

    @Inject
    lateinit var projectCreator: ProjectCreator

    @Inject
    lateinit var appConfigurationGenerator: AppConfigurationGenerator

    @Inject
    lateinit var softKeyboardController: SoftKeyboardController

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var googleAccountsManager: GoogleAccountsManager

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var settingsConnectionMatcher: SettingsConnectionMatcher

    private lateinit var binding: ManualProjectCreatorDialogLayoutBinding

    val googleAccountResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultData = result.data

        if (result.resultCode == Activity.RESULT_OK && resultData != null && resultData.extras != null) {
            val accountName = resultData.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            googleAccountsManager.selectAccount(accountName)

            val settingsJson = appConfigurationGenerator.getAppConfigurationAsJsonWithGoogleDriveDetails(
                accountName
            )

            settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson)?.let { uuid ->
                val confirmationArgs = Bundle()
                confirmationArgs.putString(SETTINGS_JSON, settingsJson)
                confirmationArgs.putString(MATCHING_PROJECT, uuid)
                DialogUtils.showIfNotShowing(DuplicateProjectConfirmationDialog::class.java, confirmationArgs, childFragmentManager)
            } ?: run {
                Analytics.log(AnalyticsEvents.GOOGLE_ACCOUNT_PROJECT)
                createProject(settingsJson)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        settingsConnectionMatcher = SettingsConnectionMatcher(projectsRepository, settingsProvider)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ManualProjectCreatorDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()

        binding.urlInputText.doOnTextChanged { text, _, _, _ ->
            binding.addButton.isEnabled = !text.isNullOrBlank()
        }

        binding.urlInputText.post {
            softKeyboardController.showSoftKeyboard(binding.urlInputText)
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addButton.setOnClickListener {
            handleAddingNewProject()
        }

        binding.gdrive.setOnClickListener {
            configureGoogleAccount()
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    private fun setUpToolbar() {
        toolbar.setTitle(R.string.add_project)
        toolbar.navigationIcon = null
    }

    private fun handleAddingNewProject() {
        if (!Validator.isUrlValid(binding.urlInputText.text?.trim().toString())) {
            ToastUtils.showShortToast(R.string.url_error)
        } else {
            val settingsJson = appConfigurationGenerator.getAppConfigurationAsJsonWithServerDetails(
                binding.urlInputText.text?.trim().toString(),
                binding.usernameInputText.text?.trim().toString(),
                binding.passwordInputText.text?.trim().toString()
            )

            settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson)?.let { uuid ->
                val confirmationArgs = Bundle()
                confirmationArgs.putString(SETTINGS_JSON, settingsJson)
                confirmationArgs.putString(MATCHING_PROJECT, uuid)
                DialogUtils.showIfNotShowing(DuplicateProjectConfirmationDialog::class.java, confirmationArgs, childFragmentManager)
            } ?: run {
                createProject(settingsJson)
                Analytics.log(AnalyticsEvents.MANUAL_CREATE_PROJECT)
            }
        }
    }

    private fun configureGoogleAccount() {
        permissionsProvider.requestGetAccountsPermission(
            activity,
            object : PermissionListener {
                override fun granted() {
                    val intent: Intent = googleAccountsManager.accountChooserIntent
                    googleAccountResultLauncher.launch(intent)
                }

                override fun denied() {
                    // nothing
                }
            }
        )
    }

    override fun createProject(settingsJson: String) {
        projectCreator.createNewProject(settingsJson)
        ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(getString(R.string.switched_project, currentProjectProvider.getCurrentProject().name))
    }

    override fun switchToProject(uuid: String) {
        currentProjectProvider.setCurrentProject(uuid)
        ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(getString(org.samarthya.collect.projects.R.string.switched_project, currentProjectProvider.getCurrentProject().name))
    }
}
