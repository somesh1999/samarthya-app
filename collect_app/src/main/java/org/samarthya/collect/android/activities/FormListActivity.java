package org.samarthya.collect.android.activities;

import org.samarthya.collect.android.activities.AppListActivity;
import org.samarthya.collect.android.database.forms.DatabaseFormColumns;

import static org.samarthya.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.samarthya.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.samarthya.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.samarthya.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;

public abstract class FormListActivity extends org.samarthya.collect.android.activities.AppListActivity {

    protected static final String SORT_BY_NAME_ASC
            = DatabaseFormColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
    protected static final String SORT_BY_NAME_DESC
            = DatabaseFormColumns.DISPLAY_NAME + " COLLATE NOCASE DESC";
    protected static final String SORT_BY_DATE_ASC = DatabaseFormColumns.DATE + " ASC";
    protected static final String SORT_BY_DATE_DESC = DatabaseFormColumns.DATE + " DESC";

    protected String getSortingOrder() {
        String sortingOrder = SORT_BY_NAME_ASC;
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortingOrder = SORT_BY_NAME_ASC;
                break;
            case BY_NAME_DESC:
                sortingOrder = SORT_BY_NAME_DESC;
                break;
            case BY_DATE_ASC:
                sortingOrder = SORT_BY_DATE_ASC;
                break;
            case BY_DATE_DESC:
                sortingOrder = SORT_BY_DATE_DESC;
                break;
        }
        return sortingOrder;
    }
}
