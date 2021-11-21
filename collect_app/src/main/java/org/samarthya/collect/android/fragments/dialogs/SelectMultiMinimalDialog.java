package org.samarthya.collect.android.fragments.dialogs;

import android.content.Context;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.samarthya.collect.android.adapters.SelectMultipleListAdapter;
import org.samarthya.collect.android.fragments.dialogs.SelectMinimalDialog;

import java.util.List;

public class SelectMultiMinimalDialog extends SelectMinimalDialog {
    public SelectMultiMinimalDialog() {
    }

    public SelectMultiMinimalDialog(List<Selection> selectedItems, boolean isFlex, boolean isAutoComplete, Context context,
                                    List<SelectChoice> items, FormEntryPrompt prompt, ReferenceManager referenceManager,
                                    int playColor, int numColumns, boolean noButtonsMode) {
        super(isFlex, isAutoComplete);
        adapter = new SelectMultipleListAdapter(selectedItems, null, context, items, prompt,
                referenceManager, null, playColor, numColumns, noButtonsMode);
    }
}
