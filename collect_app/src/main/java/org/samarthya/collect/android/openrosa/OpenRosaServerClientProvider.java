package org.samarthya.collect.android.openrosa;

import androidx.annotation.Nullable;

import org.samarthya.collect.android.openrosa.HttpCredentialsInterface;
import org.samarthya.collect.android.openrosa.OpenRosaServerClient;

public interface OpenRosaServerClientProvider {

    OpenRosaServerClient get(String schema, String userAgent, @Nullable HttpCredentialsInterface credentialsInterface);
}
