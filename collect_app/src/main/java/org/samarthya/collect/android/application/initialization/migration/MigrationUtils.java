package org.samarthya.collect.android.application.initialization.migration;

import android.annotation.SuppressLint;

import org.samarthya.collect.android.application.initialization.migration.KeyCombiner;
import org.samarthya.collect.android.application.initialization.migration.KeyExtractor;
import org.samarthya.collect.android.application.initialization.migration.KeyMover;
import org.samarthya.collect.android.application.initialization.migration.KeyRenamer;
import org.samarthya.collect.android.application.initialization.migration.KeyTranslator;
import org.samarthya.collect.android.application.initialization.migration.KeyValuePair;
import org.samarthya.collect.android.application.initialization.migration.Migration;
import org.samarthya.collect.android.application.initialization.migration.ValueTranslator;
import org.samarthya.collect.shared.Settings;

public class MigrationUtils {

    private MigrationUtils() {

    }

    public static ValueTranslator translateValue(String value) {
        return new ValueTranslator(value);
    }

    /**
     * A migration that moves the value of an old preference key over to a new key.
     * Removes the old key and writes the new key ONLY if the old key is set and
     * the new key is not set.  Example:
     *         renameKey("color").toKey("couleur")
     */
    public static org.samarthya.collect.android.application.initialization.migration.KeyRenamer renameKey(String oldKey) {
        return new KeyRenamer(oldKey);
    }

    /**
     * A migration that replaces an old preference key with a new key, translating
     * specific old values to specific new values.  Removes the old key and writes
     * the new key ONLY if the old key's value exactly matches one of the values
     * passed to fromValue() and the new key is not set.  Example:
     *         translateKey("color").toKey("couleur")
     *             .fromValue("red").toValue("rouge")
     *             .fromValue("yellow").toValue("jaune")
     */
    public static org.samarthya.collect.android.application.initialization.migration.KeyTranslator translateKey(String oldKey) {
        return new KeyTranslator(oldKey);
    }

    /**
     * A migration that combines multiple keys by looking for specific sets of
     * old values across multiple keys, and replacing them with new key-value pairs.
     * Removes the old keys and writes the new key-value pairs ONLY if all the
     * values of the old keys match the set of values passed to withValues().
     * New key-value pairs MAY overwrite existing keys.  Example:
     *         combineKeys("colour", "op")
     *             .withValues("red", "lighten")
     *                 .toPairs("colour", "pink")  // only if hue = red AND op = lighten
     *             .withValues("yellow", "darken")
     *                 .toPairs("color", "brown")  // only if hue = yellow AND op = darken
     */
    public static org.samarthya.collect.android.application.initialization.migration.KeyCombiner combineKeys(String... oldKeys) {
        return new KeyCombiner(oldKeys);
    }

    public static org.samarthya.collect.android.application.initialization.migration.KeyMover moveKey(String key) {
        return new KeyMover(key);
    }

    public static Migration removeKey(String key) {
        return prefs -> prefs.remove(key);
    }

    public static org.samarthya.collect.android.application.initialization.migration.KeyExtractor extractNewKey(String oldKey) {
        return new KeyExtractor(oldKey);
    }

    /** Removes an old key and sets a new key. */
    @SuppressLint("ApplySharedPref")
    static void replace(Settings prefs, String oldKey, String newKey, Object newValue) {
        prefs.remove(oldKey);
        prefs.save(newKey, newValue);
    }

    /** Removes one or more old keys, then adds one or more new key-value pairs. */
    @SuppressLint("ApplySharedPref")
    static void replace(Settings prefs, String[] oldKeys, org.samarthya.collect.android.application.initialization.migration.KeyValuePair... newKeyValuePairs) {
        for (String key : oldKeys) {
            prefs.remove(key);
        }
        for (org.samarthya.collect.android.application.initialization.migration.KeyValuePair keyValuePair : newKeyValuePairs) {
            prefs.save(keyValuePair.key, keyValuePair.value);
        }
    }

    /** Converts an array of alternating keys and values into an array of Pairs. */
    static org.samarthya.collect.android.application.initialization.migration.KeyValuePair[] asPairs(Object... args) {
        org.samarthya.collect.android.application.initialization.migration.KeyValuePair[] keyValuePairs = new org.samarthya.collect.android.application.initialization.migration.KeyValuePair[args.length / 2];
        for (int i = 0; i * 2 + 1 < args.length; i++) {
            keyValuePairs[i] = new org.samarthya.collect.android.application.initialization.migration.KeyValuePair((String) args[i * 2], args[i * 2 + 1]);
        }
        return keyValuePairs;
    }
}
