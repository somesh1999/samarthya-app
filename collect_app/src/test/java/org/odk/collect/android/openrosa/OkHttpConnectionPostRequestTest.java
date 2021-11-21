package org.samarthya.collect.android.openrosa;

import org.samarthya.collect.android.openrosa.okhttp.OkHttpConnection;
import org.samarthya.collect.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;

public class OkHttpConnectionPostRequestTest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                mapper,
                "Test Agent"
        );
    }
}
