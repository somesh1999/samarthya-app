package org.samarthya.collect.android.support;

import androidx.fragment.app.FragmentActivity;

import org.samarthya.collect.android.audio.AudioControllerView;

public class SwipableParentActivity extends FragmentActivity implements AudioControllerView.SwipableParent {

    private boolean swipingAllowed;

    @Override
    public void allowSwiping(boolean allowSwiping) {
        swipingAllowed = allowSwiping;
    }

    public boolean isSwipingAllowed() {
        return swipingAllowed;
    }
}
