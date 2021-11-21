package org.samarthya.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.javarosa.form.api.FormEntryPrompt;
import org.samarthya.collect.android.activities.GeoPointActivity;
import org.samarthya.collect.android.activities.GeoPointMapActivity;
import org.samarthya.collect.android.activities.GeoPolyActivity;
import org.samarthya.collect.android.listeners.PermissionListener;
import org.samarthya.collect.android.permissions.PermissionsProvider;
import org.samarthya.collect.android.utilities.Appearances;
import org.samarthya.collect.android.utilities.ApplicationConstants;
import org.samarthya.collect.android.widgets.interfaces.GeoDataRequester;
import org.samarthya.collect.android.widgets.utilities.GeoWidgetUtils;
import org.samarthya.collect.android.widgets.utilities.WaitingForDataRegistry;

public class ActivityGeoDataRequester implements GeoDataRequester {
    public static final String LOCATION = "gp";
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    public static final String DRAGGABLE_ONLY = "draggable";

    private final PermissionsProvider permissionsProvider;

    public ActivityGeoDataRequester(PermissionsProvider permissionsProvider) {
        this.permissionsProvider = permissionsProvider;
    }

    @Override
    public void requestGeoPoint(Context context, FormEntryPrompt prompt, String answerText, org.samarthya.collect.android.widgets.utilities.WaitingForDataRegistry waitingForDataRegistry) {

        permissionsProvider.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Bundle bundle = new Bundle();

                if (answerText != null && !answerText.isEmpty()) {
                    bundle.putDoubleArray(LOCATION, org.samarthya.collect.android.widgets.utilities.GeoWidgetUtils.getLocationParamsFromStringAnswer(answerText));
                }

                bundle.putDouble(ACCURACY_THRESHOLD, GeoWidgetUtils.getAccuracyThreshold(prompt.getQuestion()));
                bundle.putBoolean(READ_ONLY, prompt.isReadOnly());
                bundle.putBoolean(DRAGGABLE_ONLY, hasPlacementMapAppearance(prompt));

                Intent intent = new Intent(context, isMapsAppearance(prompt) ? GeoPointMapActivity.class : GeoPointActivity.class);
                intent.putExtras(bundle);
                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.LOCATION_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    @Override
    public void requestGeoShape(Context context, FormEntryPrompt prompt, String answerText, org.samarthya.collect.android.widgets.utilities.WaitingForDataRegistry waitingForDataRegistry) {
        permissionsProvider.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Intent intent = new Intent(context, GeoPolyActivity.class);
                intent.putExtra(GeoPolyActivity.ANSWER_KEY, answerText);
                intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE);
                intent.putExtra(READ_ONLY, prompt.isReadOnly());

                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    @Override
    public void requestGeoTrace(Context context, FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry) {
        permissionsProvider.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Intent intent = new Intent(context, GeoPolyActivity.class);
                intent.putExtra(GeoPolyActivity.ANSWER_KEY, answerText);
                intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOTRACE);
                intent.putExtra(READ_ONLY, prompt.isReadOnly());

                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    private boolean isMapsAppearance(FormEntryPrompt prompt) {
        return hasMapsAppearance(prompt) || hasPlacementMapAppearance(prompt);
    }

    private boolean hasMapsAppearance(FormEntryPrompt prompt) {
        return Appearances.hasAppearance(prompt, Appearances.MAPS);
    }

    private boolean hasPlacementMapAppearance(FormEntryPrompt prompt) {
        return Appearances.hasAppearance(prompt, Appearances.PLACEMENT_MAP);
    }
}