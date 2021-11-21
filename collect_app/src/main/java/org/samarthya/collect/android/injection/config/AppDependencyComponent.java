package org.samarthya.collect.android.injection.config;

import android.app.Application;

import org.javarosa.core.reference.ReferenceManager;
import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.activities.LaunchActivity;
import org.samarthya.collect.android.external.AndroidShortcutsActivity;
import org.samarthya.collect.android.activities.CollectAbstractActivity;
import org.samarthya.collect.android.activities.DeleteSavedFormActivity;
import org.samarthya.collect.android.activities.FillBlankFormActivity;
import org.samarthya.collect.android.activities.FormDownloadListActivity;
import org.samarthya.collect.android.activities.FormEntryActivity;
import org.samarthya.collect.android.activities.FormHierarchyActivity;
import org.samarthya.collect.android.activities.FormMapActivity;
import org.samarthya.collect.android.activities.GeoPointMapActivity;
import org.samarthya.collect.android.activities.GeoPolyActivity;
import org.samarthya.collect.android.activities.InstanceChooserList;
import org.samarthya.collect.android.activities.InstanceUploaderActivity;
import org.samarthya.collect.android.activities.InstanceUploaderListActivity;
import org.samarthya.collect.android.activities.MainMenuActivity;
import org.samarthya.collect.android.activities.SplashScreenActivity;
import org.samarthya.collect.android.adapters.InstanceUploaderAdapter;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.application.initialization.ApplicationInitializer;
import org.samarthya.collect.android.audio.AudioRecordingControllerFragment;
import org.samarthya.collect.android.audio.AudioRecordingErrorDialogFragment;
import org.samarthya.collect.android.backgroundwork.AutoSendTaskSpec;
import org.samarthya.collect.android.backgroundwork.AutoUpdateTaskSpec;
import org.samarthya.collect.android.backgroundwork.SyncFormsTaskSpec;
import org.samarthya.collect.android.configure.SettingsImporter;
import org.samarthya.collect.android.configure.qr.QRCodeScannerFragment;
import org.samarthya.collect.android.configure.qr.QRCodeTabsActivity;
import org.samarthya.collect.android.configure.qr.ShowQRCodeFragment;
import org.samarthya.collect.android.external.FormUriActivity;
import org.samarthya.collect.android.formentry.BackgroundAudioPermissionDialogFragment;
import org.samarthya.collect.android.formentry.ODKView;
import org.samarthya.collect.android.formentry.QuitFormDialogFragment;
import org.samarthya.collect.android.formentry.saving.SaveAnswerFileErrorDialogFragment;
import org.samarthya.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.samarthya.collect.android.formmanagement.FormSourceProvider;
import org.samarthya.collect.android.formmanagement.FormsUpdater;
import org.samarthya.collect.android.formmanagement.InstancesAppState;
import org.samarthya.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.samarthya.collect.android.fragments.AppListFragment;
import org.samarthya.collect.android.fragments.BarCodeScannerFragment;
import org.samarthya.collect.android.fragments.BlankFormListFragment;
import org.samarthya.collect.android.fragments.MapBoxInitializationFragment;
import org.samarthya.collect.android.fragments.SavedFormListFragment;
import org.samarthya.collect.android.activities.FirstLaunchActivity;
import org.samarthya.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.samarthya.collect.android.gdrive.GoogleDriveActivity;
import org.samarthya.collect.android.gdrive.GoogleSheetsUploaderActivity;
import org.samarthya.collect.android.geo.GoogleMapFragment;
import org.samarthya.collect.android.geo.MapboxMapFragment;
import org.samarthya.collect.android.geo.OsmDroidMapFragment;
import org.samarthya.collect.android.injection.config.AppDependencyModule;
import org.samarthya.collect.android.logic.PropertyManager;
import org.samarthya.collect.android.openrosa.OpenRosaHttpInterface;
import org.samarthya.collect.android.preferences.CaptionedListPreference;
import org.samarthya.collect.android.preferences.dialogs.AdminPasswordDialogFragment;
import org.samarthya.collect.android.preferences.dialogs.ChangeAdminPasswordDialog;
import org.samarthya.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat;
import org.samarthya.collect.android.preferences.dialogs.ServerAuthDialogFragment;
import org.samarthya.collect.android.preferences.screens.BaseAdminPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.BaseProjectPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.BasePreferencesFragment;
import org.samarthya.collect.android.preferences.screens.ExperimentalPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.FormManagementPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.FormMetadataPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.ProjectPreferencesActivity;
import org.samarthya.collect.android.preferences.screens.ProjectPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.IdentityPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.ProjectDisplayPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.ProjectManagementPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.ServerPreferencesFragment;
import org.samarthya.collect.android.preferences.screens.UserInterfacePreferencesFragment;
import org.samarthya.collect.android.preferences.source.SettingsProvider;
import org.samarthya.collect.android.projects.QrCodeProjectCreatorDialog;
import org.samarthya.collect.android.projects.CurrentProjectProvider;
import org.samarthya.collect.android.application.initialization.ExistingProjectMigrator;
import org.samarthya.collect.android.projects.ProjectImporter;
import org.samarthya.collect.android.projects.ProjectSettingsDialog;
import org.samarthya.collect.android.external.FormsProvider;
import org.samarthya.collect.android.external.InstanceProvider;
import org.samarthya.collect.android.storage.StorageInitializer;
import org.samarthya.collect.android.storage.StoragePathProvider;
import org.samarthya.collect.android.tasks.InstanceServerUploaderTask;
import org.samarthya.collect.android.tasks.MediaLoadingTask;
import org.samarthya.collect.android.upload.InstanceUploader;
import org.samarthya.collect.android.utilities.AuthDialogUtility;
import org.samarthya.collect.android.utilities.FormsRepositoryProvider;
import org.samarthya.collect.android.utilities.InstancesRepositoryProvider;
import org.samarthya.collect.android.utilities.ProjectResetter;
import org.samarthya.collect.android.utilities.ThemeUtils;
import org.samarthya.collect.android.widgets.ExStringWidget;
import org.samarthya.collect.android.widgets.QuestionWidget;
import org.samarthya.collect.android.projects.ManualProjectCreatorDialog;
import org.samarthya.collect.projects.ProjectsRepository;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/**
 * Dagger component for the application. Should include
 * application level Dagger Modules and be built with Application
 * object.
 * <p>
 * Add an `inject(MyClass myClass)` method here for objects you want
 * to inject into so Dagger knows to wire it up.
 * <p>
 * Annotated with @Singleton so modules can include @Singletons that will
 * be retained at an application level (as this an instance of this components
 * is owned by the Application object).
 * <p>
 * If you need to call a provider directly from the component (in a test
 * for example) you can add a method with the type you are looking to fetch
 * (`MyType myType()`) to this interface.
 * <p>
 * To read more about Dagger visit: https://google.github.io/dagger/users-guide
 **/

