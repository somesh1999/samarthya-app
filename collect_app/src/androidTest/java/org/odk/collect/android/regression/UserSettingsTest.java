package org.samarthya.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.support.CollectTestRule;
import org.samarthya.collect.android.support.ResetStateRule;
import org.samarthya.collect.android.support.pages.MainMenuPage;

//Issue NODK-241
@RunWith(AndroidJUnit4.class)
public class UserSettingsTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(rule);

    @Test
    public void typeOption_ShouldNotBeVisible() {
        //TestCase1
        new MainMenuPage()
                .openProjectSettings()
                .clickGeneralSettings()
                .clickAccessControl()
                .openUserSettings()
                .assertTextDoesNotExist("Type")
                .assertTextDoesNotExist("Submission transport")
                .assertText(R.string.server_settings_title);
    }
}
