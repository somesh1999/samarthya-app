package org.samarthya.collect.android.configure.qr;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.samarthya.collect.android.R;
import org.samarthya.collect.android.activities.CollectAbstractActivity;
import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.configure.SettingsImporter;
import org.samarthya.collect.android.configure.qr.AppConfigurationGenerator;
import org.samarthya.collect.android.configure.qr.QRCodeActivityResultDelegate;
import org.samarthya.collect.android.configure.qr.QRCodeDecoder;
import org.samarthya.collect.android.configure.qr.QRCodeGenerator;
import org.samarthya.collect.android.configure.qr.QRCodeMenuDelegate;
import org.samarthya.collect.android.configure.qr.QRCodeTabsAdapter;
import org.samarthya.collect.android.injection.DaggerUtils;
import org.samarthya.collect.android.listeners.PermissionListener;
import org.samarthya.collect.android.projects.CurrentProjectProvider;
import org.samarthya.collect.android.utilities.ActivityAvailability;
import org.samarthya.collect.android.utilities.FileProvider;
import org.samarthya.collect.android.utilities.MultiClickGuard;
import org.samarthya.collect.async.Scheduler;

import javax.inject.Inject;

public class QRCodeTabsActivity extends CollectAbstractActivity {

    private static String[] fragmentTitleList;

    @Inject
    QRCodeGenerator qrCodeGenerator;

    @Inject
    ActivityAvailability activityAvailability;

    @Inject
    FileProvider fileProvider;

    @Inject
    Scheduler scheduler;

    @Inject
    QRCodeDecoder qrCodeDecoder;

    @Inject
    SettingsImporter settingsImporter;

    @Inject
    Analytics analytics;

    @Inject
    AppConfigurationGenerator appConfigurationGenerator;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    private org.samarthya.collect.android.configure.qr.QRCodeMenuDelegate menuDelegate;
    private QRCodeActivityResultDelegate activityResultDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);

        menuDelegate = new org.samarthya.collect.android.configure.qr.QRCodeMenuDelegate(this, activityAvailability, qrCodeGenerator, appConfigurationGenerator, fileProvider, settingsProvider, scheduler);
        activityResultDelegate = new QRCodeActivityResultDelegate(this, settingsImporter, qrCodeDecoder, currentProjectProvider.getCurrentProject());
        setContentView(R.layout.tabs_layout);

        initToolbar(getString(R.string.reconfigure_with_qr_code_settings_title));
        menuDelegate = new QRCodeMenuDelegate(this, activityAvailability, qrCodeGenerator, appConfigurationGenerator, fileProvider, settingsProvider, scheduler);

        permissionsProvider.requestCameraPermission(this, new PermissionListener() {
            @Override
            public void granted() {
                setupViewPager();
            }

            @Override
            public void denied() {
                finish();
            }
        });
    }

    private void setupViewPager() {
        fragmentTitleList = new String[]{getString(R.string.scan_qr_code_fragment_title),
                getString(R.string.view_qr_code_fragment_title)};

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        org.samarthya.collect.android.configure.qr.QRCodeTabsAdapter adapter = new QRCodeTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(fragmentTitleList[position])).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuDelegate.onCreateOptionsMenu(getMenuInflater(), menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        if (menuDelegate.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        activityResultDelegate.onActivityResult(requestCode, resultCode, data);
    }
}
