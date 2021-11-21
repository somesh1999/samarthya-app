package org.samarthya.collect.android.utilities

import android.content.Context
import org.samarthya.collect.android.database.instances.DatabaseInstancesRepository
import org.samarthya.collect.android.storage.StoragePathProvider
import org.samarthya.collect.android.storage.StorageSubdirectory
import org.samarthya.collect.forms.instances.InstancesRepository

class InstancesRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider = StoragePathProvider()
) {

    @JvmOverloads
    fun get(projectId: String? = null): InstancesRepository {
        return DatabaseInstancesRepository(
            context,
            storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, projectId),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, projectId),
            System::currentTimeMillis
        )
    }
}
