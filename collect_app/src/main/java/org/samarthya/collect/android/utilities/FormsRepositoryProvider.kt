package org.samarthya.collect.android.utilities

import android.content.Context
import org.samarthya.collect.android.database.forms.DatabaseFormsRepository
import org.samarthya.collect.android.storage.StoragePathProvider
import org.samarthya.collect.android.storage.StorageSubdirectory
import org.samarthya.collect.forms.FormsRepository

class FormsRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider = StoragePathProvider()
) {

    private val clock = { System.currentTimeMillis() }

    @JvmOverloads
    fun get(projectId: String? = null): FormsRepository {
        val dbPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, projectId)
        val formsPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId)
        val cachePath = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId)
        return DatabaseFormsRepository(context, dbPath, formsPath, cachePath, clock)
    }
}
