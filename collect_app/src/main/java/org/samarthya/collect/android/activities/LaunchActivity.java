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

package org.samarthya.collect.android.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.samarthya.collect.android.R;
import org.samarthya.collect.android.activities.CollectAbstractActivity;
import org.samarthya.collect.android.activities.MainMenuActivity;
import org.samarthya.collect.android.activities.viewmodels.CurrentProjectViewModel;
import org.samarthya.collect.android.activities.viewmodels.FormDownloadListViewModel;
import org.samarthya.collect.android.activities.viewmodels.MainMenuViewModel;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.formentry.RefreshFormListDialogFragment;
import org.samarthya.collect.android.formmanagement.BlankFormsListViewModel;
import org.samarthya.collect.android.formmanagement.FormDownloader;
import org.samarthya.collect.android.formmanagement.ServerFormDetails;
import org.samarthya.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.samarthya.collect.android.injection.DaggerUtils;
import org.samarthya.collect.android.listeners.DownloadFormsTaskListener;
import org.samarthya.collect.android.listeners.FormListDownloaderListener;
import org.samarthya.collect.android.network.NetworkStateProvider;
import org.samarthya.collect.android.preferences.keys.MetaKeys;
import org.samarthya.collect.android.preferences.source.SettingsProvider;
import org.samarthya.collect.android.projects.ProjectIconView;
import org.samarthya.collect.android.projects.ProjectSettingsDialog;
import org.samarthya.collect.android.storage.StorageInitializer;
import org.samarthya.collect.android.tasks.DownloadFormListTask;
import org.samarthya.collect.android.tasks.DownloadFormsTask;
import org.samarthya.collect.android.utilities.ApplicationConstants;
import org.samarthya.collect.android.utilities.DialogUtils;
import org.samarthya.collect.android.utilities.MultiClickGuard;
import org.samarthya.collect.android.utilities.ToastUtils;
import org.samarthya.collect.android.utilities.TranslationHandler;
import org.samarthya.collect.android.utilities.WebCredentialsUtils;
import org.samarthya.collect.forms.FormSourceException;

import javax.inject.Inject;

