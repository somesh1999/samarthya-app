package org.samarthya.collect.android.support.pages;

import org.samarthya.collect.android.R;

public class ExperimentalPage extends Page<ExperimentalPage> {

    @Override
    public ExperimentalPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(R.string.experimental));
        return this;
    }
}
