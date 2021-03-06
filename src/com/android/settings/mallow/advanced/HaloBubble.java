/*
 * Copyright (C) 2012 ParanoidAndroid Project
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

package com.android.settings.mallow.advanced;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class HaloBubble extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_HALO_SIZE = "halo_size";
    private static final String KEY_HALO_MSGBOX_ANIMATION = "halo_msgbox_animation";
    private static final String KEY_HALO_NOTIFY_COUNT = "halo_notify_count";
    private static final String KEY_HALO_COLOR = "halo_color";
    private static final String KEY_HALO_FLOAT_NOTIFICATIONS = "halo_float_notifications";

    private ListPreference mHaloSize;
    private ListPreference mHaloNotifyCount;
    private ListPreference mHaloMsgAnimate;
    private ColorPickerPreference mHaloColor;
    private SwitchPreference mHaloFloat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.halo_bubble);

        mHaloSize = (ListPreference) findPreference(KEY_HALO_SIZE);
        try {
            float haloSize = Settings.Secure.getFloat(getContentResolver(),
                    Settings.Secure.HALO_SIZE, 1.0f);
            mHaloSize.setValue(String.valueOf(haloSize));
        } catch(Exception ex) {
            // So what
        }
        mHaloSize.setOnPreferenceChangeListener(this);

        mHaloNotifyCount = (ListPreference) findPreference(KEY_HALO_NOTIFY_COUNT);
        try {
            int haloCounter = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.HALO_NOTIFY_COUNT, 4);
            mHaloNotifyCount.setValue(String.valueOf(haloCounter));
        } catch(Exception ex) {
            // fail...
        }
        mHaloNotifyCount.setOnPreferenceChangeListener(this);

        mHaloMsgAnimate = (ListPreference) findPreference(KEY_HALO_MSGBOX_ANIMATION);
        try {
            int haloMsgAnimation = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.HALO_MSGBOX_ANIMATION, 2);
            mHaloMsgAnimate.setValue(String.valueOf(haloMsgAnimation));
        } catch(Exception ex) {
            // fail...
        }
        mHaloMsgAnimate.setOnPreferenceChangeListener(this);

        mHaloColor = (ColorPickerPreference) findPreference(KEY_HALO_COLOR);
        mHaloColor.setOnPreferenceChangeListener(this);

        mHaloFloat = (SwitchPreference) findPreference(KEY_HALO_FLOAT_NOTIFICATIONS);
        if (Settings.System.getInt(getContentResolver(),
                  Settings.System.FLOATING_WINDOW_MODE, 0) == 0) {
            mHaloFloat.setEnabled(false);
            mHaloFloat.setSummary(R.string.halo_enable_float_summary);
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DONT_TRACK_ME_BRO;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHaloSize) {
            float haloSize = Float.valueOf((String) newValue);
            Settings.Secure.putFloat(getContentResolver(),
                    Settings.Secure.HALO_SIZE, haloSize);
            return true;
        } else if (preference == mHaloMsgAnimate) {
            int haloMsgAnimation = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.HALO_MSGBOX_ANIMATION, haloMsgAnimation);
            return true;
        } else if (preference == mHaloNotifyCount) {
            int haloNotifyCount = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.HALO_NOTIFY_COUNT, haloNotifyCount);
            return true;
        } else if (preference == mHaloColor) {
            int haloColor = Integer.valueOf(String.valueOf(newValue));
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.HALO_COLOR, haloColor);
            return true;
        }
        return false;
    }
}
