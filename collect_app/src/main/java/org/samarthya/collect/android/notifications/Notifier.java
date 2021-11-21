package org.samarthya.collect.android.notifications;

import org.samarthya.collect.android.formmanagement.ServerFormDetails;
import org.samarthya.collect.forms.FormSourceException;

import java.util.List;
import java.util.Map;

public interface Notifier {

    void onUpdatesAvailable(List<ServerFormDetails> updates);

    void onUpdatesDownloaded(Map<ServerFormDetails, String> result);

    void onSync(FormSourceException exception);

    void onSubmission(boolean failure, String message);
}
