package org.samarthya.collect.android.openrosa;

import org.jetbrains.annotations.Nullable;
import org.kxml2.kdom.Document;
import org.samarthya.collect.forms.FormListItem;
import org.samarthya.collect.forms.MediaFile;

import java.util.List;

public interface OpenRosaResponseParser {

    @Nullable List<FormListItem> parseFormList(Document document);
    @Nullable List<MediaFile> parseManifest(Document document);
}
