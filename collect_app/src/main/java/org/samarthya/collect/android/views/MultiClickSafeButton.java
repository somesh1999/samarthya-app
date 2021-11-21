package org.samarthya.collect.android.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import org.samarthya.collect.android.utilities.MultiClickGuard;

public class MultiClickSafeButton extends MaterialButton {

    public MultiClickSafeButton(@NonNull Context context) {
        super(context);
    }

    public MultiClickSafeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        return MultiClickGuard.allowClick(getClass().getName()) && super.performClick();
    }
}
