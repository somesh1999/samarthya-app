package org.samarthya.collect.android.application.initialization.migration;

import org.samarthya.collect.android.application.initialization.migration.Migration;
import org.samarthya.collect.shared.Settings;

import static org.samarthya.collect.android.application.initialization.migration.MigrationUtils.replace;

public class KeyRenamer implements Migration {

    String oldKey;
    String newKey;

    KeyRenamer(String oldKey) {
        this.oldKey = oldKey;
    }

    public KeyRenamer toKey(String newKey) {
        this.newKey = newKey;
        return this;
    }

    public void apply(Settings prefs) {
        if (prefs.contains(oldKey) && !prefs.contains(newKey)) {
            Object value = prefs.getAll().get(oldKey);
            replace(prefs, oldKey, newKey, value);
        }
    }
}
