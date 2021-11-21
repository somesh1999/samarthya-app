package org.samarthya.collect.android.instancemanagement;

import android.util.Pair;

import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.gdrive.GoogleAccountsManager;
import org.samarthya.collect.android.gdrive.GoogleApiProvider;
import org.samarthya.collect.android.gdrive.InstanceGoogleSheetsUploader;
import org.samarthya.collect.android.instancemanagement.InstanceDeleter;
import org.samarthya.collect.android.instancemanagement.SubmitException;
import org.samarthya.collect.android.instancemanagement.SubmitException.Type;
import org.samarthya.collect.android.logic.PropertyManager;
import org.samarthya.collect.android.openrosa.OpenRosaHttpInterface;
import org.samarthya.collect.android.permissions.PermissionsProvider;
import org.samarthya.collect.android.preferences.keys.ProjectKeys;
import org.samarthya.collect.android.upload.InstanceServerUploader;
import org.samarthya.collect.android.upload.InstanceUploader;
import org.samarthya.collect.android.upload.UploadException;
import org.samarthya.collect.android.utilities.FormsRepositoryProvider;
import org.samarthya.collect.android.utilities.InstanceUploaderUtils;
import org.samarthya.collect.android.utilities.InstancesRepositoryProvider;
import org.samarthya.collect.android.utilities.TranslationHandler;
import org.samarthya.collect.android.utilities.WebCredentialsUtils;
import org.samarthya.collect.forms.FormsRepository;
import org.samarthya.collect.forms.instances.Instance;
import org.samarthya.collect.forms.instances.InstancesRepository;
import org.samarthya.collect.shared.Settings;
import org.samarthya.collect.shared.strings.Md5;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.samarthya.collect.android.analytics.AnalyticsEvents.CUSTOM_ENDPOINT_SUB;
import static org.samarthya.collect.android.analytics.AnalyticsEvents.SUBMISSION;
import static org.samarthya.collect.android.preferences.keys.ProjectKeys.KEY_GOOGLE_SHEETS_URL;
import static org.samarthya.collect.android.utilities.InstanceUploaderUtils.SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE;

public class InstanceSubmitter {

    private final Analytics analytics;
    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;
    private final GoogleAccountsManager googleAccountsManager;
    private final GoogleApiProvider googleApiProvider;
    private final PermissionsProvider permissionsProvider;
    private final Settings generalSettings;

    public InstanceSubmitter(Analytics analytics, FormsRepository formsRepository, InstancesRepository instancesRepository,
                             GoogleAccountsManager googleAccountsManager, GoogleApiProvider googleApiProvider, PermissionsProvider permissionsProvider, Settings generalSettings) {
        this.analytics = analytics;
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
        this.googleAccountsManager = googleAccountsManager;
        this.googleApiProvider = googleApiProvider;
        this.permissionsProvider = permissionsProvider;
        this.generalSettings = generalSettings;
    }

    public Pair<Boolean, String> submitInstances(List<Instance> toUpload) throws org.samarthya.collect.android.instancemanagement.SubmitException {
        if (toUpload.isEmpty()) {
            throw new org.samarthya.collect.android.instancemanagement.SubmitException(Type.NOTHING_TO_SUBMIT);
        }

        String protocol = generalSettings.getString(ProjectKeys.KEY_PROTOCOL);

        InstanceUploader uploader;
        Map<String, String> resultMessagesByInstanceId = new HashMap<>();
        String deviceId = null;
        boolean anyFailure = false;

        if (protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
            if (permissionsProvider.isGetAccountsPermissionGranted()) {
                String googleUsername = googleAccountsManager.getLastSelectedAccountIfValid();
                if (googleUsername.isEmpty()) {
                    throw new org.samarthya.collect.android.instancemanagement.SubmitException(Type.GOOGLE_ACCOUNT_NOT_SET);
                }
                googleAccountsManager.selectAccount(googleUsername);
                uploader = new InstanceGoogleSheetsUploader(googleApiProvider.getDriveApi(googleUsername), googleApiProvider.getSheetsApi(googleUsername));
            } else {
                throw new SubmitException(Type.GOOGLE_ACCOUNT_NOT_PERMITTED);
            }
        } else {
            OpenRosaHttpInterface httpInterface = Collect.getInstance().getComponent().openRosaHttpInterface();
            uploader = new InstanceServerUploader(httpInterface, new WebCredentialsUtils(generalSettings), new HashMap<>(), generalSettings);
            deviceId = new PropertyManager().getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID);
        }

        for (Instance instance : toUpload) {
            try {
                String destinationUrl;
                if (protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
                    destinationUrl = uploader.getUrlToSubmitTo(instance, null, null, generalSettings.getString(KEY_GOOGLE_SHEETS_URL));

                    if (!InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile(destinationUrl)) {
                        anyFailure = true;
                        resultMessagesByInstanceId.put(instance.getDbId().toString(), SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE);
                        continue;
                    }
                } else {
                    destinationUrl = uploader.getUrlToSubmitTo(instance, deviceId, null, null);
                }

                String customMessage = uploader.uploadOneSubmission(instance, destinationUrl);
                resultMessagesByInstanceId.put(instance.getDbId().toString(), customMessage != null ? customMessage : TranslationHandler.getString(Collect.getInstance(), R.string.success));

                // If the submission was successful, delete the instance if either the app-level
                // delete preference is set or the form definition requests auto-deletion.
                // TODO: this could take some time so might be better to do in a separate process,
                // perhaps another worker. It also feels like this could fail and if so should be
                // communicated to the user. Maybe successful delete should also be communicated?
                if (InstanceUploaderUtils.shouldFormBeDeleted(formsRepository, instance.getFormId(), instance.getFormVersion(),
                        generalSettings.getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND))) {
                    new InstanceDeleter(new InstancesRepositoryProvider(Collect.getInstance()).get(), new FormsRepositoryProvider(Collect.getInstance()).get()).delete(instance.getDbId());
                }

                String action = protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS) ?
                        "HTTP-Sheets auto" : "HTTP auto";
                String label = Collect.getFormIdentifierHash(instance.getFormId(), instance.getFormVersion());
                analytics.logEvent(SUBMISSION, action, label);

                String submissionEndpoint = generalSettings.getString(ProjectKeys.KEY_SUBMISSION_URL);
                if (!submissionEndpoint.equals(TranslationHandler.getString(Collect.getInstance(), R.string.default_odk_submission))) {
                    String submissionEndpointHash = Md5.getMd5Hash(new ByteArrayInputStream(submissionEndpoint.getBytes()));
                    analytics.logEvent(CUSTOM_ENDPOINT_SUB, submissionEndpointHash);
                }
            } catch (UploadException e) {
                Timber.d(e);
                anyFailure = true;
                resultMessagesByInstanceId.put(instance.getDbId().toString(),
                        e.getDisplayMessage());
            }
        }

        return new Pair<>(anyFailure, InstanceUploaderUtils.getUploadResultMessage(instancesRepository, Collect.getInstance(), resultMessagesByInstanceId));
    }
}
