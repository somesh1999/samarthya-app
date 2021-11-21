package org.samarthya.collect.android.geo;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;

import com.google.common.collect.ImmutableSet;

import org.samarthya.collect.android.R;
import org.samarthya.collect.android.geo.MapConfigurator;
import org.samarthya.collect.android.geo.MapFragment;
import org.samarthya.collect.android.geo.MapboxMapFragment;
import org.samarthya.collect.android.geo.MapboxUtils;
import org.samarthya.collect.android.geo.MbtilesFile;
import org.samarthya.collect.android.preferences.PrefUtils;
import org.samarthya.collect.shared.Settings;
import org.samarthya.collect.android.utilities.ToastUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_MAPBOX_MAP_STYLE;
import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_REFERENCE_LAYER;

class MapboxMapConfigurator implements MapConfigurator {
    private final String prefKey;
    private final int sourceLabelId;
    private final MapboxUrlOption[] options;

    /** Constructs a configurator with a few Mapbox style URL options to choose from. */
    MapboxMapConfigurator(String prefKey, int sourceLabelId, MapboxUrlOption... options) {
        this.prefKey = prefKey;
        this.sourceLabelId = sourceLabelId;
        this.options = options;
    }

    @Override public boolean isAvailable(Context context) {
        return org.samarthya.collect.android.geo.MapboxUtils.initMapbox() != null;
    }

    @Override public void showUnavailableMessage(Context context) {
        ToastUtils.showLongToast(context.getString(
            R.string.basemap_source_unavailable, context.getString(sourceLabelId)));
    }

    @Override public MapFragment createMapFragment(Context context) {
        return MapboxUtils.initMapbox() != null ? new org.samarthya.collect.android.geo.MapboxMapFragment() : null;
    }

    @Override public List<Preference> createPrefs(Context context) {
        int[] labelIds = new int[options.length];
        String[] values = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labelIds[i] = options[i].labelId;
            values[i] = options[i].url;
        }
        String prefTitle = context.getString(
            R.string.map_style_label, context.getString(sourceLabelId));
        return Collections.singletonList(PrefUtils.createListPref(
            context, prefKey, prefTitle, labelIds, values
        ));
    }

    @Override public Set<String> getPrefKeys() {
        return prefKey.isEmpty() ? ImmutableSet.of(KEY_REFERENCE_LAYER) :
            ImmutableSet.of(prefKey, KEY_REFERENCE_LAYER);
    }

    @Override public Bundle buildConfig(Settings prefs) {
        Bundle config = new Bundle();
        config.putString(org.samarthya.collect.android.geo.MapboxMapFragment.KEY_STYLE_URL,
            prefs.getString(KEY_MAPBOX_MAP_STYLE));
        config.putString(MapboxMapFragment.KEY_REFERENCE_LAYER,
            prefs.getString(KEY_REFERENCE_LAYER));
        return config;
    }

    @Override public boolean supportsLayer(File file) {
        // MapboxMapFragment supports any file that MbtilesFile can read.
        return org.samarthya.collect.android.geo.MbtilesFile.readLayerType(file) != null;
    }

    @Override public String getDisplayName(File file) {
        String name = org.samarthya.collect.android.geo.MbtilesFile.readName(file);
        return name != null ? name : file.getName();
    }

    static class MapboxUrlOption {
        final String url;
        final int labelId;

        MapboxUrlOption(String url, int labelId) {
            this.url = url;
            this.labelId = labelId;
        }
    }
}
