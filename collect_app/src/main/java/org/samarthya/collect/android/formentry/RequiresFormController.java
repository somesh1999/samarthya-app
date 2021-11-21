package org.samarthya.collect.android.formentry;

import androidx.annotation.NonNull;

import org.samarthya.collect.android.javarosawrapper.FormController;

public interface RequiresFormController {
    void formLoaded(@NonNull FormController formController);
}
