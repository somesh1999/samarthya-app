package org.samarthya.collect.android.utilities;

import org.samarthya.collect.android.BuildConfig;
import org.samarthya.collect.utilities.UserAgentProvider;

public final class AndroidUserAgent implements UserAgentProvider {

    @Override
    public String getUserAgent() {
        return String.format("%s/%s %s",
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME,
                System.getProperty("http.agent"));
    }

}
