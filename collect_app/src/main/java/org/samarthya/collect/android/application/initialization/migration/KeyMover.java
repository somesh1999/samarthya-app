package org.samarthya.collect.android.application.initialization.migration;

import org.samarthya.collect.android.application.initialization.migration.Migration;
import org.samarthya.collect.shared.Settings;

import java.util.Map;

public class KeyMover implements Migration {
    private final String key;
    private Settings newPrefs;

    public KeyMover(String key) {
        this.key = key;
    }

    public KeyMover toPreferences(Settings newPrefs) {
        this.newPrefs = newPrefs;
        return this;
    }

    @Override
    public void apply(Settings prefs) {
        if (newPrefs.contains(key)) {
            return;
        }

        Map<String, ?> all = prefs.getAll();
        Object value = all.get(key);

        prefs.remove(key);
        newPrefs.save(key, value);
    }
}
