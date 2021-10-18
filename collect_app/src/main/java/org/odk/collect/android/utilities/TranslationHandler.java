package org.odk.collect.android.utilities;

import android.content.Context;

import static org.odk.collect.strings.LocalizedApplicationKt.getLocalizedString;

public class TranslationHandler {
    private TranslationHandler() {

    }

    public static String getString(Context context, int stringId, Object... formatArgs) {
        return getLocalizedString(context, stringId, formatArgs);
    }
}
