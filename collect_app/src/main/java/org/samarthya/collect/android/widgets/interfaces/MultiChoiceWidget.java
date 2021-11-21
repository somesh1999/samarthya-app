package org.samarthya.collect.android.widgets.interfaces;

import org.samarthya.collect.android.widgets.interfaces.Widget;

/**
 * @author James Knight
 */
public interface MultiChoiceWidget extends Widget {
    int getChoiceCount();

    void setChoiceSelected(int choiceIndex, boolean isSelected);
}
