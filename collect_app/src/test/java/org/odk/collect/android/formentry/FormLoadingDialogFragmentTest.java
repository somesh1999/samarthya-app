package org.samarthya.collect.android.formentry;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.samarthya.collect.android.R;
import org.samarthya.collect.testshared.RobolectricHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class FormLoadingDialogFragmentTest {

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<FormLoadingDialogFragment> fragmentScenario = RobolectricHelpers.launchDialogFragment(FormLoadingDialogFragment.class, R.style.Theme_Collect_Light);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }
}
