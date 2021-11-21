package org.samarthya.collect.android.projects

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.google.zxing.client.android.BeepManager
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import org.samarthya.collect.analytics.Analytics
import org.samarthya.collect.android.R
import org.samarthya.collect.android.activities.ActivityUtils
import org.samarthya.collect.android.activities.MainMenuActivity
import org.samarthya.collect.android.analytics.AnalyticsEvents
import org.samarthya.collect.android.configure.SettingsImporter
import org.samarthya.collect.android.configure.qr.QRCodeDecoder
import org.samarthya.collect.android.databinding.QrCodeProjectCreatorDialogLayoutBinding
import org.samarthya.collect.android.injection.DaggerUtils
import org.samarthya.collect.android.listeners.PermissionListener
import org.samarthya.collect.android.permissions.PermissionsProvider
import org.samarthya.collect.android.preferences.source.SettingsProvider
import org.samarthya.collect.android.utilities.ActivityAvailability
import org.samarthya.collect.android.utilities.CodeCaptureManagerFactory
import org.samarthya.collect.android.utilities.CompressionUtils
import org.samarthya.collect.android.utilities.DialogUtils
import org.samarthya.collect.android.utilities.ToastUtils
import org.samarthya.collect.android.utilities.ToastUtils.showShortToast
import org.samarthya.collect.android.views.BarcodeViewDecoder
import org.samarthya.collect.material.MaterialFullScreenDialogFragment
import org.samarthya.collect.projects.ProjectsRepository
import timber.log.Timber
import javax.inject.Inject

class QrCodeProjectCreatorDialog :
    MaterialFullScreenDialogFragment(),
    DuplicateProjectConfirmationDialog.DuplicateProjectConfirmationListener {

    @Inject
    lateinit var codeCaptureManagerFactory: CodeCaptureManagerFactory

    @Inject
    lateinit var barcodeViewDecoder: BarcodeViewDecoder

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var projectCreator: ProjectCreator

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var settingsConnectionMatcher: SettingsConnectionMatcher

    private var capture: CaptureManager? = null

    private lateinit var beepManager: BeepManager
    lateinit var binding: QrCodeProjectCreatorDialogLayoutBinding

    @Inject
    lateinit var activityAvailability: ActivityAvailability

    @Inject
    lateinit var qrCodeDecoder: QRCodeDecoder

    @Inject
    lateinit var settingsImporter: SettingsImporter

    private val imageQrCodeImportResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                requireActivity().contentResolver.openInputStream(imageUri).use {
                    try {
                        val settingsJson = qrCodeDecoder.decode(it)
                        createProjectOrError(settingsJson)
                    } catch (e: QRCodeDecoder.InvalidException) {
                        showShortToast(R.string.invalid_qrcode)
                    } catch (e: QRCodeDecoder.NotFoundException) {
                        showShortToast(R.string.qr_code_not_found)
                    }
                }
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        settingsConnectionMatcher = SettingsConnectionMatcher(projectsRepository, settingsProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = QrCodeProjectCreatorDialogLayoutBinding.inflate(inflater)

        configureMenu()

        binding.configureManuallyButton.setOnClickListener {
            DialogUtils.showIfNotShowing(
                ManualProjectCreatorDialog::class.java,
                requireActivity().supportFragmentManager
            )
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        beepManager = BeepManager(requireActivity())
        permissionsProvider.requestCameraPermission(
            requireActivity(),
            object : PermissionListener {
                override fun granted() {
                    // Do not call from a fragment that does not exist anymore https://github.com/getodk/collect/issues/4741
                    if (isAdded) {
                        startScanning(savedInstanceState)
                    }
                }

                override fun denied() {
                }
            }
        )
        return binding.root
    }

    private fun configureMenu() {
        binding.toolbar.menu.removeItem(R.id.menu_item_share)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_item_scan_sd_card -> {
                    val photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    if (activityAvailability.isActivityAvailable(photoPickerIntent)) {
                        imageQrCodeImportResultLauncher.launch(photoPickerIntent)
                    } else {
                        showShortToast(
                            getString(
                                R.string.activity_not_found,
                                getString(R.string.choose_image)
                            )
                        )
                        Timber.w(
                            getString(
                                R.string.activity_not_found,
                                getString(R.string.choose_image)
                            )
                        )
                    }
                }
            }
            false
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        capture?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pauseAndWait()
        capture?.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
        capture?.onResume()
    }

    override fun onDestroy() {
        capture?.onDestroy()
        super.onDestroy()
    }

    override fun getToolbar(): Toolbar? {
        return binding.toolbar
    }

    private fun startScanning(savedInstanceState: Bundle?) {
        capture = codeCaptureManagerFactory.getCaptureManager(
            requireActivity(),
            binding.barcodeView,
            savedInstanceState,
            listOf(IntentIntegrator.QR_CODE)
        )

        barcodeViewDecoder.waitForBarcode(binding.barcodeView).observe(
            viewLifecycleOwner,
            { barcodeResult: BarcodeResult ->
                try {
                    beepManager.playBeepSoundAndVibrate()
                } catch (e: Exception) {
                    // ignore because beeping isn't essential and this can crash the whole app
                }

                try {
                    val settingsJson = CompressionUtils.decompress(barcodeResult.text)
                    createProjectOrError(settingsJson)
                } catch (e: Exception) {
                    showShortToast(getString(R.string.invalid_qrcode))
                }
            }
        )
    }

    private fun createProjectOrError(settingsJson: String) {
        try {
            settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson)?.let { uuid ->
                val confirmationArgs = Bundle()
                confirmationArgs.putString(
                    DuplicateProjectConfirmationKeys.SETTINGS_JSON,
                    settingsJson
                )
                confirmationArgs.putString(DuplicateProjectConfirmationKeys.MATCHING_PROJECT, uuid)
                DialogUtils.showIfNotShowing(
                    DuplicateProjectConfirmationDialog::class.java,
                    confirmationArgs,
                    childFragmentManager
                )
            } ?: run {
                createProject(settingsJson)
            }
        } catch (e: Exception) {
            showShortToast(getString(R.string.invalid_qrcode))
        }
    }

    override fun createProject(settingsJson: String) {
        val projectCreatedSuccessfully = projectCreator.createNewProject(settingsJson)

        if (projectCreatedSuccessfully) {
            Analytics.log(AnalyticsEvents.QR_CREATE_PROJECT)

            ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
            ToastUtils.showLongToast(
                getString(
                    R.string.switched_project,
                    currentProjectProvider.getCurrentProject().name
                )
            )
        } else {
            ToastUtils.showLongToast(getString(R.string.invalid_qrcode))
        }
    }

    override fun switchToProject(uuid: String) {
        currentProjectProvider.setCurrentProject(uuid)
        ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(
            getString(
                R.string.switched_project,
                currentProjectProvider.getCurrentProject().name
            )
        )
    }
}
