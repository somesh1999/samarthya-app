package org.samarthya.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.support.CollectTestRule;
import org.samarthya.collect.android.support.CopyFormRule;
import org.samarthya.collect.android.support.ResetStateRule;
import org.samarthya.collect.android.support.pages.MainMenuPage;

// Issue number NODK-238
@RunWith(AndroidJUnit4.class)
public class UserAndDeviceIdentityTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule("metadata.xml"))
            .around(rule);

    @Test
    public void setEmail_validatesEmail() {
        //TestCase1
        new MainMenuPage()
                .openProjectSettings()
                .clickGeneralSettings()
                .clickUserAndDeviceIdentity()
                .clickFormMetadata()
                .clickEmail()
                .inputText("aabb")
                .clickOKOnDialog()
                .checkIsToastWithMessageDisplayed(R.string.invalid_email_address)
                .clickEmail()
                .inputText("aa@bb")
                .clickOKOnDialog()
                .assertText("aa@bb");
    }
}
