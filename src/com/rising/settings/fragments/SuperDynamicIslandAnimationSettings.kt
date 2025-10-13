/*
 * Copyright (C) 2024 risingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.rising.settings.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.android.settings.preferences.ui.AdaptivePreference
import com.android.settings.R

/**
 * Advanced animation settings for Super Dynamic Island
 * Includes live preview and fine-tuned animation controls
 */
class SuperDynamicIslandAnimationSettings : PreferenceFragmentCompat() {

    companion object {
        private const val TAG = "SDI_AnimationSettings"
        
        // Preference keys
        private const val KEY_ANIMATION_SPEED = "sdi_animation_speed"
        private const val KEY_ANIMATION_STYLE = "sdi_animation_style"
        private const val KEY_HAPTIC_FEEDBACK = "sdi_haptic_feedback"
        private const val KEY_ANIMATION_PREVIEW = "sdi_animation_preview"
        private const val KEY_CUSTOM_DURATION = "sdi_custom_duration"
        private const val KEY_BOUNCE_INTENSITY = "sdi_bounce_intensity"
        private const val KEY_FADE_TRANSITIONS = "sdi_fade_transitions"
        private const val KEY_SCALE_ANIMATIONS = "sdi_scale_animations"
        private const val KEY_ROTATION_EFFECTS = "sdi_rotation_effects"
        private const val KEY_PARALLAX_EFFECTS = "sdi_parallax_effects"
        
        // Settings keys
        private const val SETTING_ANIMATION_SPEED = "super_dynamic_island_animation_speed"
        private const val SETTING_ANIMATION_STYLE = "super_dynamic_island_animation_style"
        private const val SETTING_HAPTIC_FEEDBACK = "super_dynamic_island_haptic_feedback"
        private const val SETTING_CUSTOM_DURATION = "super_dynamic_island_custom_duration"
        private const val SETTING_BOUNCE_INTENSITY = "super_dynamic_island_bounce_intensity"
        private const val SETTING_FADE_TRANSITIONS = "super_dynamic_island_fade_transitions"
        private const val SETTING_SCALE_ANIMATIONS = "super_dynamic_island_scale_animations"
        private const val SETTING_ROTATION_EFFECTS = "super_dynamic_island_rotation_effects"
        private const val SETTING_PARALLAX_EFFECTS = "super_dynamic_island_parallax_effects"
        
        // Default values
        private const val DEFAULT_ANIMATION_SPEED = 1 // Normal
        private const val DEFAULT_ANIMATION_STYLE = 0 // Smooth
        private const val DEFAULT_CUSTOM_DURATION = 400 // milliseconds
        private const val DEFAULT_BOUNCE_INTENSITY = 50 // 50%
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var previewView: View? = null
    private var currentPreviewAnimation: Animator? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sdi_animation_settings, rootKey)
        
        setupAnimationSpeedPreference()
        setupAnimationStylePreference()
        setupHapticFeedbackPreference()
        setupAnimationPreview()
        setupAdvancedSettings()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
            ?: throw IllegalStateException("Failed to create view")
        
        // Create preview container
        createPreviewContainer(view)
        
