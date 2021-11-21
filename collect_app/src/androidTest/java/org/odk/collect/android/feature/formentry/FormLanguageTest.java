package org.samarthya.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.support.CollectTestRule;
import org.samarthya.collect.android.support.TestRuleChain;
import org.samarthya.collect.android.support.pages.MainMenuPage;
import org.samarthya.collect.android.support.pages.SaveOrIgnoreDialog;

@RunWith(AndroidJUnit4.class)
public class FormLanguageTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain().around(rule);

    @Test
    public void canSwitchLanguagesInForm() {
        rule.startAtMainMenu()
                .copyForm("one-question-translation.xml")
                .startBlankForm("One Question")
                .assertQuestion("what is your age")
                .clickOptionsIcon()
                .clickOnString(R.string.change_language)
                .clickOnText("French (fr)")
                .assertQuestion("quel âge as-tu");
    }

    @Test
    public void languageChoiceIsPersisted() {
        rule.startAtMainMenu()
                .copyForm("one-question-translation.xml")
                .startBlankForm("One Question")
                .clickOptionsIcon()
                .clickOnString(R.string.change_language)
                .clickOnText("French (fr)")
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage()))
                .clickIgnoreChanges()

                .startBlankForm("One Question")
                .assertQuestion("quel âge as-tu");
    }
}
