package org.odk.collect.android.widgets;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.StringData;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ExternalAppIntentProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.shadows.ShadowToast;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.utilities.QuestionFontSizeUtils.DEFAULT_FONT_SIZE;
import static org.robolectric.Shadows.shadowOf;

public class ExImageWidgetTest extends FileWidgetTest<ExImageWidget> {
    @Mock
    MediaUtils mediaUtils;

    @Mock
    ExternalAppIntentProvider externalAppIntentProvider;

    @Before
    public void setup() {
        when(mediaUtils.isImageFile(any())).thenReturn(true);
    }

    @Override
    public StringData getInitialAnswer() {
        return new StringData("image1.png");
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData("image2.png");
    }

    @NonNull
    @Override
    public ExImageWidget createWidget() {
        return new ExImageWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID", readOnlyOverride),
                new FakeQuestionMediaManager(), new FakeWaitingForDataRegistry(), mediaUtils, externalAppIntentProvider, new ActivityAvailability(activity));
    }

    @Test
    public void whenWidgetCreated_shouldLaunchButtonBeVisible() {
        assertThat(getWidget().binding.launchExternalAppButton.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenWidgetCreated_shouldLaunchButtonHaveProperName() {
        assertThat(getWidget().binding.launchExternalAppButton.getText(), is("Launch"));
    }

    @Test
    public void whenFontSizeNotChanged_defaultFontSizeShouldBeUsed() {
        assertThat((int) getWidget().binding.launchExternalAppButton.getTextSize(), is(DEFAULT_FONT_SIZE - 1));
    }

    @Test
    public void whenFontSizeChanged_CustomFontSizeShouldBeUsed() {
        settingsProvider.getGeneralSettings().save(KEY_FONT_SIZE, "30");

        assertThat((int) getWidget().binding.launchExternalAppButton.getTextSize(), is(29));
    }

    @Test
    public void whenThereIsNoAnswer_shouldImageViewBeHidden() {
        assertThat(getWidget().binding.imageView.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenThereIsAnswer_shouldImageViewBeDisplayed() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        assertThat(getWidget().binding.imageView.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenAnswerCleared_shouldImageViewBeHidden() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        widget.clearAnswer();
        assertThat(getWidget().binding.imageView.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenLaunchButtonClicked_externalAppShouldBeLaunchedByIntent() throws ExternalParamsException, XPathSyntaxException {
        Intent intent = mock(Intent.class);
        when(externalAppIntentProvider.getIntentToRunExternalApp(any(), any(), any(), any())).thenReturn(intent);
        getWidget().binding.launchExternalAppButton.performClick();
        assertThat(shadowOf(activity).getNextStartedActivity(), is(intent));
    }

    @Test
    public void whenImageViewClicked_shouldFileViewerByCalled() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        widget.binding.imageView.performClick();
        verify(mediaUtils).openFile(activity, widget.answerFile, "image/*");
    }

    @Test
    public void whenSetDataCalledWithNull_shouldExistedAnswerBeRemoved() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        widget.setData(null);
        assertThat(widget.getAnswer(), is(nullValue()));
        assertThat(widget.binding.imageView.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldNotThatFileBeAdded() throws IOException {
        ExImageWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isImageFile(answer)).thenReturn(false);
        widget.setData(answer);
        assertThat(widget.getAnswer(), is(nullValue()));
        assertThat(widget.binding.imageView.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldTheFileBeRemoved() throws IOException {
        ExImageWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isImageFile(answer)).thenReturn(false);
        widget.setData(answer);
        verify(mediaUtils).deleteMediaFile(answer.getAbsolutePath());
    }

    @Test
    public void whenUnsupportedFileTypeAttached_shouldToastBeDisplayed() throws IOException {
        ExImageWidget widget = getWidget();
        File answer = File.createTempFile("doc", ".pdf");
        when(mediaUtils.isImageFile(answer)).thenReturn(false);
        widget.setData(answer);
        assertThat(ShadowToast.getTextOfLatestToast(), is("Application returned an invalid file type"));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        assertThat(widget.binding.launchExternalAppButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.imageView.getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true;
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExImageWidget widget = getWidget();
        assertThat(widget.binding.launchExternalAppButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.imageView.getVisibility(), is(View.VISIBLE));
    }
}
