/*
 * Copyright (C) 2024 risingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.rising.settings.fragments

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import android.util.Log

import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference

import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.search.BaseSearchIndexProvider
import com.rising.settings.fragments.OptimizedSettingsFragment
import com.android.settingslib.search.SearchIndexable

/**
 * Settings fragment for Super Dynamic Island
 * Provides comprehensive control over all SDI features
 */
@SearchIndexable
class SuperDynamicIslandSettings : OptimizedSettingsFragment(), Preference.OnPreferenceChangeListener {

    companion object {
        private const val TAG = "SuperDynamicIslandSettings"
        
        // Master toggle
        private const val KEY_SDI_MASTER = "super_dynamic_island_enabled"
        
        // Feature toggles
        private const val KEY_SDI_CALLS = "sdi_call_enabled"
        private const val KEY_SDI_TIMER = "sdi_timer_enabled"
        private const val KEY_SDI_MEDIA = "sdi_media_enabled"
        private const val KEY_SDI_NOTIFICATIONS = "sdi_notification_enabled"
        private const val KEY_SDI_RECORDING = "sdi_recording_enabled"
        private const val KEY_SDI_FACEID = "sdi_faceid_enabled"
        private const val KEY_SDI_CHARGING = "sdi_charging_enabled"
        
        // Categories
        private const val KEY_CATEGORY_FEATURES = "sdi_features_category"
        private const val KEY_CATEGORY_ANIMATIONS = "sdi_animations_category"
        
        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER = object : BaseSearchIndexProvider(R.xml.super_dynamic_island_settings) {
            override fun getNonIndexableKeys(context: Context): List<String> {
                val keys = super.getNonIndexableKeys(context)
                val resources = context.resources
                
                // Add any conditional keys here based on device capabilities
                // For example, if Face ID is not supported:
                // if (!isFaceIDSupported(context)) {
                //     keys.add(KEY_SDI_FACEID)
                // }
                
                return keys
            }
        }
    }

    private var masterToggle: SwitchPreference? = null
    private var featuresCategory: PreferenceCategory? = null
    
