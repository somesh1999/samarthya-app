package org.samarthya.collect.android.support.pages;

import org.samarthya.collect.android.R;

public class FormMapPage extends Page<FormMapPage> {

    @Override
    public FormMapPage assertOnPage() {
        return checkIsIdDisplayed(R.id.geometry_status);
    }

    public FormEntryPage clickFillBlankFormButton(String formName) {
        clickOnId(R.id.new_instance);
        return new FormEntryPage(formName).assertOnPage();
    }
}
