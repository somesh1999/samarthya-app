package org.samarthya.collect.android.support;

import android.content.Context;
import android.webkit.MimeTypeMap;

import androidx.test.espresso.IdlingResource;
import androidx.work.WorkManager;

import org.samarthya.collect.android.gdrive.GoogleAccountPicker;
import org.samarthya.collect.android.gdrive.GoogleApiProvider;
import org.samarthya.collect.android.gdrive.sheets.DriveApi;
import org.samarthya.collect.android.gdrive.sheets.SheetsApi;
import org.samarthya.collect.android.injection.config.AppDependencyModule;
import org.samarthya.collect.android.openrosa.OpenRosaHttpInterface;
import org.samarthya.collect.android.storage.StoragePathProvider;
import org.samarthya.collect.android.views.BarcodeViewDecoder;
import org.samarthya.collect.async.Scheduler;
import org.samarthya.collect.utilities.UserAgentProvider;

import java.util.List;

import static java.util.Arrays.asList;

public class TestDependencies extends AppDependencyModule {

    private final CallbackCountingTaskExecutorRule countingTaskExecutorRule = new CallbackCountingTaskExecutorRule();

    public final StubOpenRosaServer server = new StubOpenRosaServer();
    public final TestScheduler scheduler = new TestScheduler();
    public final FakeGoogleApi googleApi = new FakeGoogleApi();
    public final FakeGoogleAccountPicker googleAccountPicker = new FakeGoogleAccountPicker();
    public final StoragePathProvider storagePathProvider = new StoragePathProvider();
    public final StubBarcodeViewDecoder stubBarcodeViewDecoder = new StubBarcodeViewDecoder();


    public final List<IdlingResource> idlingResources = asList(
            new SchedulerIdlingResource(scheduler),
            new CountingTaskExecutorIdlingResource(countingTaskExecutorRule)
    );

    @Override
    public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
        return server;
    }

    @Override
    public Scheduler providesScheduler(WorkManager workManager) {
        return scheduler;
    }

    @Override
    public GoogleApiProvider providesGoogleApiProvider(Context context) {
        return new GoogleApiProvider(context) {

            @Override
            public SheetsApi getSheetsApi(String account) {
                googleApi.setAttemptAccount(account);
                return googleApi;
            }

            @Override
            public DriveApi getDriveApi(String account) {
                googleApi.setAttemptAccount(account);
                return googleApi;
            }
        };
    }

    @Override
    public GoogleAccountPicker providesGoogleAccountPicker(Context context) {
        return googleAccountPicker;
    }

    @Override
    public BarcodeViewDecoder providesBarcodeViewDecoder() {
        return stubBarcodeViewDecoder;
    }
}
