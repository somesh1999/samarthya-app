package org.samarthya.collect.android.utilities;

import android.content.Context;

import static org.samarthya.collect.strings.LocalizedApplicationKt.getLocalizedString;

public class TranslationHandler {
    private TranslationHandler() {

    }

    public static String getString(Context context, int stringId, Object... formatArgs) {
        return getLocalizedString(context, stringId, formatArgs);
    }
}
