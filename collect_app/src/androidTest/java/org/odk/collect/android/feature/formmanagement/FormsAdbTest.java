package org.samarthya.collect.android.feature.formmanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.samarthya.collect.android.storage.StorageSubdirectory;
import org.samarthya.collect.android.support.CollectTestRule;
import org.samarthya.collect.android.support.AdbFormLoadingUtils;
import org.samarthya.collect.android.support.TestDependencies;
import org.samarthya.collect.android.support.TestRuleChain;
import org.samarthya.collect.android.support.pages.MainMenuPage;

import java.io.File;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FormsAdbTest {

    public final TestDependencies testDependencies = new TestDependencies();
    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(rule);

    @Test
    public void canUpdateFormOnDisk() throws Exception {
        MainMenuPage mainMenuPage = rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm()
                .assertFormExists("One Question")
                .pressBack(new MainMenuPage());

        AdbFormLoadingUtils.copyFormToStorage("one-question-updated.xml", "one-question.xml");

        mainMenuPage
                .clickFillBlankForm()
                .assertFormExists("One Question Updated")
                .assertFormDoesNotExist("One Question");
    }

    @Test
    public void canDeleteFormFromDisk() {
        MainMenuPage mainMenuPage = rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm()
                .assertFormExists("One Question")
                .pressBack(new MainMenuPage());

        String formsDir = testDependencies.storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS);
        boolean formDeleted = new File(formsDir, "one-question.xml").delete();
        assertTrue(formDeleted);

        mainMenuPage
                .clickFillBlankForm()
                .assertNoForms();
    }
}
