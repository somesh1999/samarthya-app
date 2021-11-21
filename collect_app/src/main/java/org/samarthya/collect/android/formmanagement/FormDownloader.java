package org.samarthya.collect.android.formmanagement;

import org.samarthya.collect.android.formmanagement.FormDownloadException;
import org.samarthya.collect.android.formmanagement.ServerFormDetails;

import java.util.function.Supplier;

import javax.annotation.Nullable;

public interface FormDownloader {

    void downloadForm(ServerFormDetails form, @Nullable ProgressReporter progressReporter, @Nullable Supplier<Boolean> isCancelled) throws FormDownloadException, InterruptedException;

    interface ProgressReporter {
        void onDownloadingMediaFile(int count);
    }
}
