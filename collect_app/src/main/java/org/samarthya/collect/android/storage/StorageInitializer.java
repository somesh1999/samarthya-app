package org.samarthya.collect.android.storage;

import org.samarthya.collect.android.storage.StoragePathProvider;
import org.samarthya.collect.android.utilities.FileUtils;

public class StorageInitializer {

    private final StoragePathProvider storagePathProvider;

    public StorageInitializer(StoragePathProvider storagePathProvider) {
        this.storagePathProvider = storagePathProvider;
    }

    public void createOdkDirsOnStorage() throws RuntimeException {
        String[] dirPaths = storagePathProvider.getOdkRootDirPaths();
        for (String dirPath : dirPaths) {
            FileUtils.createDir(dirPath);
        }
    }

}
