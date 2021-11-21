package org.samarthya.collect.android.formentry.backgroundlocation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.samarthya.collect.android.activities.FormEntryActivity;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.formentry.backgroundlocation.BackgroundLocationHelper;
import org.samarthya.collect.android.formentry.backgroundlocation.BackgroundLocationManager;
import org.samarthya.collect.location.GoogleFusedLocationClient;
import org.samarthya.collect.android.permissions.PermissionsProvider;
import org.samarthya.collect.shared.Settings;

import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_BACKGROUND_LOCATION;

/**
 * Ensures that background location tracking continues throughout the activity lifecycle. Builds
 * location-related dependency, receives activity events from #{@link FormEntryActivity} and
 * forwards those events to the location manager.
 *
 * The current goal is to keep this component very thin but this may evolve as it is involved in
 * managing more model objects.
 */
public class BackgroundLocationViewModel extends ViewModel {
    @NonNull
    private final org.samarthya.collect.android.formentry.backgroundlocation.BackgroundLocationManager locationManager;

    public BackgroundLocationViewModel(org.samarthya.collect.android.formentry.backgroundlocation.BackgroundLocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void formFinishedLoading() {
        locationManager.formFinishedLoading();
    }

    public org.samarthya.collect.android.formentry.backgroundlocation.BackgroundLocationManager.BackgroundLocationMessage activityDisplayed() {
        return locationManager.activityDisplayed();
    }

    public void activityHidden() {
        locationManager.activityHidden();
    }

    public boolean isBackgroundLocationPermissionsCheckNeeded() {
        return locationManager.isPendingPermissionCheck();
    }

    public org.samarthya.collect.android.formentry.backgroundlocation.BackgroundLocationManager.BackgroundLocationMessage locationPermissionsGranted() {
        return locationManager.locationPermissionGranted();
    }

    public void locationPermissionsDenied() {
        locationManager.locationPermissionDenied();
    }

    public void locationPermissionChanged() {
        locationManager.locationPermissionChanged();
    }

    public void locationProvidersChanged() {
        locationManager.locationProvidersChanged();
    }

    public void backgroundLocationPreferenceToggled(Settings generalSettings) {
        generalSettings.save(KEY_BACKGROUND_LOCATION, !generalSettings.getBoolean(KEY_BACKGROUND_LOCATION));
        locationManager.backgroundLocationPreferenceToggled();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final PermissionsProvider permissionsProvider;
        private final Settings generalSettings;

        public Factory(PermissionsProvider permissionsProvider, Settings generalSettings) {
            this.permissionsProvider = permissionsProvider;
            this.generalSettings = generalSettings;
        }

        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.equals(BackgroundLocationViewModel.class)) {
                GoogleFusedLocationClient googleLocationClient = new GoogleFusedLocationClient(Collect.getInstance());

                org.samarthya.collect.android.formentry.backgroundlocation.BackgroundLocationManager locationManager =
                        new BackgroundLocationManager(googleLocationClient, new BackgroundLocationHelper(permissionsProvider, generalSettings));
                return (T) new BackgroundLocationViewModel(locationManager);
            }
            return null;
        }
    }
}
