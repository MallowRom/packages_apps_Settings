/*
* Copyright (C) 2015 MallowRom
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

package com.android.settings.mallow.statusbar;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.mallow.MallowUtils;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.android.settings.mallow.preference.SeekBarPreferenceCHOS;

import java.util.Locale;

public class NotificationDrawer extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "NotificationDrawer";

    private static final String PREF_QUICK_PULLDOWN = "quick_pulldown";
    private static final String PREF_SMART_PULLDOWN = "smart_pulldown";
    private static final String FLASHLIGHT_NOTIFICATION = "flashlight_notification";
    private static final String PREF_QS_TRANSPARENT_SHADE = "qs_transparent_shade";
    private static final String PREF_QS_TRANSPARENT_HEADER = "qs_transparent_header";
    private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
    private static final String PREF_QS_STROKE = "qs_stroke";
    private static final String PREF_QS_STROKE_COLOR = "qs_stroke_color";
    private static final String PREF_QS_STROKE_THICKNESS = "qs_stroke_thickness";
    private static final String PREF_QS_CORNER_RADIUS = "qs_corner_radius";

    private ListPreference mQuickPulldown;
    private ListPreference mSmartPulldown;
    private SwitchPreference mFlashlightNotification;
    private SeekBarPreferenceCHOS mQSShadeAlpha;
    private SeekBarPreferenceCHOS mQSHeaderAlpha;
    private ListPreference mNumColumns;
    private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;
    private ListPreference mTileAnimationInterpolator;
    private ListPreference mQSStroke;
    private ColorPickerPreference mQSStrokeColor;
    private SeekBarPreferenceCHOS mQSStrokeThickness;
    private SeekBarPreferenceCHOS mQSCornerRadius;

    static final int DEFAULT_QS_STROKE_COLOR = 0xFF59007F;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notification_drawer);

        PreferenceScreen prefSet = getPreferenceScreen();

        // Quick pulldown
        mQuickPulldown = (ListPreference) findPreference(PREF_QUICK_PULLDOWN);
        if (!Utils.isPhone(getActivity())) {
            prefSet.removePreference(mQuickPulldown);
        } else {
            mQuickPulldown.setOnPreferenceChangeListener(this);
            int statusQuickPulldown = Settings.System.getInt(getContentResolver(),
                    Settings.System.QS_QUICK_PULLDOWN, 0);
            mQuickPulldown.setValue(String.valueOf(statusQuickPulldown));
            updateQuickPulldownSummary(statusQuickPulldown);
        }

        // Smart pulldown
		mSmartPulldown = (ListPreference) findPreference(PREF_SMART_PULLDOWN);
		mSmartPulldown.setOnPreferenceChangeListener(this);
        int smartPulldown = Settings.System.getInt(getContentResolver(),
              Settings.System.QS_SMART_PULLDOWN, 0);
        mSmartPulldown.setValue(String.valueOf(smartPulldown));
        updateSmartPulldownSummary(smartPulldown);

        // QS shade alpha
        mQSShadeAlpha =
             (SeekBarPreferenceCHOS) prefSet.findPreference(PREF_QS_TRANSPARENT_SHADE);
        int qSShadeAlpha = Settings.System.getInt(getContentResolver(),
             Settings.System.QS_TRANSPARENT_SHADE, 255);
        mQSShadeAlpha.setValue(qSShadeAlpha / 1);
        mQSShadeAlpha.setOnPreferenceChangeListener(this);

        // QS header alpha
        mQSHeaderAlpha =
             (SeekBarPreferenceCHOS) prefSet.findPreference(PREF_QS_TRANSPARENT_HEADER);
        int qSHeaderAlpha = Settings.System.getInt(getContentResolver(),
             Settings.System.QS_TRANSPARENT_HEADER, 255);
        mQSHeaderAlpha.setValue(qSHeaderAlpha / 1);
        mQSHeaderAlpha.setOnPreferenceChangeListener(this);

        // QS tile column number
        mNumColumns = (ListPreference) findPreference("sysui_qs_num_columns");
        int numColumns = Settings.Secure.getIntForUser(getContentResolver(),
             Settings.Secure.QS_NUM_TILE_COLUMNS, getDefaultNumColums(),
             UserHandle.USER_CURRENT);
        mNumColumns.setValue(String.valueOf(numColumns));
        updateNumColumnsSummary(numColumns);
        mNumColumns.setOnPreferenceChangeListener(this);

        // Flashlight notification
        mFlashlightNotification = (SwitchPreference) findPreference(FLASHLIGHT_NOTIFICATION);
        mFlashlightNotification.setOnPreferenceChangeListener(this);

        if (!MallowUtils.deviceSupportsFlashLight(getActivity())) {
            prefSet.removePreference(mFlashlightNotification);
        } else {
            mFlashlightNotification.setChecked((Settings.System.getInt
                (getContentResolver(), Settings.System.FLASHLIGHT_NOTIFICATION, 0) == 1));
        }

        // QS animation style
        mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
        int tileAnimationStyle = Settings.System.getIntForUser(
                getContentResolver(), Settings.System.ANIM_TILE_STYLE, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
        updateTileAnimationStyleSummary(tileAnimationStyle);
        updateAnimTileStyle(tileAnimationStyle);
        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        // QS animation duration
        mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
        int tileAnimationDuration = Settings.System.getIntForUser(
                getContentResolver(), Settings.System.ANIM_TILE_DURATION, 2000,
                UserHandle.USER_CURRENT);
        mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
        updateTileAnimationDurationSummary(tileAnimationDuration);
        mTileAnimationDuration.setOnPreferenceChangeListener(this);

        // QS animation interpolator
        mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
        int tileAnimationInterpolator = Settings.System.getIntForUser(
                getContentResolver(), Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
        updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
        mTileAnimationInterpolator.setOnPreferenceChangeListener(this);

        // QS stroke
        mQSStroke = (ListPreference) findPreference(PREF_QS_STROKE);
        int qSStroke = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_STROKE, 1, UserHandle.USER_CURRENT);
        mQSStroke.setValue(String.valueOf(qSStroke));
        mQSStroke.setSummary(mQSStroke.getEntry());
        mQSStroke.setOnPreferenceChangeListener(this);

        // QS stroke color
        mQSStrokeColor = 
                (ColorPickerPreference) findPreference(PREF_QS_STROKE_COLOR);
        mQSStrokeColor.setOnPreferenceChangeListener(this);
        int qSIntColor = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_STROKE_COLOR, DEFAULT_QS_STROKE_COLOR);
        String qSHexColor = String.format("#%08x", (0xFF80CBC4 & qSIntColor));
        mQSStrokeColor.setSummary(qSHexColor);
        mQSStrokeColor.setNewPreviewColor(qSIntColor);

        // QS stroke thickness
        mQSStrokeThickness =
                (SeekBarPreferenceCHOS) findPreference(PREF_QS_STROKE_THICKNESS);
        int qSStrokeThickness = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_STROKE_THICKNESS, 4);
        mQSStrokeThickness.setValue(qSStrokeThickness / 1);
        mQSStrokeThickness.setOnPreferenceChangeListener(this);

        // QS corner radius
        mQSCornerRadius =
                (SeekBarPreferenceCHOS) findPreference(PREF_QS_CORNER_RADIUS);
        int qSCornerRadius = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_CORNER_RADIUS, 5);
        mQSCornerRadius.setValue(qSCornerRadius / 1);
        mQSCornerRadius.setOnPreferenceChangeListener(this);

        QSSettingsDisabler(qSStroke);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DONT_TRACK_ME_BRO;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mQuickPulldown) {
            int statusQuickPulldown = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                Settings.System.QS_QUICK_PULLDOWN,
                    statusQuickPulldown);
            updateQuickPulldownSummary(statusQuickPulldown);
            return true;
        } else if (preference == mSmartPulldown) {
            int smartPulldown = Integer.valueOf((String) objValue);
                Settings.System.putInt(getContentResolver(),
                    Settings.System.QS_SMART_PULLDOWN, smartPulldown);
            updateSmartPulldownSummary(smartPulldown);
            return true;
        } else if  (preference == mFlashlightNotification) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                Settings.System.FLASHLIGHT_NOTIFICATION, checked ? 1:0);
            return true;		
        } else if (preference == mQSShadeAlpha) {
            int alpha = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                Settings.System.QS_TRANSPARENT_SHADE, alpha * 1);
            return true;
		} else if (preference == mQSHeaderAlpha) {
            int alpha = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                Settings.System.QS_TRANSPARENT_HEADER, alpha * 1);
            return true;			
        } else if (preference == mNumColumns) {
            int numColumns = Integer.valueOf((String) objValue);
            Settings.Secure.putIntForUser(getContentResolver(), 
                Settings.Secure.QS_NUM_TILE_COLUMNS,
                    numColumns, UserHandle.USER_CURRENT);
            updateNumColumnsSummary(numColumns);
            return true;
        } else if (preference == mTileAnimationStyle) {
            int tileAnimationStyle = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_STYLE,
                    tileAnimationStyle, UserHandle.USER_CURRENT);
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            return true;
        } else if (preference == mTileAnimationDuration) {
            int tileAnimationDuration = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_DURATION,
                    tileAnimationDuration, UserHandle.USER_CURRENT);
            updateTileAnimationDurationSummary(tileAnimationDuration);
            return true;
        } else if (preference == mTileAnimationInterpolator) {
            int tileAnimationInterpolator = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_INTERPOLATOR,
                    tileAnimationInterpolator, UserHandle.USER_CURRENT);
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            return true;
	    } else if (preference == mQSStroke) {
            int qSStroke = Integer.parseInt((String) objValue);
            int index = mQSStroke.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.
                QS_STROKE, qSStroke, UserHandle.USER_CURRENT);
            mQSStroke.setSummary(mQSStroke.getEntries()[index]);
            QSSettingsDisabler(qSStroke);
            return true;
        } else if (preference == mQSStrokeColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                Settings.System.QS_STROKE_COLOR, intHex);
            return true;
        } else if (preference == mQSStrokeThickness) {
            int val = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                Settings.System.QS_STROKE_THICKNESS, val * 1);
            return true;
        } else if (preference == mQSCornerRadius) {
            int val = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                Settings.System.QS_CORNER_RADIUS, val * 1);
            return true;
        }
        return false;
    }

    private void QSSettingsDisabler(int qSStroke) {
        if (qSStroke == 0) {
            mQSStrokeColor.setEnabled(false);
            mQSStrokeThickness.setEnabled(false);
        } else if (qSStroke == 1) {
            mQSStrokeColor.setEnabled(false);
            mQSStrokeThickness.setEnabled(true);
        } else {
            mQSStrokeColor.setEnabled(true);
            mQSStrokeThickness.setEnabled(true);
        }
    }

    private void updateQuickPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // quick pulldown deactivated
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else {
            Locale l = Locale.getDefault();
            boolean isRtl = TextUtils.getLayoutDirectionFromLocale(l) == View.LAYOUT_DIRECTION_RTL;
            String direction = res.getString(value == 2
                    ? (isRtl ? R.string.quick_pulldown_right : R.string.quick_pulldown_left)
                    : (isRtl ? R.string.quick_pulldown_left : R.string.quick_pulldown_right));
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary, direction));
        }
    }

    private void updateNumColumnsSummary(int numColumns) {
        String prefix = (String) mNumColumns.getEntries()[mNumColumns.findIndexOfValue(String
                .valueOf(numColumns))];
        mNumColumns.setSummary(getActivity().getResources().getString(R.string.qs_num_columns_showing, prefix));
    }

    private int getDefaultNumColums() {
        try {
            Resources res = getActivity().getPackageManager()
                    .getResourcesForApplication("com.android.systemui");
            int val = res.getInteger(res.getIdentifier("quick_settings_num_columns", "integer",
                    "com.android.systemui"));
            return Math.max(1, val);
        } catch (Exception e) {
            return 3;
        }
    }

    private void updateSmartPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Smart pulldown deactivated
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off));
        } else {
            String type = null;
            switch (value) {
                case 1:
                    type = res.getString(R.string.smart_pulldown_dismissable);
                    break;
                case 2:
                    type = res.getString(R.string.smart_pulldown_persistent);
                    break;
                default:
                    type = res.getString(R.string.smart_pulldown_all);
                    break;
            }
            // Remove title capitalized formatting
            type = type.toLowerCase();
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
        }
    }

    private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
        String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                .valueOf(tileAnimationStyle))];
        mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
    }

    private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
        String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                .valueOf(tileAnimationDuration))];
        mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
    }

    private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
        String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                .valueOf(tileAnimationInterpolator))];
        mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
    }

    private void updateAnimTileStyle(int tileAnimationStyle) {
        if (mTileAnimationDuration != null) {
            if (tileAnimationStyle == 0) {
                mTileAnimationDuration.setSelectable(false);
                mTileAnimationInterpolator.setSelectable(false);
            } else {
                mTileAnimationDuration.setSelectable(true);
                mTileAnimationInterpolator.setSelectable(true);
            }
        }
    }
}
