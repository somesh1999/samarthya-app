package org.samarthya.collect.android.database.itemsets

import org.samarthya.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.samarthya.collect.android.itemsets.FastExternalItemsetsRepository

class DatabaseFastExternalItemsetsRepository : FastExternalItemsetsRepository {

    override fun deleteAllByCsvPath(path: String) {
        ItemsetDbAdapter().open().use {
            it.delete(path)
        }
    }
}
