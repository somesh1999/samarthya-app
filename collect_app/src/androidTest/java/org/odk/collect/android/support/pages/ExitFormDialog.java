package org.samarthya.collect.android.support.pages;

import org.samarthya.collect.android.R;

public class ExitFormDialog extends Page<ExitFormDialog> {

    private final String formName;

    public ExitFormDialog(String formName) {
        this.formName = formName;
    }

    @Override
    public ExitFormDialog assertOnPage() {
        String title = getTranslatedString(R.string.exit) + " " + formName;
        assertText(title);
        return this;
    }
}
