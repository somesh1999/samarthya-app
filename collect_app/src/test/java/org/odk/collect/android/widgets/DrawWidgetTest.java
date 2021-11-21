package org.samarthya.collect.android.widgets;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.activities.DrawActivity;
import org.samarthya.collect.android.formentry.questions.QuestionDetails;
import org.samarthya.collect.android.support.MockFormEntryPromptBuilder;
import org.samarthya.collect.android.utilities.QuestionMediaManager;
import org.samarthya.collect.android.widgets.base.FileWidgetTest;
import org.samarthya.collect.android.widgets.support.FakeQuestionMediaManager;
import org.samarthya.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.samarthya.collect.shared.TempFiles;

import java.io.File;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.samarthya.collect.android.support.CollectHelpers.overrideReferenceManager;
import static org.samarthya.collect.android.support.CollectHelpers.setupFakeReferenceManager;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */
@RunWith(AndroidJUnit4.class)
public class DrawWidgetTest extends FileWidgetTest<DrawWidget> {

    //Package visibility for sharing with related tests
    static final String DEFAULT_IMAGE_ANSWER = "jr://images/referenceURI";
    static final String USER_SPECIFIED_IMAGE_ANSWER = "current.bmp";

    private File currentFile;

    @NonNull
    @Override
    public DrawWidget createWidget() {
        QuestionMediaManager fakeQuestionMediaManager = new FakeQuestionMediaManager() {
            @Override
            public File getAnswerFile(String fileName) {
                File result;
                if (currentFile == null) {
                    result = super.getAnswerFile(fileName);
                } else {
                    result = fileName.equals(USER_SPECIFIED_IMAGE_ANSWER) ? currentFile : null;
                }
                return result;
            }
        };
        return new DrawWidget(activity,
                new QuestionDetails(formEntryPrompt, "formAnalyticsID", readOnlyOverride),
                fakeQuestionMediaManager, new FakeWaitingForDataRegistry(), TempFiles.getPathInTempDir());
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Test
    public void buttonsShouldLaunchCorrectIntents() {
        stubAllRuntimePermissionsGranted(true);

        Intent intent = getIntentLaunchedByClick(R.id.simple_button);
        assertComponentEquals(activity, DrawActivity.class, intent);
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_DRAW, intent);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().drawButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.isReadOnly()).thenReturn(false);

        assertThat(getSpyWidget().drawButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenPromptHasDefaultAnswer_showsInImageView() throws Exception {
        String imagePath = File.createTempFile("default", ".bmp").getAbsolutePath();
        overrideReferenceManager(setupFakeReferenceManager(singletonList(
                new Pair<>(DEFAULT_IMAGE_ANSWER, imagePath)
        )));

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(DEFAULT_IMAGE_ANSWER)
                .build();

        DrawWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView, notNullValue());
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedPath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedPath, equalTo(imagePath));
    }

    @Test
    public void whenPromptHasCurrentAnswer_showsInImageView() throws Exception {
        String imagePath = File.createTempFile("current", ".bmp").getAbsolutePath();
        currentFile = new File(imagePath);

        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText(USER_SPECIFIED_IMAGE_ANSWER)
                .build();

        DrawWidget widget = createWidget();
        ImageView imageView = widget.getImageView();
        assertThat(imageView, notNullValue());
        Drawable drawable = imageView.getDrawable();
        assertThat(drawable, notNullValue());

        String loadedPath = shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromPath();
        assertThat(loadedPath, equalTo(imagePath));
    }
}
