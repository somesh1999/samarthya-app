package org.samarthya.collect.android.formentry.loading;

import com.google.common.io.Files;

import org.junit.Test;
import org.samarthya.collect.android.storage.StoragePathProvider;
import org.samarthya.collect.android.storage.StorageSubdirectory;
import org.samarthya.collect.utilities.Clock;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormInstanceFileCreatorTest {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);
    private final StoragePathProvider pathProvider = mock(StoragePathProvider.class);
    private final Clock clock = mock(Clock.class);

    @Test
    public void createsDirectory_basedOnDefinitionPathAndCurrentTime_inInstancesDirectory() throws Exception {
        String instancesDir = Files.createTempDir().getAbsolutePath();
        when(pathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES)).thenReturn(instancesDir);
        when(clock.getCurrentTime()).thenReturn(simpleDateFormat.parse("1990-04-24_00-00-00").getTime());

        FormInstanceFileCreator instanceFileCreator = new FormInstanceFileCreator(pathProvider, clock);
        instanceFileCreator.createInstanceFile("/blah/blah/Cool form name.xml");

        File instanceDir = new File(instancesDir + File.separator + "Cool form name_1990-04-24_00-00-00");
        assertThat(instanceDir.exists(), is(true));
        assertThat(instanceDir.isDirectory(), is(true));
    }

    @Test
    public void returnsInstanceFile_inInstanceDirectory() throws Exception {
        String instancesDir = Files.createTempDir().getAbsolutePath();
        when(pathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES)).thenReturn(instancesDir);
        when(clock.getCurrentTime()).thenReturn(simpleDateFormat.parse("1990-04-24_00-00-00").getTime());

        FormInstanceFileCreator instanceFileCreator = new FormInstanceFileCreator(pathProvider, clock);
        File instanceFile = instanceFileCreator.createInstanceFile("/blah/blah/Cool form name.xml");

        String instanceDir = instancesDir + File.separator + "Cool form name_1990-04-24_00-00-00";
        assertThat(instanceFile.getAbsolutePath(), is(instanceDir + File.separator + "Cool form name_1990-04-24_00-00-00.xml"));
    }

    @Test
    public void whenCreatingDirFails_returnsNull() throws Exception {
        File tempFile = File.createTempFile("not-a", "dir"); // Create a file where it needs a dir
        when(pathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES)).thenReturn(tempFile.getAbsolutePath());
        when(clock.getCurrentTime()).thenReturn(simpleDateFormat.parse("1990-04-24_00-00-00").getTime());

        FormInstanceFileCreator instanceFileCreator = new FormInstanceFileCreator(pathProvider, clock);
        File instanceFile = instanceFileCreator.createInstanceFile("/blah/blah/Cool form name.xml");
        assertThat(instanceFile, is(nullValue()));
    }
}