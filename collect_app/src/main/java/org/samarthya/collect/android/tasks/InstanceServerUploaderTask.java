/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.samarthya.collect.android.tasks;

import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.tasks.InstanceUploaderTask;
import org.samarthya.collect.forms.instances.Instance;
import org.samarthya.collect.android.logic.PropertyManager;
import org.samarthya.collect.android.openrosa.OpenRosaHttpInterface;
import org.samarthya.collect.android.upload.InstanceServerUploader;
import org.samarthya.collect.android.upload.UploadAuthRequestedException;
import org.samarthya.collect.android.upload.UploadException;
import org.samarthya.collect.android.utilities.TranslationHandler;
import org.samarthya.collect.android.utilities.WebCredentialsUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import static org.samarthya.collect.android.analytics.AnalyticsEvents.SUBMISSION;

/**
 * Background task for uploading completed forms.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceServerUploaderTask extends InstanceUploaderTask {
    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    @Inject
    Analytics analytics;

    // Custom submission URL, username and password that can be sent via intent extras by external
    // applications
    private String completeDestinationUrl;
    private String customUsername;
    private String customPassword;

    public InstanceServerUploaderTask() {
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    public Outcome doInBackground(Long... instanceIdsToUpload) {
        Outcome outcome = new Outcome();

        InstanceServerUploader uploader = new InstanceServerUploader(httpInterface, webCredentialsUtils, new HashMap<>(), settingsProvider.getGeneralSettings());
        List<Instance> instancesToUpload = uploader.getInstancesFromIds(instanceIdsToUpload);

        String deviceId = new PropertyManager().getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID);

        for (int i = 0; i < instancesToUpload.size(); i++) {
            if (isCancelled()) {
                return outcome;
            }
            Instance instance = instancesToUpload.get(i);

            publishProgress(i + 1, instancesToUpload.size());

            try {
                //String destinationUrl = uploader.getUrlToSubmitTo(instance, deviceId, completeDestinationUrl, null);
                /*String customMessage = uploader.uploadOneSubmission(instance, destinationUrl);
                outcome.messagesByInstanceId.put(instance.getDbId().toString(),
                        customMessage != null ? customMessage : TranslationHandler.getString(Collect.getInstance(), R.string.success));*/

                /*analytics.logEvent(SUBMISSION, "HTTP", Collect.getFormIdentifierHash(instance.getFormId(), instance.getFormVersion()));*/

                String destinationUrl = "";
                if(instance.getDisplayName().contains("School")){
                    destinationUrl = "https://samarthyaodisha.in/server/samarthya/uploadfilesfromappschool.php";
                }else if(instance.getDisplayName().contains("Monitoring")){
                    destinationUrl = "https://samarthyaodisha.in/server/samarthya/uploadfilesfromappmonitoring.php";
                }else {
                    destinationUrl = "https://samarthyaodisha.in/server/samarthya/uploadfilesfromappteacher.php";
                }
                String customMessage = uploader.uploadXmlForm(instance, destinationUrl);
                outcome.messagesByInstanceId.put(instance.getDbId().toString(),
                        customMessage != null ? customMessage : TranslationHandler.getString(Collect.getInstance(), R.string.success));
                analytics.logEvent(SUBMISSION, "HTTP", Collect.getFormIdentifierHash(instance.getFormId(), instance.getFormVersion()));

            }
            catch (Exception e) {
                outcome.messagesByInstanceId.put(instance.getDbId().toString(),
                        e.getMessage());
            }/*catch (UploadAuthRequestedException e) {
                outcome.authRequestingServer = e.getAuthRequestingServer();
                // Don't add the instance that caused an auth request to the map because we want to
                // retry. Items present in the map are considered already attempted and won't be
                // retried.
            } catch (UploadException e) {
                outcome.messagesByInstanceId.put(instance.getDbId().toString(),
                        e.getDisplayMessage());
            }*/
        }
        
        return outcome;
    }

    @Override
    protected void onPostExecute(Outcome outcome) {
        super.onPostExecute(outcome);

        // Clear temp credentials
        clearTemporaryCredentials();
    }

    @Override
    protected void onCancelled() {
        clearTemporaryCredentials();
    }

    public void setCompleteDestinationUrl(String completeDestinationUrl) {
        setCompleteDestinationUrl(completeDestinationUrl, true);
    }

    public void setCompleteDestinationUrl(String completeDestinationUrl, boolean clearPreviousConfig) {
        this.completeDestinationUrl = completeDestinationUrl;
        if (clearPreviousConfig) {
            setTemporaryCredentials();
        }
    }

    public void setCustomUsername(String customUsername) {
        this.customUsername = customUsername;
        setTemporaryCredentials();
    }

    public void setCustomPassword(String customPassword) {
        this.customPassword = customPassword;
        setTemporaryCredentials();
    }

    private void setTemporaryCredentials() {
        if (customUsername != null && customPassword != null) {
            webCredentialsUtils.saveCredentials(completeDestinationUrl, customUsername, customPassword);
        } else {
            // In the case for anonymous logins, clear the previous credentials for that host
            webCredentialsUtils.clearCredentials(completeDestinationUrl);
        }
    }

    private void clearTemporaryCredentials() {
        if (customUsername != null && customPassword != null) {
            webCredentialsUtils.clearCredentials(completeDestinationUrl);
        }
    }
}
