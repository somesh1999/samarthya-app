package org.samarthya.collect.android.fakes;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import org.samarthya.collect.android.listeners.PermissionListener;
import org.samarthya.collect.android.permissions.PermissionsChecker;
import org.samarthya.collect.android.permissions.PermissionsProvider;

/**
 * Mocked implementation of {@link PermissionsProvider}.
 * The runtime permissions can be stubbed for unit testing
 *
 * @author Shobhit Agarwal
 */
public class FakePermissionsProvider extends PermissionsProvider {

    private boolean isPermissionGranted;

    public FakePermissionsProvider() {
        super(new PermissionsChecker(InstrumentationRegistry.getInstrumentation().getTargetContext()));
    }

    @Override
    protected void requestPermissions(Activity activity, @NonNull PermissionListener listener, String... permissions) {
        if (isPermissionGranted) {
            listener.granted();
        } else {
            listener.denied();
        }
    }

    @Override
    protected void showAdditionalExplanation(Activity activity, int title, int message, int drawable, @NonNull PermissionListener action) {
        action.denied();
    }

    public void setPermissionGranted(boolean permissionGranted) {
        isPermissionGranted = permissionGranted;
    }
}
