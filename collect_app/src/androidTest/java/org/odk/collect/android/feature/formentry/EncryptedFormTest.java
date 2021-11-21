/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.samarthya.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.support.CollectTestRule;
import org.samarthya.collect.android.support.CopyFormRule;
import org.samarthya.collect.android.support.TestDependencies;
import org.samarthya.collect.android.support.TestRuleChain;
import org.samarthya.collect.android.support.pages.MainMenuPage;
import org.samarthya.collect.android.support.pages.SendFinalizedFormPage;
import org.samarthya.collect.forms.instances.Instance;

public class EncryptedFormTest {

    TestDependencies testDependencies = new TestDependencies();

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(testDependencies)
            .around(new CopyFormRule("encrypted.xml"))
            .around(new CopyFormRule("encrypted-no-instanceID.xml"))
            .around(rule);

    @Test
    public void instanceOfEncryptedForm_cantBeEditedWhenFinalized() {
        rule.startAtMainMenu()
                .startBlankForm("encrypted")
                .assertQuestion("Question 1")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_ok)
                .clickEditSavedForm()
                .checkInstanceState("encrypted", Instance.STATUS_COMPLETE)
                .clickOnFormWithDialog("encrypted")
                .assertText(R.string.cannot_edit_completed_form);
    }

    @Test
    public void instanceOfEncryptedForm_cantBeViewedAfterSending() {
        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())

                .startBlankForm("encrypted")
                .assertQuestion("Question 1")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm("encrypted")
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage())
                .pressBack(new MainMenuPage())

                .clickViewSentForm(1)
                .clickOnText("encrypted")
                .assertText(R.string.encrypted_form)
                .assertOnPage();
    }

    //TestCase47
    @Test
    public void instanceOfEncryptedFormWithoutInstanceID_failsFinalizationWithMessage() {
        rule.startAtMainMenu()
                .startBlankForm("encrypted-no-instanceID")
                .assertQuestion("Question 1")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .checkIsToastWithMessageDisplayed("This form does not specify an instanceID. You must specify one to enable encryption. Form has not been saved as finalized.")
                .clickEditSavedForm()
                .checkInstanceState("encrypted-no-instanceID", Instance.STATUS_INCOMPLETE);
    }
}
