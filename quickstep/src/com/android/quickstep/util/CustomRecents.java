/*
 * Copyright (C) 2026 The Resurrected Remix Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.quickstep.util;

import android.content.Context;
import android.provider.Settings;

import com.android.quickstep.SystemUiProxy;

/**
 * The Recents choice in Launcher settings. While Slim Recents is picked, SystemUI shows
 * it where a gesture would open the overview.
 */
public final class CustomRecents {
    public static final String KEY = "rising_slim_recents_style";

    private CustomRecents() {}

    public static boolean isEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), KEY, 0) == 1
                && SystemUiProxy.INSTANCE.get(context).isActive();
    }

    public static void show(Context context) {
        SystemUiProxy.INSTANCE.get(context).showCustomRecents();
    }
}