        return view
    }

    /**
     * Setup animation speed preference
     */
    private fun setupAnimationSpeedPreference() {
        val speedPreference = findPreference<ListPreference>(KEY_ANIMATION_SPEED)
        speedPreference?.apply {
            entries = resources.getStringArray(R.array.sdi_animation_speed_entries)
            entryValues = resources.getStringArray(R.array.sdi_animation_speed_values)
            
            value = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_ANIMATION_SPEED,
                DEFAULT_ANIMATION_SPEED
            ).toString()
            
            summary = entry
            
            setOnPreferenceChangeListener { _, newValue ->
                val speed = newValue.toString().toInt()
                Settings.System.putInt(
                    requireContext().contentResolver,
                    SETTING_ANIMATION_SPEED,
                    speed
                )
                
                summary = entries[speed]
                triggerPreviewAnimation("speed_change")
                true
            }
        }
    }

    /**
     * Setup animation style preference
     */
    private fun setupAnimationStylePreference() {
        val stylePreference = findPreference<ListPreference>(KEY_ANIMATION_STYLE)
        stylePreference?.apply {
            entries = resources.getStringArray(R.array.sdi_animation_style_entries)
            entryValues = resources.getStringArray(R.array.sdi_animation_style_values)
            
            value = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_ANIMATION_STYLE,
                DEFAULT_ANIMATION_STYLE
            ).toString()
            
            summary = entry
            
            setOnPreferenceChangeListener { _, newValue ->
                val style = newValue.toString().toInt()
                Settings.System.putInt(
                    requireContext().contentResolver,
                    SETTING_ANIMATION_STYLE,
                    style
                )
                
                summary = entries[style]
                triggerPreviewAnimation("style_change")
                true
            }
        }
    }

    /**
     * Setup haptic feedback preference
     */
    private fun setupHapticFeedbackPreference() {
        val hapticPreference = findPreference<SwitchPreference>(KEY_HAPTIC_FEEDBACK)
        hapticPreference?.apply {
            isChecked = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_HAPTIC_FEEDBACK,
                1
            ) == 1
            
            setOnPreferenceChangeListener { _, newValue ->
                Settings.System.putInt(
                    requireContext().contentResolver,
                    SETTING_HAPTIC_FEEDBACK,
                    if (newValue as Boolean) 1 else 0
                )
                
                if (newValue) {
                    performHapticFeedback()
                }
                true
            }
        }
    }

    /**
     * Setup animation preview
     */
    private fun setupAnimationPreview() {
        val previewPreference = findPreference<AdaptivePreference>(KEY_ANIMATION_PREVIEW)
        previewPreference?.apply {
            summary = "Tap to preview current animation settings"
            
            setOnPreferenceClickListener {
                triggerPreviewAnimation("manual_preview")
                true
            }
        }
    }

    /**
     * Setup advanced animation settings
     */
    private fun setupAdvancedSettings() {
        setupCustomDurationSetting()
        setupBounceIntensitySetting()
        setupFadeTransitionsSetting()
        setupScaleAnimationsSetting()
        setupRotationEffectsSetting()
        setupParallaxEffectsSetting()
    }

    /**
     * Setup custom duration setting
     */
    private fun setupCustomDurationSetting() {
        val durationPreference = findPreference<Preference>(KEY_CUSTOM_DURATION)
        durationPreference?.apply {
            val currentDuration = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_CUSTOM_DURATION,
                DEFAULT_CUSTOM_DURATION
            )
            
            summary = "${currentDuration}ms"
            
            setOnPreferenceClickListener {
                showDurationDialog(currentDuration) { newDuration ->
                    Settings.System.putInt(
                        requireContext().contentResolver,
                        SETTING_CUSTOM_DURATION,
                        newDuration
                    )
                    summary = "${newDuration}ms"
                    triggerPreviewAnimation("duration_change")
                }
                true
            }
        }
    }

    /**
     * Setup bounce intensity setting
     */
    private fun setupBounceIntensitySetting() {
        val bouncePreference = findPreference<Preference>(KEY_BOUNCE_INTENSITY)
        bouncePreference?.apply {
            val currentIntensity = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_BOUNCE_INTENSITY,
                DEFAULT_BOUNCE_INTENSITY
            )
            
            summary = "${currentIntensity}%"
            
            setOnPreferenceClickListener {
                showIntensityDialog(currentIntensity) { newIntensity ->
                    Settings.System.putInt(
                        requireContext().contentResolver,
                        SETTING_BOUNCE_INTENSITY,
                        newIntensity
                    )
                    summary = "${newIntensity}%"
                    triggerPreviewAnimation("bounce_change")
                }
                true
            }
        }
    }

    /**
     * Setup fade transitions setting
     */
    private fun setupFadeTransitionsSetting() {
        val fadePreference = findPreference<SwitchPreference>(KEY_FADE_TRANSITIONS)
        fadePreference?.apply {
            isChecked = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_FADE_TRANSITIONS,
                1
            ) == 1
            
            setOnPreferenceChangeListener { _, newValue ->
                Settings.System.putInt(
                    requireContext().contentResolver,
                    SETTING_FADE_TRANSITIONS,
                    if (newValue as Boolean) 1 else 0
                )
                triggerPreviewAnimation("fade_change")
                true
            }
        }
    }

    /**
     * Setup scale animations setting
     */
    private fun setupScaleAnimationsSetting() {
        val scalePreference = findPreference<SwitchPreference>(KEY_SCALE_ANIMATIONS)
        scalePreference?.apply {
            isChecked = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_SCALE_ANIMATIONS,
                1
            ) == 1
            
            setOnPreferenceChangeListener { _, newValue ->
                Settings.System.putInt(
                    requireContext().contentResolver,
                    SETTING_SCALE_ANIMATIONS,
                    if (newValue as Boolean) 1 else 0
                )
                triggerPreviewAnimation("scale_change")
                true
            }
        }
    }

    /**
     * Setup rotation effects setting
     */
    private fun setupRotationEffectsSetting() {
        val rotationPreference = findPreference<SwitchPreference>(KEY_ROTATION_EFFECTS)
        rotationPreference?.apply {
            isChecked = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_ROTATION_EFFECTS,
                0
            ) == 1
            
            setOnPreferenceChangeListener { _, newValue ->
                Settings.System.putInt(
                    requireContext().contentResolver,
                    SETTING_ROTATION_EFFECTS,
                    if (newValue as Boolean) 1 else 0
                )
                triggerPreviewAnimation("rotation_change")
                true
            }
        }
    }

    /**
     * Setup parallax effects setting
     */
    private fun setupParallaxEffectsSetting() {
        val parallaxPreference = findPreference<SwitchPreference>(KEY_PARALLAX_EFFECTS)
        parallaxPreference?.apply {
            isChecked = Settings.System.getInt(
                requireContext().contentResolver,
                SETTING_PARALLAX_EFFECTS,
                0
            ) == 1
            
            setOnPreferenceChangeListener { _, newValue ->
                Settings.System.putInt(
                    requireContext().contentResolver,
                    SETTING_PARALLAX_EFFECTS,
                    if (newValue as Boolean) 1 else 0
                )
                triggerPreviewAnimation("parallax_change")
                true
            }
        }
    }

    /**
     * Create preview container
     */
    private fun createPreviewContainer(parentView: View?) {
        // This would create a preview area in the settings
        // For now, we'll use a simple view that can be animated
        
        parentView?.let { parent ->
            if (parent is ViewGroup) {
                val previewContainer = LayoutInflater.from(requireContext())
                    .inflate(R.layout.sdi_animation_preview, parent, false)
                
                previewView = previewContainer.findViewById<View>(R.id.sdi_preview_island)
                
                // Add to parent at top
                parent.addView(previewContainer, 0)
            }
        }
    }

    /**
     * Trigger preview animation
     */
    private fun triggerPreviewAnimation(trigger: String) {
        val preview = previewView ?: return
        
        Log.d(TAG, "Triggering preview animation: $trigger")
        
        // Cancel current animation
        currentPreviewAnimation?.cancel()
        
        // Get current settings
        val animationSpeed = Settings.System.getInt(
            requireContext().contentResolver,
            SETTING_ANIMATION_SPEED,
            DEFAULT_ANIMATION_SPEED
        )
        
        val animationStyle = Settings.System.getInt(
            requireContext().contentResolver,
            SETTING_ANIMATION_STYLE,
            DEFAULT_ANIMATION_STYLE
        )
        
        val customDuration = Settings.System.getInt(
            requireContext().contentResolver,
            SETTING_CUSTOM_DURATION,
            DEFAULT_CUSTOM_DURATION
        )
        
        val bounceIntensity = Settings.System.getInt(
            requireContext().contentResolver,
            SETTING_BOUNCE_INTENSITY,
            DEFAULT_BOUNCE_INTENSITY
        ) / 100f
        
        // Create animation based on current settings
        currentPreviewAnimation = createPreviewAnimation(
            preview, animationSpeed, animationStyle, customDuration, bounceIntensity
        )
        
        currentPreviewAnimation?.start()
        
        // Trigger haptic feedback if enabled
        if (Settings.System.getInt(requireContext().contentResolver, SETTING_HAPTIC_FEEDBACK, 1) == 1) {
            performHapticFeedback()
        }
    }

    /**
     * Create preview animation based on settings
     */
    private fun createPreviewAnimation(
        view: View,
        speed: Int,
        style: Int,
        duration: Int,
        bounceIntensity: Float
    ): Animator {
        val speedMultiplier = when (speed) {
            0 -> 1.5f // Slow
            1 -> 1.0f // Normal
            2 -> 0.75f // Fast
            3 -> 0.5f // Very Fast
            else -> 1.0f
        }
        
        val actualDuration = (duration * speedMultiplier).toLong()
        
        return when (style) {
            0 -> createSmoothAnimation(view, actualDuration) // Smooth
            1 -> createBouncyAnimation(view, actualDuration, bounceIntensity) // Bouncy
            2 -> createSharpAnimation(view, actualDuration) // Sharp
            3 -> createElasticAnimation(view, actualDuration, bounceIntensity) // Elastic
            else -> createSmoothAnimation(view, actualDuration)
        }
    }

    /**
     * Create smooth animation
     */
    private fun createSmoothAnimation(view: View, duration: Long): Animator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0.3f, 1.0f, 0.3f, 1.0f).apply {
            this.duration = duration
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }
    }

    /**
     * Create bouncy animation
     */
    private fun createBouncyAnimation(view: View, duration: Long, intensity: Float): Animator {
        val scaleMax = 1.0f + (intensity * 0.3f)
        return ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f, scaleMax, 1.0f).apply {
            this.duration = duration
            interpolator = android.view.animation.BounceInterpolator()
            
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f, scaleMax, 1.0f).apply {
                        this.duration = duration
                        interpolator = android.view.animation.BounceInterpolator()
                        start()
                    }
                }
            })
        }
    }

    /**
     * Create sharp animation
     */
    private fun createSharpAnimation(view: View, duration: Long): Animator {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -20f, 0f).apply {
            this.duration = duration
            interpolator = android.view.animation.DecelerateInterpolator(2.0f)
        }
    }

    /**
     * Create elastic animation
     */
    private fun createElasticAnimation(view: View, duration: Long, intensity: Float): Animator {
        val overshoot = 1.0f + intensity
        return ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 10f, -5f, 0f).apply {
            this.duration = duration
            interpolator = android.view.animation.OvershootInterpolator(overshoot)
        }
    }

    /**
     * Show duration selection dialog
     */
    private fun showDurationDialog(currentDuration: Int, onDurationSelected: (Int) -> Unit) {
        // This would show a dialog with a SeekBar for duration selection
        // For now, we'll cycle through common durations
        val durations = listOf(200, 300, 400, 500, 600, 800, 1000)
        val currentIndex = durations.indexOf(currentDuration).takeIf { it >= 0 } ?: 2
        val nextIndex = (currentIndex + 1) % durations.size
        onDurationSelected(durations[nextIndex])
    }

    /**
     * Show intensity selection dialog
     */
    private fun showIntensityDialog(currentIntensity: Int, onIntensitySelected: (Int) -> Unit) {
        // This would show a dialog with a SeekBar for intensity selection
        // For now, we'll cycle through common intensities
        val intensities = listOf(25, 50, 75, 100)
        val currentIndex = intensities.indexOf(currentIntensity).takeIf { it >= 0 } ?: 1
        val nextIndex = (currentIndex + 1) % intensities.size
        onIntensitySelected(intensities[nextIndex])
    }

    /**
     * Perform haptic feedback
     */
    private fun performHapticFeedback() {
        try {
            view?.performHapticFeedback(
                android.view.HapticFeedbackConstants.VIRTUAL_KEY,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform haptic feedback", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentPreviewAnimation?.cancel()
        currentPreviewAnimation = null
        previewView = null
    }
}
