package org.samarthya.collect.android.support.pages;

import org.samarthya.collect.android.R;

public class MapsSettingsPage extends Page<MapsSettingsPage> {

    @Override
    public MapsSettingsPage assertOnPage() {
        assertText(R.string.maps);
        return this;
    }
}
