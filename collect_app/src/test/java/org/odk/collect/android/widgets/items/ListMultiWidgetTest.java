package org.samarthya.collect.android.widgets.items;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.javarosa.core.model.SelectChoice;
import org.junit.Test;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.formentry.questions.QuestionDetails;
import org.samarthya.collect.android.support.MockFormEntryPromptBuilder;
import org.samarthya.collect.android.widgets.base.GeneralSelectMultiWidgetTest;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ListMultiWidgetTest extends GeneralSelectMultiWidgetTest<ListMultiWidget> {
    @NonNull
    @Override
    public ListMultiWidget createWidget() {
        return new ListMultiWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), true);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        for (CheckBox checkBox : getSpyWidget().checkBoxes) {
            assertThat(checkBox.getVisibility(), is(View.VISIBLE));
            assertThat(checkBox.isEnabled(), is(Boolean.FALSE));
        }
    }

    @Test
    public void whenSpacesInUnderlyingValuesExist_shouldAppropriateWarningBeDisplayed() {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withSelectChoices(asList(
                        new SelectChoice("a", "a a"),
                        new SelectChoice("a", "b b")
                ))
                .build();

        TextView warningTv = getWidget().findViewById(R.id.warning_text);
        assertThat(warningTv.getVisibility(), is(View.VISIBLE));
        assertThat(warningTv.getText(), is("Warning: underlying values a a, b b have spaces"));
    }
}
