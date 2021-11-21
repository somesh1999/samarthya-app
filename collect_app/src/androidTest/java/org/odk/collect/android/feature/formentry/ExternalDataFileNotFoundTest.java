package org.samarthya.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.storage.StoragePathProvider;
import org.samarthya.collect.android.storage.StorageSubdirectory;
import org.samarthya.collect.android.support.CopyFormRule;
import org.samarthya.collect.android.support.FormActivityTestRule;
import org.samarthya.collect.android.support.AdbFormLoadingUtils;
import org.samarthya.collect.android.support.ResetStateRule;
import org.samarthya.collect.android.support.pages.FormEntryPage;

public class ExternalDataFileNotFoundTest {
    private static final String EXTERNAL_DATA_QUESTIONS = "external_data_questions.xml";

    @Rule
    public FormActivityTestRule activityTestRule = AdbFormLoadingUtils.getFormActivityTestRuleFor(EXTERNAL_DATA_QUESTIONS);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(EXTERNAL_DATA_QUESTIONS, true));

    @Test
    public void questionsThatUseExternalFiles_ShouldDisplayFriendlyMessageWhenFilesAreMissing() {
        String formsDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS);

        new FormEntryPage("externalDataQuestions")
                .assertText(R.string.file_missing, formsDirPath + "/external_data_questions-media/fruits.csv")
                .swipeToNextQuestion()
                .assertText(R.string.file_missing, formsDirPath + "/external_data_questions-media/itemsets.csv");
    }
}
