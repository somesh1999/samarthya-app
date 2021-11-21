package org.samarthya.collect.android.formentry.media;

import android.content.Context;

import org.samarthya.collect.android.audio.AudioHelper;

public interface AudioHelperFactory {

    AudioHelper create(Context context);
}
