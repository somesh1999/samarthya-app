package org.samarthya.collect.android.configure.qr

import android.content.Context
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import org.samarthya.collect.analytics.Analytics
import org.samarthya.collect.android.R
import org.samarthya.collect.android.activities.ActivityUtils
import org.samarthya.collect.android.activities.MainMenuActivity
import org.samarthya.collect.android.analytics.AnalyticsEvents
import org.samarthya.collect.android.configure.SettingsImporter
import org.samarthya.collect.android.fragments.BarCodeScannerFragment
import org.samarthya.collect.android.injection.DaggerUtils
import org.samarthya.collect.android.projects.CurrentProjectProvider
import org.samarthya.collect.android.storage.StoragePathProvider
import org.samarthya.collect.android.utilities.CompressionUtils
import org.samarthya.collect.android.utilities.ToastUtils.showLongToast
import java.io.File
import java.io.IOException
import java.util.zip.DataFormatException
import javax.inject.Inject

class QRCodeScannerFragment : BarCodeScannerFragment() {
    @Inject
    lateinit var settingsImporter: SettingsImporter

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var storagePathProvider: StoragePathProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    @Throws(IOException::class, DataFormatException::class)
    override fun handleScanningResult(result: BarcodeResult) {
        val oldProjectName = currentProjectProvider.getCurrentProject().name

        val importSuccess = settingsImporter.fromJSON(
            CompressionUtils.decompress(result.text),
            currentProjectProvider.getCurrentProject()
        )

        if (importSuccess) {
            Analytics.log(AnalyticsEvents.RECONFIGURE_PROJECT)

            val newProjectName = currentProjectProvider.getCurrentProject().name
            if (newProjectName != oldProjectName) {
                File(storagePathProvider.getProjectRootDirPath() + File.separator + oldProjectName).delete()
                File(storagePathProvider.getProjectRootDirPath() + File.separator + newProjectName).createNewFile()
            }

            showLongToast(getString(R.string.successfully_imported_settings))
            ActivityUtils.startActivityAndCloseAllOthers(
                requireActivity(),
                MainMenuActivity::class.java
            )
        } else {
            showLongToast(getString(R.string.invalid_qrcode))
        }
    }

    override fun getSupportedCodeFormats(): Collection<String> {
        return listOf(IntentIntegrator.QR_CODE)
    }
}
