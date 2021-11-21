package org.samarthya.collect.android.openrosa;

import org.jetbrains.annotations.NotNull;
import org.samarthya.collect.android.openrosa.HttpGetResult;
import org.samarthya.collect.android.openrosa.OpenRosaHttpInterface;
import org.samarthya.collect.android.openrosa.OpenRosaResponseParser;
import org.samarthya.collect.android.openrosa.OpenRosaXmlFetcher;
import org.samarthya.collect.android.utilities.DocumentFetchResult;
import org.samarthya.collect.android.utilities.WebCredentialsUtils;
import org.samarthya.collect.forms.FormListItem;
import org.samarthya.collect.forms.FormSource;
import org.samarthya.collect.forms.FormSourceException;
import org.samarthya.collect.forms.ManifestFile;
import org.samarthya.collect.forms.MediaFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLException;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class OpenRosaFormSource implements FormSource {

    private final OpenRosaXmlFetcher openRosaXMLFetcher;
    private final OpenRosaResponseParser openRosaResponseParser;
    private final WebCredentialsUtils webCredentialsUtils;

    private String serverURL;
    private final String formListPath;

    public OpenRosaFormSource(String serverURL, String formListPath, OpenRosaHttpInterface openRosaHttpInterface, WebCredentialsUtils webCredentialsUtils, OpenRosaResponseParser openRosaResponseParser) {
        this.openRosaResponseParser = openRosaResponseParser;
        this.webCredentialsUtils = webCredentialsUtils;
        this.openRosaXMLFetcher = new OpenRosaXmlFetcher(openRosaHttpInterface, this.webCredentialsUtils);
        this.serverURL = serverURL;
        this.formListPath = formListPath;
    }

    @Override
    public List<FormListItem> fetchFormList() throws FormSourceException {
        DocumentFetchResult result = mapException(() -> openRosaXMLFetcher.getXML(getFormListURL()));

        if (result.errorMessage != null) {
            if (result.responseCode == HTTP_UNAUTHORIZED) {
                throw new FormSourceException.AuthRequired();
            } else if (result.responseCode == HTTP_NOT_FOUND) {
                throw new FormSourceException.Unreachable(serverURL);
            } else {
                throw new FormSourceException.ServerError(result.responseCode, serverURL);
            }
        }

        if (result.isOpenRosaResponse) {
            List<FormListItem> formList = openRosaResponseParser.parseFormList(result.doc);

            if (formList != null) {
                return formList;
            } else {
                throw new FormSourceException.ParseError(serverURL);
            }
        } else {
            throw new FormSourceException.ServerNotOpenRosaError();
        }
    }

    @Override
    public ManifestFile fetchManifest(String manifestURL) throws FormSourceException {
        if (manifestURL == null) {
            return null;
        }

        DocumentFetchResult result = mapException(() -> openRosaXMLFetcher.getXML(manifestURL));

        if (result.errorMessage != null) {
            if (result.responseCode != HttpURLConnection.HTTP_OK) {
                throw new FormSourceException.ServerError(result.responseCode, serverURL);
            } else {
                throw new FormSourceException.FetchError();
            }
        }

        if (!result.isOpenRosaResponse) {
            throw new FormSourceException.ParseError(serverURL);
        }

        List<MediaFile> mediaFiles = openRosaResponseParser.parseManifest(result.doc);
        if (mediaFiles != null) {
            return new ManifestFile(result.getHash(), mediaFiles);
        } else {
            throw new FormSourceException.ParseError(serverURL);
        }
    }

    @Override
    @NotNull
    public InputStream fetchForm(String formURL) throws FormSourceException {
        org.samarthya.collect.android.openrosa.HttpGetResult result = mapException(() -> openRosaXMLFetcher.fetch(formURL, null));

        if (result.getInputStream() == null) {
            throw new FormSourceException.ServerError(result.getStatusCode(), serverURL);
        } else {
            return result.getInputStream();
        }
    }

    @Override
    @NotNull
    public InputStream fetchMediaFile(String mediaFileURL) throws FormSourceException {
        HttpGetResult result = mapException(() -> openRosaXMLFetcher.fetch(mediaFileURL, null));

        if (result.getInputStream() == null) {
            throw new FormSourceException.ServerError(result.getStatusCode(), serverURL);
        } else {
            return result.getInputStream();
        }
    }

    public void updateUrl(String url) {
        this.serverURL = url;
    }

    public void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils) {
        this.openRosaXMLFetcher.updateWebCredentialsUtils(webCredentialsUtils);
    }

    @NotNull
    private <T> T mapException(Callable<T> callable) throws FormSourceException {
        try {
            T result = callable.call();

            if (result != null) {
                return result;
            } else {
                throw new FormSourceException.FetchError();
            }
        } catch (UnknownHostException e) {
            throw new FormSourceException.Unreachable(serverURL);
        } catch (SSLException e) {
            throw new FormSourceException.SecurityError(serverURL);
        } catch (Exception e) {
            throw new FormSourceException.FetchError();
        }
    }

    @NotNull
    private String getFormListURL() {
        String downloadListUrl = serverURL;

        while (downloadListUrl.endsWith("/")) {
            downloadListUrl = downloadListUrl.substring(0, downloadListUrl.length() - 1);
        }

        downloadListUrl += formListPath;
        return downloadListUrl;
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getFormListPath() {
        return formListPath;
    }

    public WebCredentialsUtils getWebCredentialsUtils() {
        return webCredentialsUtils;
    }
}
