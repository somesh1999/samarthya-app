/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.samarthya.collect.android.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import org.samarthya.collect.android.application.Collect;

public class ScreenUtils {

    private final Context context;

    public ScreenUtils(Context context) {
        this.context = context;
    }

    public static int getScreenWidth() {
        return getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return getDisplayMetrics().heightPixels;
    }

    public static float xdpi() {
        return getDisplayMetrics().xdpi;
    }

    public static float ydpi() {
        return getDisplayMetrics().ydpi;
    }

    private static DisplayMetrics getDisplayMetrics() {
        return Collect.getInstance().getResources().getDisplayMetrics();
    }

    public int getScreenSizeConfiguration() {
        return context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
    }
}