    // Feature preferences
    private var callsPreference: SwitchPreference? = null
    private var timerPreference: SwitchPreference? = null
    private var mediaPreference: SwitchPreference? = null
    private var notificationsPreference: SwitchPreference? = null
    private var recordingPreference: SwitchPreference? = null
    private var faceIdPreference: SwitchPreference? = null
    private var chargingPreference: SwitchPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.super_dynamic_island_settings)

        val context = getSafeContext() ?: return
        val resolver = context.contentResolver
        val prefScreen = preferenceScreen
        val resources = context.resources

        initializePreferences(prefScreen, resolver, resources)
        updatePreferenceStates()
    }

    /**
     * Initialize all preference references and listeners
     */
    private fun initializePreferences(prefScreen: PreferenceScreen, resolver: ContentResolver, resources: Resources) {
        // Master toggle
        masterToggle = findPreference<SwitchPreference>(KEY_SDI_MASTER)?.apply {
            onPreferenceChangeListener = this@SuperDynamicIslandSettings
            isChecked = Settings.System.getInt(resolver, KEY_SDI_MASTER, 0) == 1
        }

        // Features category
        featuresCategory = findPreference(KEY_CATEGORY_FEATURES)

        // Feature preferences
        callsPreference = findPreference<SwitchPreference>(KEY_SDI_CALLS)?.apply {
            onPreferenceChangeListener = this@SuperDynamicIslandSettings
            isChecked = Settings.System.getInt(resolver, KEY_SDI_CALLS, 1) == 1
        }

        timerPreference = findPreference<SwitchPreference>(KEY_SDI_TIMER)?.apply {
            onPreferenceChangeListener = this@SuperDynamicIslandSettings
            isChecked = Settings.System.getInt(resolver, KEY_SDI_TIMER, 1) == 1
        }

        mediaPreference = findPreference<SwitchPreference>(KEY_SDI_MEDIA)?.apply {
            onPreferenceChangeListener = this@SuperDynamicIslandSettings
            isChecked = Settings.System.getInt(resolver, KEY_SDI_MEDIA, 1) == 1
        }

        notificationsPreference = findPreference<SwitchPreference>(KEY_SDI_NOTIFICATIONS)?.apply {
            onPreferenceChangeListener = this@SuperDynamicIslandSettings
            isChecked = Settings.System.getInt(resolver, KEY_SDI_NOTIFICATIONS, 1) == 1
        }

        recordingPreference = findPreference<SwitchPreference>(KEY_SDI_RECORDING)?.apply {
            onPreferenceChangeListener = this@SuperDynamicIslandSettings
            isChecked = Settings.System.getInt(resolver, KEY_SDI_RECORDING, 1) == 1
        }

        faceIdPreference = findPreference<SwitchPreference>(KEY_SDI_FACEID)?.apply {
            onPreferenceChangeListener = this@SuperDynamicIslandSettings
            isChecked = Settings.System.getInt(resolver, KEY_SDI_FACEID, 1) == 1
        }

        chargingPreference = findPreference<SwitchPreference>(KEY_SDI_CHARGING)?.apply {
            onPreferenceChangeListener = this@SuperDynamicIslandSettings
            isChecked = Settings.System.getInt(resolver, KEY_SDI_CHARGING, 1) == 1
        }

        Log.d(TAG, "Preferences initialized")
    }

    /**
     * Update preference states based on master toggle
     */
    private fun updatePreferenceStates() {
        val masterEnabled = masterToggle?.isChecked ?: false
        
        // Enable/disable feature preferences based on master toggle
        callsPreference?.isEnabled = masterEnabled
        timerPreference?.isEnabled = masterEnabled
        mediaPreference?.isEnabled = masterEnabled
        notificationsPreference?.isEnabled = masterEnabled
        recordingPreference?.isEnabled = masterEnabled
        faceIdPreference?.isEnabled = masterEnabled
        chargingPreference?.isEnabled = masterEnabled
        
        // Update category visibility
        featuresCategory?.isVisible = masterEnabled
        
        Log.d(TAG, "Preference states updated - master enabled: $masterEnabled")
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val context = getSafeContext() ?: return false
        val resolver = context.contentResolver
        
        try {
            when (preference.key) {
                KEY_SDI_MASTER -> {
                    val enabled = newValue as Boolean
                    Settings.System.putInt(resolver, KEY_SDI_MASTER, if (enabled) 1 else 0)
                    
                    // Update dependent preferences
                    updatePreferenceStates()
                    
                    Log.i(TAG, "Master toggle changed: $enabled")
                    
                    // If disabling, also disable all features for safety
                    if (!enabled) {
                        disableAllFeatures(resolver)
                    }
                }
                
                KEY_SDI_CALLS -> {
                    val enabled = newValue as Boolean
                    Settings.System.putInt(resolver, KEY_SDI_CALLS, if (enabled) 1 else 0)
                    Log.d(TAG, "Calls feature changed: $enabled")
                }
                
                KEY_SDI_TIMER -> {
                    val enabled = newValue as Boolean
                    Settings.System.putInt(resolver, KEY_SDI_TIMER, if (enabled) 1 else 0)
                    Log.d(TAG, "Timer feature changed: $enabled")
                }
                
                KEY_SDI_MEDIA -> {
                    val enabled = newValue as Boolean
                    Settings.System.putInt(resolver, KEY_SDI_MEDIA, if (enabled) 1 else 0)
                    Log.d(TAG, "Media feature changed: $enabled")
                }
                
                KEY_SDI_NOTIFICATIONS -> {
                    val enabled = newValue as Boolean
                    Settings.System.putInt(resolver, KEY_SDI_NOTIFICATIONS, if (enabled) 1 else 0)
                    Log.d(TAG, "Notifications feature changed: $enabled")
                }
                
                KEY_SDI_RECORDING -> {
                    val enabled = newValue as Boolean
                    Settings.System.putInt(resolver, KEY_SDI_RECORDING, if (enabled) 1 else 0)
                    Log.d(TAG, "Recording feature changed: $enabled")
                }
                
                KEY_SDI_FACEID -> {
                    val enabled = newValue as Boolean
                    Settings.System.putInt(resolver, KEY_SDI_FACEID, if (enabled) 1 else 0)
                    Log.d(TAG, "Face ID feature changed: $enabled")
                }
                
                KEY_SDI_CHARGING -> {
                    val enabled = newValue as Boolean
                    Settings.System.putInt(resolver, KEY_SDI_CHARGING, if (enabled) 1 else 0)
                    Log.d(TAG, "Charging feature changed: $enabled")
                }
                
                else -> {
                    Log.w(TAG, "Unknown preference key: ${preference.key}")
                    return false
                }
            }
            
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling preference change for ${preference.key}", e)
            return false
        }
    }

    /**
     * Disable all features (safety measure)
     */
    private fun disableAllFeatures(resolver: ContentResolver) {
        try {
            Settings.System.putInt(resolver, KEY_SDI_CALLS, 0)
            Settings.System.putInt(resolver, KEY_SDI_TIMER, 0)
            Settings.System.putInt(resolver, KEY_SDI_MEDIA, 0)
            Settings.System.putInt(resolver, KEY_SDI_NOTIFICATIONS, 0)
            Settings.System.putInt(resolver, KEY_SDI_RECORDING, 0)
            Settings.System.putInt(resolver, KEY_SDI_FACEID, 0)
            Settings.System.putInt(resolver, KEY_SDI_CHARGING, 0)
            
            // Update UI
            callsPreference?.isChecked = false
            timerPreference?.isChecked = false
            mediaPreference?.isChecked = false
            notificationsPreference?.isChecked = false
            recordingPreference?.isChecked = false
            faceIdPreference?.isChecked = false
            chargingPreference?.isChecked = false
            
            Log.i(TAG, "All features disabled")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling all features", e)
        }
    }

    /**
     * Check if device supports Face ID
     */
    private fun isFaceIDSupported(context: Context): Boolean {
        // This is a placeholder - implement actual Face ID capability check
        return context.packageManager.hasSystemFeature("android.hardware.biometrics.face")
    }

    /**
     * Perform compatibility check before enabling features
     */
    private fun performCompatibilityCheck(): Boolean {
        val context = getSafeContext() ?: return false
        
        try {
            // Basic compatibility checks
            // In a real implementation, you'd check for:
            // - risingOS version compatibility
            // - SystemUI integration availability
            // - Required permissions
            
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Compatibility check failed", e)
            return false
        }
    }

    override fun getMetricsCategory(): Int {
        return MetricsEvent.VIEW_UNKNOWN
    }

    override fun onResume() {
        super.onResume()
        
        // Refresh preference states when returning to fragment
        val context = getSafeContext() ?: return
        val resolver = context.contentResolver
        
        try {
            // Update master toggle
            masterToggle?.isChecked = Settings.System.getInt(resolver, KEY_SDI_MASTER, 0) == 1
            
            // Update feature toggles
            callsPreference?.isChecked = Settings.System.getInt(resolver, KEY_SDI_CALLS, 1) == 1
            timerPreference?.isChecked = Settings.System.getInt(resolver, KEY_SDI_TIMER, 1) == 1
            mediaPreference?.isChecked = Settings.System.getInt(resolver, KEY_SDI_MEDIA, 1) == 1
            notificationsPreference?.isChecked = Settings.System.getInt(resolver, KEY_SDI_NOTIFICATIONS, 1) == 1
            recordingPreference?.isChecked = Settings.System.getInt(resolver, KEY_SDI_RECORDING, 1) == 1
            faceIdPreference?.isChecked = Settings.System.getInt(resolver, KEY_SDI_FACEID, 1) == 1
            chargingPreference?.isChecked = Settings.System.getInt(resolver, KEY_SDI_CHARGING, 1) == 1
            
            updatePreferenceStates()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing preferences", e)
        }
    }

    /**
     * Get current configuration status for debugging
     */
    fun getConfigurationStatus(): String {
        val context = getSafeContext() ?: return "Context unavailable"
        val resolver = context.contentResolver
        
        return buildString {
            appendLine("Super Dynamic Island Configuration:")
            appendLine("- Master: ${Settings.System.getInt(resolver, KEY_SDI_MASTER, 0) == 1}")
            appendLine("- Calls: ${Settings.System.getInt(resolver, KEY_SDI_CALLS, 1) == 1}")
            appendLine("- Timer: ${Settings.System.getInt(resolver, KEY_SDI_TIMER, 1) == 1}")
            appendLine("- Media: ${Settings.System.getInt(resolver, KEY_SDI_MEDIA, 1) == 1}")
            appendLine("- Notifications: ${Settings.System.getInt(resolver, KEY_SDI_NOTIFICATIONS, 1) == 1}")
            appendLine("- Recording: ${Settings.System.getInt(resolver, KEY_SDI_RECORDING, 1) == 1}")
            appendLine("- Face ID: ${Settings.System.getInt(resolver, KEY_SDI_FACEID, 1) == 1}")
            appendLine("- Charging: ${Settings.System.getInt(resolver, KEY_SDI_CHARGING, 1) == 1}")
        }
    }
}
