package org.samarthya.collect.android.adapters;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.samarthya.collect.android.fragments.BlankFormListFragment;
import org.samarthya.collect.android.fragments.SavedFormListFragment;

public class DeleteFormsTabsAdapter extends FragmentStateAdapter {

    private final boolean matchExactlyEnabled;

    String formFormat = null;

    public DeleteFormsTabsAdapter(FragmentActivity fa, boolean matchExactlyEnabled, String formFormat) {
        super(fa);
        this.matchExactlyEnabled = matchExactlyEnabled;
        this.formFormat = formFormat;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            /*case 0:
                return new SavedFormListFragment();
            case 1:
                return new BlankFormListFragment();
            default:
                // should never reach here
                throw new IllegalArgumentException("Fragment position out of bounds");*/
            case 0:
                //return new BlankFormListFragment(formFormat);
                return new SavedFormListFragment(formFormat);
            default:
                // should never reach here
                throw new IllegalArgumentException("Fragment position out of bounds");
        }
    }

    @Override
    public int getItemCount() {
        if (matchExactlyEnabled) {
            return 1;
        } else {
            return 2;
        }
    }
}