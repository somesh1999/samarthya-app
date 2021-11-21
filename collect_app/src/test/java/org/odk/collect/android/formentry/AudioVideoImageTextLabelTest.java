package org.samarthya.collect.android.formentry;

import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.audio.AudioButton;
import org.samarthya.collect.android.audio.AudioHelper;
import org.samarthya.collect.android.formentry.questions.AudioVideoImageTextLabel;
import org.samarthya.collect.android.support.TestScreenContextActivity;
import org.robolectric.shadows.ShadowToast;

import java.io.File;

import static android.view.View.VISIBLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.samarthya.collect.android.support.CollectHelpers.createThemedActivity;

@RunWith(AndroidJUnit4.class)
public class AudioVideoImageTextLabelTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public AudioHelper audioHelper;

    private TestScreenContextActivity activity;

    @Before
    public void setup() {
        activity = createThemedActivity(TestScreenContextActivity.class);
    }

    @Test
    public void withNullText_hidesTextLabel() {
        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setText(null, false, 16);

        assertThat(audioVideoImageTextLabel.getLabelTextView().getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void withBlankText_hidesTextLabel() {
        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setText("", false, 16);

        assertThat(audioVideoImageTextLabel.getLabelTextView().getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void withText_andAudio_showsTextAndAudioButton()  {
        MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
        when(audioHelper.setAudio(any(AudioButton.class), any())).thenReturn(isPlaying);

        AudioVideoImageTextLabel label = new AudioVideoImageTextLabel(activity);
        label.setText("blah", false, 16);
        label.setAudio("file://audio.mp3", audioHelper);

        assertThat(label.getLabelTextView().getVisibility(), equalTo(VISIBLE));
        assertThat(label.getLabelTextView().getText().toString(), equalTo("blah"));
        assertThat(label.findViewById(R.id.audioButton).getVisibility(), equalTo(VISIBLE));
    }

    @Test
    public void withText_andAudio_playingAudio_highlightsText() {
        MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
        when(audioHelper.setAudio(any(AudioButton.class), any())).thenReturn(isPlaying);

        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setText("blah", false, 16);
        audioVideoImageTextLabel.setAudio("file://audio.mp3", audioHelper);

        int originalTextColor = audioVideoImageTextLabel.getLabelTextView().getCurrentTextColor();

        isPlaying.setValue(true);
        int textColor = audioVideoImageTextLabel.getLabelTextView().getCurrentTextColor();
        assertThat(textColor, not(equalTo(originalTextColor)));

        isPlaying.setValue(false);
        textColor = audioVideoImageTextLabel.getLabelTextView().getCurrentTextColor();
        assertThat(textColor, equalTo(originalTextColor));
    }

    @Test
    public void bothClickingLabelAndImageView_shouldSelectOptionInSelectOneMode() {
        File imageFile = mock(File.class);
        when(imageFile.exists()).thenReturn(true);

        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setImage(imageFile);
        audioVideoImageTextLabel.setTextView(new RadioButton(activity));

        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(false));

        // click on label
        audioVideoImageTextLabel.getLabelTextView().performClick();
        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));

        // clear answer
        ((RadioButton) audioVideoImageTextLabel.getLabelTextView()).setChecked(false);
        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(false));

        // click on image
        audioVideoImageTextLabel.getImageView().performClick();
        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));
    }

    @Test
    public void bothClickingLabelAndImageView_shouldSelectOptionInSelectMultiMode() {
        File imageFile = mock(File.class);
        when(imageFile.exists()).thenReturn(true);

        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setImage(imageFile);
        audioVideoImageTextLabel.setTextView(new CheckBox(activity));

        assertThat(((CheckBox) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(false));

        // click on label
        audioVideoImageTextLabel.getLabelTextView().performClick();
        assertThat(((CheckBox) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));

        // click on image
        audioVideoImageTextLabel.getImageView().performClick();
        assertThat(((CheckBox) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(false));
    }

    @Test
    public void whenClickOneElementTwiceInSelectOneMode_shouldThatElementRemainSelected() {
        File imageFile = mock(File.class);
        when(imageFile.exists()).thenReturn(true);

        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setImage(imageFile);
        audioVideoImageTextLabel.setTextView(new RadioButton(activity));

        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(false));

        // click on label
        audioVideoImageTextLabel.getLabelTextView().performClick();
        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));

        // click on label again
        audioVideoImageTextLabel.getLabelTextView().performClick();
        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));

        // click on image
        audioVideoImageTextLabel.getImageView().performClick();
        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));

        // click on image again
        audioVideoImageTextLabel.getImageView().performClick();
        assertThat(((RadioButton) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));
    }

    @Test
    public void whenClickOneElementTwiceInSelectMultiMode_shouldThatElementBeUnSelected() {
        File imageFile = mock(File.class);
        when(imageFile.exists()).thenReturn(true);

        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setImage(imageFile);
        audioVideoImageTextLabel.setTextView(new CheckBox(activity));

        assertThat(((CheckBox) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(false));

        // click on label
        audioVideoImageTextLabel.getLabelTextView().performClick();
        assertThat(((CheckBox) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));

        // click on label again
        audioVideoImageTextLabel.getLabelTextView().performClick();
        assertThat(((CheckBox) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(false));

        // click on image
        audioVideoImageTextLabel.getImageView().performClick();
        assertThat(((CheckBox) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(true));

        // click on image again
        audioVideoImageTextLabel.getImageView().performClick();
        assertThat(((CheckBox) audioVideoImageTextLabel.getLabelTextView()).isChecked(), is(false));
    }

    @Test
    public void whenImageFileDoesNotExist_ShouldAnAppropriateMessageBeDisplayed() {
        File imageFile = new File("file://image.png");

        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setImage(imageFile);

        assertThat(audioVideoImageTextLabel.getMissingImage().getVisibility(), is(VISIBLE));
        assertThat(audioVideoImageTextLabel.getMissingImage().getText().toString(), is("File: file:/image.png is missing."));
    }

    @Test
    public void whenVideoFileDoesNotExist_ShouldAnAppropriateMessageBeDisplayed() {
        File videoFile = new File("file://video.mp4");

        AudioVideoImageTextLabel audioVideoImageTextLabel = new AudioVideoImageTextLabel(activity);
        audioVideoImageTextLabel.setVideo(videoFile);
        audioVideoImageTextLabel.getVideoButton().performClick();

        assertEquals(ShadowToast.getTextOfLatestToast(), "File: file:/video.mp4 is missing.");
        assertEquals(ShadowToast.getLatestToast().getDuration(), Toast.LENGTH_LONG);
    }
}
