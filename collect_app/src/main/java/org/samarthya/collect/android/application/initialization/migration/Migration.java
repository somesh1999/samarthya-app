package org.samarthya.collect.android.application.initialization.migration;

import org.samarthya.collect.shared.Settings;

public interface Migration {
    void apply(Settings prefs);
}
