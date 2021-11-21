package org.samarthya.collect.android.openrosa;

import android.webkit.MimeTypeMap;

import org.samarthya.collect.android.openrosa.okhttp.OkHttpConnection;
import org.samarthya.collect.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;

public class OkHttpConnectionHeadRequestTest extends OpenRosaHeadRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                new CollectThenSystemContentTypeMapper(MimeTypeMap.getSingleton()),
                USER_AGENT
        );
    }
}