@Singleton
@Component(modules = {
        org.samarthya.collect.android.injection.config.AppDependencyModule.class
})
public interface AppDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        Builder appDependencyModule(AppDependencyModule testDependencyModule);

        AppDependencyComponent build();
    }

    void inject(Collect collect);

    void inject(InstanceUploaderAdapter instanceUploaderAdapter);

    void inject(SavedFormListFragment savedFormListFragment);

    void inject(PropertyManager propertyManager);

    void inject(FormEntryActivity formEntryActivity);

    void inject(InstanceServerUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(ProjectDisplayPreferencesFragment projectDisplayPreferencesFragment);

    void inject(ProjectManagementPreferencesFragment projectManagementPreferencesFragment);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(InstanceUploaderListActivity activity);

    void inject(GoogleDriveActivity googleDriveActivity);

    void inject(GoogleSheetsUploaderActivity googleSheetsUploaderActivity);

    void inject(QuestionWidget questionWidget);

    void inject(ExStringWidget exStringWidget);

    void inject(ODKView odkView);

    void inject(FormMetadataPreferencesFragment formMetadataPreferencesFragment);

    void inject(GeoPointMapActivity geoMapActivity);

    void inject(GeoPolyActivity geoPolyActivity);

    void inject(FormMapActivity formMapActivity);

    void inject(OsmDroidMapFragment mapFragment);

    void inject(GoogleMapFragment mapFragment);

    void inject(MapboxMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(LaunchActivity launchActivity);

    void inject(QRCodeTabsActivity qrCodeTabsActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(StorageInitializer storageInitializer);

    void inject(AutoSendTaskSpec autoSendTaskSpec);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(SplashScreenActivity splashScreenActivity);

    void inject(FormHierarchyActivity formHierarchyActivity);

    void inject(FormManagementPreferencesFragment formManagementPreferencesFragment);

    void inject(IdentityPreferencesFragment identityPreferencesFragment);

    void inject(UserInterfacePreferencesFragment userInterfacePreferencesFragment);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    void inject(QuitFormDialogFragment quitFormDialogFragment);

    void inject(BarCodeScannerFragment barCodeScannerFragment);

    void inject(QRCodeScannerFragment qrCodeScannerFragment);

    void inject(ProjectPreferencesActivity projectPreferencesActivity);

    void inject(ResetDialogPreferenceFragmentCompat resetDialogPreferenceFragmentCompat);

    void inject(FillBlankFormActivity fillBlankFormActivity);

    void inject(MapBoxInitializationFragment mapBoxInitializationFragment);

    void inject(SyncFormsTaskSpec syncWork);

    void inject(ExperimentalPreferencesFragment experimentalPreferencesFragment);

    void inject(AutoUpdateTaskSpec autoUpdateTaskSpec);

    void inject(ServerAuthDialogFragment serverAuthDialogFragment);

    void inject(BasePreferencesFragment basePreferencesFragment);

    void inject(BlankFormListFragment blankFormListFragment);

    void inject(InstanceUploaderActivity instanceUploaderActivity);

    void inject(ProjectPreferencesFragment projectPreferencesFragment);

    void inject(DeleteSavedFormActivity deleteSavedFormActivity);

    void inject(SelectMinimalDialog selectMinimalDialog);

    void inject(AudioRecordingControllerFragment audioRecordingControllerFragment);

    void inject(SaveAnswerFileErrorDialogFragment saveAnswerFileErrorDialogFragment);

    void inject(AudioRecordingErrorDialogFragment audioRecordingErrorDialogFragment);

    void inject(CollectAbstractActivity collectAbstractActivity);

    void inject(InstanceChooserList instanceChooserList);

    void inject(FormsProvider formsProvider);

    void inject(InstanceProvider instanceProvider);

    void inject(BackgroundAudioPermissionDialogFragment backgroundAudioPermissionDialogFragment);

    void inject(AppListFragment appListFragment);

    void inject(ChangeAdminPasswordDialog changeAdminPasswordDialog);

    void inject(MediaLoadingTask mediaLoadingTask);

    void inject(ThemeUtils themeUtils);

    void inject(BaseProjectPreferencesFragment baseProjectPreferencesFragment);

    void inject(BaseAdminPreferencesFragment baseAdminPreferencesFragment);

    void inject(CaptionedListPreference captionedListPreference);

    void inject(AndroidShortcutsActivity androidShortcutsActivity);

    void inject(ProjectSettingsDialog projectSettingsDialog);

    void inject(ManualProjectCreatorDialog manualProjectCreatorDialog);

    void inject(QrCodeProjectCreatorDialog qrCodeProjectCreatorDialog);

    void inject(FirstLaunchActivity firstLaunchActivity);

    void inject(InstanceUploader instanceUploader);

    void inject(FormUriActivity formUriActivity);

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    Analytics analytics();

    SettingsProvider settingsProvider();

    ApplicationInitializer applicationInitializer();

    SettingsImporter settingsImporter();

    ProjectsRepository projectsRepository();

    CurrentProjectProvider currentProjectProvider();

    InstancesAppState instancesAppState();

    ProjectImporter projectImporter();

    StorageInitializer storageInitializer();

    StoragePathProvider storagePathProvider();

    FormsUpdater formUpdateChecker();

    FormsRepositoryProvider formsRepositoryProvider();

    InstancesRepositoryProvider instancesRepositoryProvider();

    SyncStatusAppState syncStatusAppState();

    FormSourceProvider formSourceProvider();

    ExistingProjectMigrator existingProjectMigrator();

    ProjectResetter projectResetter();
}
