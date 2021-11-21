package org.samarthya.collect.android.support.pages;

import org.samarthya.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class GetBlankFormPage extends Page<GetBlankFormPage> {

    @Override
    public GetBlankFormPage assertOnPage() {
        onView(withText(getTranslatedString(R.string.get_forms))).check(matches(isDisplayed()));
        return this;
    }

    public OkDialog clickGetSelected() {
        onView(withText(getTranslatedString(R.string.download))).perform(click());
        return new OkDialog().assertOnPage();
    }
}
