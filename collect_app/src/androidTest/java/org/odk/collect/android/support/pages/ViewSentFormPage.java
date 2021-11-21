package org.samarthya.collect.android.support.pages;

import org.samarthya.collect.android.R;
import org.samarthya.collect.android.database.forms.DatabaseFormColumns;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;

public class ViewSentFormPage extends Page<ViewSentFormPage> {

    @Override
    public ViewSentFormPage assertOnPage() {
        assertToolbarTitle(R.string.view_sent_forms);
        return this;
    }

    public FormHierarchyPage clickOnForm(String formName) {
        onData(withRowString(DatabaseFormColumns.DISPLAY_NAME, formName)).perform(click());
        return new FormHierarchyPage(formName);
    }
}
