package org.samarthya.collect.android.support.pages;

import org.samarthya.collect.android.R;

public class ErrorDialog extends OkDialog {

    @Override
    public ErrorDialog assertOnPage() {
        super.assertOnPage();
        assertText(R.string.error_occured);
        return this;
    }
}
