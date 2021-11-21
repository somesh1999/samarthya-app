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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.samarthya.collect.android.R;
import org.samarthya.collect.android.activities.FormEntryActivity;
import org.samarthya.collect.android.activities.FormListActivity;
import org.samarthya.collect.android.activities.FormMapActivity;
import org.samarthya.collect.android.activities.MainMenuActivity;
import org.samarthya.collect.android.adapters.FormListAdapter;
import org.samarthya.collect.android.dao.CursorLoaderFactory;
import org.samarthya.collect.android.database.forms.DatabaseFormColumns;
import org.samarthya.collect.android.external.FormsContract;
import org.samarthya.collect.android.formmanagement.BlankFormListMenuDelegate;
import org.samarthya.collect.android.formmanagement.BlankFormsListViewModel;
import org.samarthya.collect.android.injection.DaggerUtils;
import org.samarthya.collect.android.listeners.DiskSyncListener;
import org.samarthya.collect.android.listeners.PermissionListener;
import org.samarthya.collect.android.network.NetworkStateProvider;
import org.samarthya.collect.android.preferences.dialogs.ServerAuthDialogFragment;
import org.samarthya.collect.android.preferences.keys.ProjectKeys;
import org.samarthya.collect.android.projects.CurrentProjectProvider;
import org.samarthya.collect.android.tasks.FormSyncTask;
import org.samarthya.collect.android.utilities.ApplicationConstants;
import org.samarthya.collect.android.utilities.DialogUtils;
import org.samarthya.collect.android.utilities.MultiClickGuard;
import org.samarthya.collect.android.views.ObviousProgressBar;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores the path to
 * selected form for use by {@link MainMenuActivity}.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FillBlankFormActivity extends FormListActivity implements
        DiskSyncListener, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String FORM_CHOOSER_LIST_SORTING_ORDER = "formChooserListSortingOrder";

    private FormSyncTask formSyncTask;

    @Inject
    NetworkStateProvider networkStateProvider;

    @Inject
    BlankFormsListViewModel.Factory blankFormsListViewModelFactory;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    BlankFormListMenuDelegate menuDelegate;

    String formFormat = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_chooser_list);
        DaggerUtils.getComponent(this).inject(this);

        setTitle(getString(R.string.enter_data));

        Intent intent = getIntent();
        formFormat = intent.getStringExtra("formFormat");

        BlankFormsListViewModel blankFormsListViewModel = new ViewModelProvider(this, blankFormsListViewModelFactory).get(BlankFormsListViewModel.class);
        blankFormsListViewModel.isSyncing().observe(this, syncing -> {
            ObviousProgressBar progressBar = findViewById(R.id.progressBar);

            if (syncing) {
                progressBar.show();
            } else {
                progressBar.hide(View.GONE);
            }
        });

        blankFormsListViewModel.isAuthenticationRequired().observe(this, authenticationRequired -> {
            if (authenticationRequired) {
                DialogUtils.showIfNotShowing(ServerAuthDialogFragment.class, getSupportFragmentManager());
            } else {
                DialogUtils.dismissDialog(ServerAuthDialogFragment.class, getSupportFragmentManager());
            }
        });

        menuDelegate = new BlankFormListMenuDelegate(this, blankFormsListViewModel, networkStateProvider);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menuDelegate.onCreateOptionsMenu(getMenuInflater(), menu);
        return result;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        menuDelegate.onPrepareOptionsMenu(menu);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        if (super.onOptionsItemSelected(item)) {
            return true;
        } else {
            return menuDelegate.onOptionsItemSelected(item);
        }
    }

    private void init() {
        setupAdapter();

        // DiskSyncTask checks the disk for any forms not already in the content provider
        // that is, put here by dragging and dropping onto the SDCard
        formSyncTask = (FormSyncTask) getLastCustomNonConfigurationInstance();
        if (formSyncTask == null) {
            Timber.i("Starting new disk sync task");
            formSyncTask = new FormSyncTask();
            formSyncTask.setDiskSyncListener(this);
            formSyncTask.execute((Void[]) null);
        }
        sortingOptions = new int[]{
                R.string.sort_by_name_asc, R.string.sort_by_name_desc,
                R.string.sort_by_date_asc, R.string.sort_by_date_desc,
        };

        setupAdapter();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        // pass the thread on restart
        return formSyncTask;
    }

    /**
     * Stores the path of selected form and finishes.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            // get uri to form
            long idFormsTable = listView.getAdapter().getItemId(position);
            Uri formUri = FormsContract.getUri(currentProjectProvider.getCurrentProject().getUuid(), idFormsTable);

            String action = getIntent().getAction();
            if (Intent.ACTION_PICK.equals(action)) {
                // caller is waiting on a picked form
                setResult(RESULT_OK, new Intent().setData(formUri));
            } else {
                // caller wants to view/edit a form, so launch formentryactivity
                Intent intent = new Intent(this, FormEntryActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.setData(formUri);
                intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                startActivity(intent);
            }

            finish();
        }
    }

    public void onMapButtonClick(AdapterView<?> parent, View view, int position, long id) {
        final Intent intent = new Intent(this, org.samarthya.collect.android.activities.FormMapActivity.class);
        intent.putExtra(FormMapActivity.EXTRA_FORM_ID, id);

        permissionsProvider.requestLocationPermissions(this, new PermissionListener() {
            @Override
            public void granted() {
                startActivity(intent);
            }

            @Override
            public void denied() {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (formSyncTask != null) {
            formSyncTask.setDiskSyncListener(this);
            if (formSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
                syncComplete(formSyncTask.getStatusMessage());
            }
        }
    }

    @Override
    protected void onPause() {
        if (formSyncTask != null) {
            formSyncTask.setDiskSyncListener(null);
        }
        super.onPause();
    }

    /**
     * Called by DiskSyncTask when the task is finished
     */
    @Override
    public void syncComplete(@NonNull String result) {
        Timber.i("Disk scan complete");
        hideProgressBarAndAllow();
        showSnackbar(result);
    }

    private void setupAdapter() {
        String[] columnNames = {
                DatabaseFormColumns.DISPLAY_NAME,
                DatabaseFormColumns.JR_VERSION,
                DatabaseFormColumns.DATE,
                DatabaseFormColumns.GEOMETRY_XPATH
        };
        int[] viewIds = {
                R.id.form_title,
                R.id.form_subtitle,
                R.id.form_subtitle2,
                R.id.map_view
        };

        listAdapter = new FormListAdapter(
                listView, DatabaseFormColumns.JR_VERSION, this, R.layout.form_chooser_list_item,
                this::onMapButtonClick, columnNames, viewIds);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected String getSortingOrderKey() {
        return FORM_CHOOSER_LIST_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressBar();

        return new CursorLoaderFactory(currentProjectProvider).getFormsCursorLoader(getFilterText(), getSortingOrder(), hideOldFormVersions(), formFormat);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarIfAllowed();
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        listAdapter.swapCursor(null);
    }

    private boolean hideOldFormVersions() {
        return settingsProvider.getGeneralSettings().getBoolean(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS);
    }
}