import static org.samarthya.collect.android.utilities.DialogUtils.showIfNotShowing;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class LaunchActivity extends CollectAbstractActivity implements FormListDownloaderListener, DownloadFormsTaskListener{
    // buttons
    private Button sendDataButton;
    private Button reviewDataButton;

    @Inject
    MainMenuViewModel.Factory viewModelFactory;

    @Inject
    CurrentProjectViewModel.Factory currentProjectViewModelFactory;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    StorageInitializer storageInitializer;

    @Inject
    NetworkStateProvider connectivityProvider;

    @Inject
    ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    @Inject
    FormDownloader formDownloader;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    @Inject
    BlankFormsListViewModel.Factory blankFormsListViewModelFactory;

    private MainMenuViewModel mainMenuViewModel;

    private CurrentProjectViewModel currentProjectViewModel;

    private FormDownloadListViewModel viewModel;

    private DownloadFormListTask downloadFormListTask;

    private DownloadFormsTask downloadFormsTask;

    private ProgressDialog cancelDialog;

    private AlertDialog alertDialog;

    private static final boolean EXIT = true;

    FloatingActionButton fab;

    List<String> downloadedForms = new ArrayList<>();

    String formFormat = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        setContentView(R.layout.launch_activity);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonShowPopupWindowClick(view);
            }
        });

        settingsProvider.getMetaSettings().save(MetaKeys.FIRST_LAUNCH, false);

        mainMenuViewModel = new ViewModelProvider(this, viewModelFactory).get(MainMenuViewModel.class);
        currentProjectViewModel = new ViewModelProvider(this, currentProjectViewModelFactory).get(CurrentProjectViewModel.class);
        currentProjectViewModel.getCurrentProject().observe(this, project -> {
            invalidateOptionsMenu();
            setTitle("  "+getString(R.string.collect_app_name)); // project.getName()

        });

        viewModel = new ViewModelProvider(this, new FormDownloadListViewModel.Factory())
                .get(FormDownloadListViewModel.class);

        initToolbar();

        // enter data button. expects a result.
        Button enterDataButton = findViewById(R.id.enter_data);
        enterDataButton.setText(getString(R.string.teacher_format));
        enterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(getApplicationContext(),
                        MainMenuActivity.class);
                startActivity(i);*/
                downloadedForms.clear();
                for(int i=1001; i<=1100; i++){
                    downloadedForms.add("snapshot_xml"+i);
                }
                formFormat = "Teacher";
                downloadFormList();
            }
        });

        // review data button. expects a result.
        reviewDataButton = findViewById(R.id.review_data);
        reviewDataButton.setText(getString(R.string.head_format));
        reviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
                i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.EDIT_SAVED);
                startActivity(i);*/
                for(int i=1101; i<=1200; i++){
                    downloadedForms.add("snapshot_xml"+i);
                }
                formFormat = "School";
                downloadFormList();
            }
        });

        // send data button. expects a result.
        sendDataButton = findViewById(R.id.send_data);
        sendDataButton.setText(getString(R.string.monitoring_format));
        sendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent i = new Intent(getApplicationContext(),
                        MainMenuActivity.class);
                startActivity(i);*/
                for(int i=1201; i<=1300; i++){
                    downloadedForms.add("snapshot_xml"+i);
                }
                formFormat = "Monitoring";
                downloadFormList();
            }
        });



        TextView appName = findViewById(R.id.app_name);
        //appName.setText(String.format("%s %s", getString(R.string.collect_app_name), mainMenuViewModel.getVersion()));

        TextView versionSHAView = findViewById(R.id.version_sha);
        //String versionSHA = mainMenuViewModel.getVersionCommitDescription();
        String versionSHA = null;
        if (versionSHA != null) {
            versionSHAView.setText(versionSHA);
        } else {
            versionSHAView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainMenuViewModel.refreshInstances();

        setButtonsVisibility();
        currentProjectViewModel.refresh();
    }

    private void setButtonsVisibility() {
        reviewDataButton.setVisibility(mainMenuViewModel.shouldEditSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        sendDataButton.setVisibility(mainMenuViewModel.shouldSendFinalizedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem projectsMenuItem = menu.findItem(R.id.projects);

        ProjectIconView projectIconView = (ProjectIconView) projectsMenuItem.getActionView();
        projectIconView.setProject(currentProjectViewModel.getCurrentProject().getValue());
        projectIconView.setOnClickListener(v -> onOptionsItemSelected(projectsMenuItem));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        if (item.getItemId() == R.id.projects) {
            showIfNotShowing(ProjectSettingsDialog.class, getSupportFragmentManager());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.samarthya_logo_appbar);
        setSupportActionBar(toolbar);
    }


    public void onButtonShowPopupWindowClick(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.help_popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        Toolbar toolbar = popupView.findViewById(R.id.toolbar);
        toolbar.setTitle("User Guide");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        WebView webView = (WebView)popupView.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        ProgressDialog progressBar = ProgressDialog.show(LaunchActivity.this, "User Guide", "Loading...");
        webView.loadUrl("https://drive.google.com/file/d/1czLt_JKLmmQpa_2IYYYjemhI0MwupHzx/view?usp=sharing");
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }
            public void onPageFinished(WebView view, String url) {
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            }
        });

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void downloadFormList() {
        boolean isFormPresent = false;
        if (!connectivityProvider.isDeviceOnline()) {
            viewModel.setDownloadOnlyMode(true);
            if (viewModel.isDownloadOnlyMode()) {
                Intent i = new Intent(getBaseContext(), MainMenuActivity.class);
                startActivity(i);
            }
        } else {
            BlankFormsListViewModel blankFormsListViewModel = new ViewModelProvider(this, blankFormsListViewModelFactory).get(BlankFormsListViewModel.class);
            for(int i=0; i<blankFormsListViewModel.getForms().size(); i++){
                if(blankFormsListViewModel.getForms().get(i).getName().contains(formFormat)){
                    isFormPresent = true;
                    break;
                }
            }
            if(!isFormPresent) { // condition of form exits
                viewModel.clearFormDetailsByFormId();
                DialogUtils.showIfNotShowing(RefreshFormListDialogFragment.class, getSupportFragmentManager());
                if (downloadFormListTask != null
                        && downloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
                    return; // we are already doing the download!!!
                } else if (downloadFormListTask != null) {
                    downloadFormListTask.setDownloaderListener(null);
                    downloadFormListTask.cancel(true);
                    downloadFormListTask = null;
                }
                downloadFormListTask = new DownloadFormListTask(serverFormsDetailsFetcher);
                downloadFormListTask.setDownloaderListener(this);
                downloadFormListTask.execute();
            }else{
                Intent i = new Intent(getBaseContext(), MainMenuActivity.class);
                i.putExtra("formFormat", formFormat);
                startActivity(i);
            }
        }
    }

    private void setReturnResult(boolean successful, @Nullable String message, @Nullable HashMap<String, Boolean> resultFormIds) {
        Intent intent = new Intent();
        intent.putExtra(ApplicationConstants.BundleKeys.SUCCESS_KEY, successful);
        if (message != null) {
            intent.putExtra(ApplicationConstants.BundleKeys.MESSAGE, message);
        }
        if (resultFormIds != null) {
            intent.putExtra(ApplicationConstants.BundleKeys.FORM_IDS, resultFormIds);
        }

        setResult(RESULT_OK, intent);
    }

    @Override
    public void formListDownloadingComplete(HashMap<String, ServerFormDetails> formList, FormSourceException exception) {
        DialogUtils.dismissDialog(RefreshFormListDialogFragment.class, getSupportFragmentManager());
        downloadFormListTask.setDownloaderListener(null);
        downloadFormListTask = null;
        if (exception == null) {
            // Everything worked. Clear the list and add the results.
            viewModel.setFormDetailsByFormId(formList);
            viewModel.clearFormList();

            ArrayList<String> ids = new ArrayList<>(viewModel.getFormDetailsByFormId().keySet());
            List<Integer> tempFormKey = new ArrayList<>();
            for (int i = 0; i < formList.size(); i++) {
                String formDetailsKey = ids.get(i);
                if(downloadedForms.contains(formDetailsKey)) {
                    int tempFormVal = Integer.parseInt(formDetailsKey.split("xml")[1]);
                    tempFormKey.add(tempFormVal);
                    /*ArrayList<ServerFormDetails> filesToDownload = getFilesToDownload(formDetailsKey);
                    startFormsDownload(filesToDownload);*/
                }
            }
            ArrayList<ServerFormDetails> filesToDownload = getFilesToDownload("snapshot_xml"+ Collections.max(tempFormKey));
            startFormsDownload(filesToDownload);
        }
    }

    private ArrayList<ServerFormDetails> getFilesToDownload(String formDetailsKey) {
        ArrayList<ServerFormDetails> filesToDownload = new ArrayList<>();
        filesToDownload.add(viewModel.getFormDetailsByFormId().get(formDetailsKey));
        return filesToDownload;
    }

    @SuppressWarnings("unchecked")
    private void startFormsDownload(@NonNull ArrayList<ServerFormDetails> filesToDownload) {
        int totalCount = filesToDownload.size();
        if (totalCount > 0) {
            // show dialog box
            DialogUtils.showIfNotShowing(RefreshFormListDialogFragment.class, getSupportFragmentManager());

            downloadFormsTask = new DownloadFormsTask(formDownloader);
            downloadFormsTask.setDownloaderListener(this);

            if (viewModel.getUrl() != null) {
                if (viewModel.getUsername() != null && viewModel.getPassword() != null) {
                    webCredentialsUtils.saveCredentials(viewModel.getUrl(), viewModel.getUsername(), viewModel.getPassword());
                } else {
                    webCredentialsUtils.clearCredentials(viewModel.getUrl());
                }
            }

            downloadFormsTask.execute(filesToDownload);
        } else {
            ToastUtils.showShortToast(R.string.noselect_error);
        }
    }

    @Override
    public void formsDownloadingComplete(Map<ServerFormDetails, String> result) {
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(null);
        }

        cleanUpWebCredentials();

        DialogUtils.dismissDialog(RefreshFormListDialogFragment.class, getSupportFragmentManager());

        // Set result to true for forms which were downloaded
        if (viewModel.isDownloadOnlyMode()) {
            for (ServerFormDetails serverFormDetails : result.keySet()) {
                String successKey = result.get(serverFormDetails);
                if (getString(R.string.success).equals(successKey)) {
                    if (viewModel.getFormResults().containsKey(serverFormDetails.getFormId())) {
                        viewModel.putFormResult(serverFormDetails.getFormId(), true);
                    }
                }
            }

            setReturnResult(true, null, viewModel.getFormResults());
        }
        createAlertDialog(getString(R.string.download_forms_result), getDownloadResultMessage(result), EXIT);
    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        RefreshFormListDialogFragment fragment = (RefreshFormListDialogFragment) getSupportFragmentManager().findFragmentByTag(RefreshFormListDialogFragment.class.getName());

        if (fragment != null) {
            fragment.setMessage(getString(R.string.fetching_file, currentFile,
                    String.valueOf(progress), String.valueOf(total)));
        }
    }

    @Override
    public void formsDownloadingCancelled() {
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(null);
            downloadFormsTask = null;
        }
        cleanUpWebCredentials();

        if (cancelDialog != null && cancelDialog.isShowing()) {
            cancelDialog.dismiss();
            viewModel.setCancelDialogShowing(false);
        }
        if (viewModel.isDownloadOnlyMode()) {
            setReturnResult(false, "Download cancelled", null);
            finish();
        }
    }

    private void cleanUpWebCredentials() {
        if (viewModel.getUrl() != null) {
            String host = Uri.parse(viewModel.getUrl())
                    .getHost();

            if (host != null) {
                webCredentialsUtils.clearCredentials(viewModel.getUrl());
            }
        }
    }

    /**
     * Creates an alert dialog with the given tite and message. If shouldExit is set to true, the
     * activity will exit when the user clicks "ok".
     */
    private void createAlertDialog(String title, String message, final boolean shouldExit) {
        Intent i = new Intent(getBaseContext(), MainMenuActivity.class);
        i.putExtra("formFormat", formFormat);
        startActivity(i);
    }

    public static String getDownloadResultMessage(Map<ServerFormDetails, String> result) {
        Set<ServerFormDetails> keys = result.keySet();
        StringBuilder b = new StringBuilder();
        for (ServerFormDetails k : keys) {
            b.append(k.getFormName() + " ("
                    + ((k.getFormVersion() != null)
                    ? (TranslationHandler.getString(Collect.getInstance(), R.string.version) + ": " + k.getFormVersion() + " ")
                    : "") + "ID: " + k.getFormId() + ") - " + result.get(k));
            b.append("\n\n");
        }

        return b.toString().trim();
    }
}
