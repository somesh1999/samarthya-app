package org.samarthya.collect.android.support;

import android.app.Application;

import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.injection.DaggerUtils;
import org.samarthya.collect.android.injection.config.AppDependencyComponent;
import org.samarthya.collect.android.injection.config.AppDependencyModule;
import org.samarthya.collect.android.injection.config.DaggerAppDependencyComponent;
import org.samarthya.collect.projects.Project;
import org.samarthya.collect.testshared.RobolectricHelpers;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CollectHelpers {

    private CollectHelpers() {

    }

    public static void overrideReferenceManager(ReferenceManager referenceManager) {
        overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }
        });
    }

    public static ReferenceManager setupFakeReferenceManager(List<Pair<String, String>> references) throws InvalidReferenceException {
        ReferenceManager referenceManager = mock(ReferenceManager.class);

        for (Pair<String, String> reference : references) {
            createFakeReference(referenceManager, reference.first, reference.second);
        }

        return referenceManager;
    }

    private static String createFakeReference(ReferenceManager referenceManager, String referenceURI, String localURI) throws InvalidReferenceException {
        Reference reference = mock(Reference.class);
        when(reference.getLocalURI()).thenReturn(localURI);
        when(referenceManager.deriveReference(referenceURI)).thenReturn(reference);

        return localURI;
    }

    public static void overrideAppDependencyModule(AppDependencyModule appDependencyModule) {
        AppDependencyComponent testComponent = DaggerAppDependencyComponent.builder()
                .application(ApplicationProvider.getApplicationContext())
                .appDependencyModule(appDependencyModule)
                .build();
        ((Collect) ApplicationProvider.getApplicationContext()).setComponent(testComponent);
    }

    public static void createThemedContext() {
        ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_Collect_Light);
    }

    public static <T extends FragmentActivity> T createThemedActivity(Class<T> clazz) {
        return RobolectricHelpers.createThemedActivity(clazz, R.style.Theme_Collect_Light);
    }

    public static FragmentActivity createThemedActivity() {
        return createThemedActivity(FragmentActivity.class);
    }

    public static <T extends FragmentActivity> ActivityController<T> buildThemedActivity(Class<T> clazz) {
        ActivityController<T> activity = Robolectric.buildActivity(clazz);
        activity.get().setTheme(R.style.Theme_Collect_Light);

        return activity;
    }

    public static String setupDemoProject() {
        createDemoProject();
        DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext()).currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);
        return Project.DEMO_PROJECT_ID;
    }

    public static String createDemoProject() {
        return createProject(Project.Companion.getDEMO_PROJECT());
    }

    public static String createProject(Project project) {
        Project.Saved savedProject = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext()).projectImporter().importNewProject(project);
        return savedProject.getUuid();
    }
}
