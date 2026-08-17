package com.example

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class KeyboardLayoutType(val displayName: String) {
    FULL_PC("Full PC (with Function row)"),
    COMPACT_PC("Compact PC (Merged)"),
    PC_NAVIGATION("PC + Navigation Cluster")
}

enum class KeyboardHeight(val displayName: String, val scaleFactor: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.18f)
}

enum class KeyboardPosition(val displayName: String) {
    BOTTOM("Bottom"),
    FLOATING("Floating / Raised")
}

enum class KeyboardThemeType(val displayName: String) {
    CLASSIC_BLACK("Classic Black"),
    CLASSIC_WHITE("Classic White"),
    GLASSMORPHISM("Glassmorphism"),
    RGB_BLACK("RGB Black"),
    RGB_WHITE("RGB White"),
    AMOLED_BLACK("AMOLED Black"),
    MINIMAL("Minimal")
}

enum class RgbMode(val displayName: String) {
    OFF("RGB Off"),
    STATIC("Static RGB"),
    ANIMATED("Animated RGB")
}

enum class RgbSpeed(val displayName: String, val durationMs: Int) {
    SLOW("Slow", 5000),
    MEDIUM("Medium", 3000),
    FAST("Fast", 1500)
}

enum class KeyPressEffect(val displayName: String) {
    OFF("Off"),
    SUBTLE("Subtle (~60ms)"),
    STRONG("Strong Glow & Scale (~90ms)")
}

enum class SoundStyle(val displayName: String) {
    SOFT("Soft Tap"),
    MECHANICAL("Mechanical Click"),
    CLASSIC_PC("Classic PC"),
    CLICK("Modern Click"),
    TYPEWRITER("Typewriter")
}

enum class HapticStrength(val displayName: String) {
    LIGHT("Light"),
    MEDIUM("Medium"),
    STRONG("Strong")
}

enum class BackgroundType(val displayName: String) {
    SOLID("Solid"),
    GRADIENT("Gradient"),
    TRANSPARENT("Transparent"),
    GLASS("Glass"),
    RGB_ACCENT("RGB Accent")
}

class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pc_keyboard_settings", Context.MODE_PRIVATE)

    // Reactive StateFlows for real-time UI updates
    private val _layoutType = MutableStateFlow(getSavedLayoutType())
    val layoutType: StateFlow<KeyboardLayoutType> = _layoutType.asStateFlow()

    private val _landscapeLayoutType = MutableStateFlow(getSavedLandscapeLayoutType())
    val landscapeLayoutType: StateFlow<KeyboardLayoutType> = _landscapeLayoutType.asStateFlow()

    private val _keyboardHeight = MutableStateFlow(getSavedKeyboardHeight())
    val keyboardHeight: StateFlow<KeyboardHeight> = _keyboardHeight.asStateFlow()

    private val _keyHeight = MutableStateFlow(prefs.getFloat(KEY_KEY_HEIGHT, 44f))
    val keyHeight: StateFlow<Float> = _keyHeight.asStateFlow()

    private val _keySpacing = MutableStateFlow(prefs.getFloat(KEY_KEY_SPACING, 2f))
    val keySpacing: StateFlow<Float> = _keySpacing.asStateFlow()

    private val _keyboardPosition = MutableStateFlow(getSavedKeyboardPosition())
    val keyboardPosition: StateFlow<KeyboardPosition> = _keyboardPosition.asStateFlow()

    private val _themeType = MutableStateFlow(getSavedThemeType())
    val themeType: StateFlow<KeyboardThemeType> = _themeType.asStateFlow()

    private val _backgroundType = MutableStateFlow(getSavedBackgroundType())
    val backgroundType: StateFlow<BackgroundType> = _backgroundType.asStateFlow()

    private val _transparency = MutableStateFlow(prefs.getInt(KEY_TRANSPARENCY, 0))
    val transparency: StateFlow<Int> = _transparency.asStateFlow()

    private val _rgbMode = MutableStateFlow(getSavedRgbMode())
    val rgbMode: StateFlow<RgbMode> = _rgbMode.asStateFlow()

    private val _rgbSpeed = MutableStateFlow(getSavedRgbSpeed())
    val rgbSpeed: StateFlow<RgbSpeed> = _rgbSpeed.asStateFlow()

    private val _rgbBrightness = MutableStateFlow(prefs.getFloat(KEY_RGB_BRIGHTNESS, 0.9f))
    val rgbBrightness: StateFlow<Float> = _rgbBrightness.asStateFlow()

    private val _keyPressEffect = MutableStateFlow(getSavedKeyPressEffect())
    val keyPressEffect: StateFlow<KeyPressEffect> = _keyPressEffect.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _soundStyle = MutableStateFlow(getSavedSoundStyle())
    val soundStyle: StateFlow<SoundStyle> = _soundStyle.asStateFlow()

    private val _soundVolume = MutableStateFlow(prefs.getFloat(KEY_SOUND_VOLUME, 0.7f))
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC_ENABLED, true))
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _hapticStrength = MutableStateFlow(getSavedHapticStrength())
    val hapticStrength: StateFlow<HapticStrength> = _hapticStrength.asStateFlow()

    private val _isClipboardEnabled = MutableStateFlow(prefs.getBoolean(KEY_CLIPBOARD_ENABLED, true))
    val isClipboardEnabled: StateFlow<Boolean> = _isClipboardEnabled.asStateFlow()

    private val _clipboardLimit = MutableStateFlow(prefs.getInt(KEY_CLIPBOARD_LIMIT, 30))
    val clipboardLimit: StateFlow<Int> = _clipboardLimit.asStateFlow()

    // Getters from Shared Preferences
    private fun getSavedLayoutType(): KeyboardLayoutType {
        val name = prefs.getString(KEY_LAYOUT_TYPE, KeyboardLayoutType.FULL_PC.name)
        return try { KeyboardLayoutType.valueOf(name ?: KeyboardLayoutType.FULL_PC.name) } catch (e: Exception) { KeyboardLayoutType.FULL_PC }
    }

    private fun getSavedLandscapeLayoutType(): KeyboardLayoutType {
        val name = prefs.getString(KEY_LANDSCAPE_LAYOUT_TYPE, KeyboardLayoutType.FULL_PC.name)
        return try { KeyboardLayoutType.valueOf(name ?: KeyboardLayoutType.FULL_PC.name) } catch (e: Exception) { KeyboardLayoutType.FULL_PC }
    }

    private fun getSavedKeyboardHeight(): KeyboardHeight {
        val name = prefs.getString(KEY_KEYBOARD_HEIGHT, KeyboardHeight.MEDIUM.name)
        return try { KeyboardHeight.valueOf(name ?: KeyboardHeight.MEDIUM.name) } catch (e: Exception) { KeyboardHeight.MEDIUM }
    }

    private fun getSavedKeyboardPosition(): KeyboardPosition {
        val name = prefs.getString(KEY_KEYBOARD_POSITION, KeyboardPosition.BOTTOM.name)
        return try { KeyboardPosition.valueOf(name ?: KeyboardPosition.BOTTOM.name) } catch (e: Exception) { KeyboardPosition.BOTTOM }
    }

    private fun getSavedThemeType(): KeyboardThemeType {
        val name = prefs.getString(KEY_THEME_TYPE, KeyboardThemeType.CLASSIC_BLACK.name)
        return try { KeyboardThemeType.valueOf(name ?: KeyboardThemeType.CLASSIC_BLACK.name) } catch (e: Exception) { KeyboardThemeType.CLASSIC_BLACK }
    }

    private fun getSavedBackgroundType(): BackgroundType {
        val name = prefs.getString(KEY_BACKGROUND_TYPE, BackgroundType.SOLID.name)
        return try { BackgroundType.valueOf(name ?: BackgroundType.SOLID.name) } catch (e: Exception) { BackgroundType.SOLID }
    }

    private fun getSavedRgbMode(): RgbMode {
        val name = prefs.getString(KEY_RGB_MODE, RgbMode.OFF.name)
        return try { RgbMode.valueOf(name ?: RgbMode.OFF.name) } catch (e: Exception) { RgbMode.OFF }
    }

    private fun getSavedRgbSpeed(): RgbSpeed {
        val name = prefs.getString(KEY_RGB_SPEED, RgbSpeed.MEDIUM.name)
        return try { RgbSpeed.valueOf(name ?: RgbSpeed.MEDIUM.name) } catch (e: Exception) { RgbSpeed.MEDIUM }
    }

    private fun getSavedKeyPressEffect(): KeyPressEffect {
        val name = prefs.getString(KEY_KEY_PRESS_EFFECT, KeyPressEffect.SUBTLE.name)
        return try { KeyPressEffect.valueOf(name ?: KeyPressEffect.SUBTLE.name) } catch (e: Exception) { KeyPressEffect.SUBTLE }
    }

    private fun getSavedSoundStyle(): SoundStyle {
        val name = prefs.getString(KEY_SOUND_STYLE, SoundStyle.MECHANICAL.name)
        return try { SoundStyle.valueOf(name ?: SoundStyle.MECHANICAL.name) } catch (e: Exception) { SoundStyle.MECHANICAL }
    }

    private fun getSavedHapticStrength(): HapticStrength {
        val name = prefs.getString(KEY_HAPTIC_STRENGTH, HapticStrength.MEDIUM.name)
        return try { HapticStrength.valueOf(name ?: HapticStrength.MEDIUM.name) } catch (e: Exception) { HapticStrength.MEDIUM }
    }

    // Setters
    fun setLayoutType(type: KeyboardLayoutType) {
        prefs.edit().putString(KEY_LAYOUT_TYPE, type.name).apply()
        _layoutType.value = type
    }

    fun setLandscapeLayoutType(type: KeyboardLayoutType) {
        prefs.edit().putString(KEY_LANDSCAPE_LAYOUT_TYPE, type.name).apply()
        _landscapeLayoutType.value = type
    }

    fun setKeyboardHeight(height: KeyboardHeight) {
        prefs.edit().putString(KEY_KEYBOARD_HEIGHT, height.name).apply()
        _keyboardHeight.value = height
    }

    fun setKeyHeight(heightDp: Float) {
        prefs.edit().putFloat(KEY_KEY_HEIGHT, heightDp).apply()
        _keyHeight.value = heightDp
    }

    fun setKeySpacing(spacingDp: Float) {
        prefs.edit().putFloat(KEY_KEY_SPACING, spacingDp).apply()
        _keySpacing.value = spacingDp
    }

    fun setKeyboardPosition(position: KeyboardPosition) {
        prefs.edit().putString(KEY_KEYBOARD_POSITION, position.name).apply()
        _keyboardPosition.value = position
    }

    fun setThemeType(theme: KeyboardThemeType) {
        prefs.edit().putString(KEY_THEME_TYPE, theme.name).apply()
        _themeType.value = theme
    }

    fun setBackgroundType(type: BackgroundType) {
        prefs.edit().putString(KEY_BACKGROUND_TYPE, type.name).apply()
        _backgroundType.value = type
    }

    fun setTransparency(percent: Int) {
        val clamped = percent.coerceIn(0, 80)
        prefs.edit().putInt(KEY_TRANSPARENCY, clamped).apply()
        _transparency.value = clamped
    }

    fun setRgbMode(mode: RgbMode) {
        prefs.edit().putString(KEY_RGB_MODE, mode.name).apply()
        _rgbMode.value = mode
    }

    fun setRgbSpeed(speed: RgbSpeed) {
        prefs.edit().putString(KEY_RGB_SPEED, speed.name).apply()
        _rgbSpeed.value = speed
    }

    fun setRgbBrightness(brightness: Float) {
        val clamped = brightness.coerceIn(0.1f, 1.0f)
        prefs.edit().putFloat(KEY_RGB_BRIGHTNESS, clamped).apply()
        _rgbBrightness.value = clamped
    }

    fun setKeyPressEffect(effect: KeyPressEffect) {
        prefs.edit().putString(KEY_KEY_PRESS_EFFECT, effect.name).apply()
        _keyPressEffect.value = effect
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _isSoundEnabled.value = enabled
    }

    fun setSoundStyle(style: SoundStyle) {
        prefs.edit().putString(KEY_SOUND_STYLE, style.name).apply()
        _soundStyle.value = style
    }

    fun setSoundVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        prefs.edit().putFloat(KEY_SOUND_VOLUME, clamped).apply()
        _soundVolume.value = clamped
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
        _isHapticEnabled.value = enabled
    }

    fun setHapticStrength(strength: HapticStrength) {
        prefs.edit().putString(KEY_HAPTIC_STRENGTH, strength.name).apply()
        _hapticStrength.value = strength
    }

    fun setClipboardEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLIPBOARD_ENABLED, enabled).apply()
        _isClipboardEnabled.value = enabled
    }

    fun setClipboardLimit(limit: Int) {
        val clamped = limit.coerceIn(10, 100)
        prefs.edit().putInt(KEY_CLIPBOARD_LIMIT, clamped).apply()
        _clipboardLimit.value = clamped
    }

    companion object {
        private const val KEY_LAYOUT_TYPE = "layout_type"
        private const val KEY_LANDSCAPE_LAYOUT_TYPE = "landscape_layout_type"
        private const val KEY_KEYBOARD_HEIGHT = "keyboard_height"
        private const val KEY_KEY_HEIGHT = "key_height"
        private const val KEY_KEY_SPACING = "key_spacing"
        private const val KEY_KEYBOARD_POSITION = "keyboard_position"
        private const val KEY_THEME_TYPE = "theme_type"
        private const val KEY_BACKGROUND_TYPE = "background_type"
        private const val KEY_TRANSPARENCY = "transparency"
        private const val KEY_RGB_MODE = "rgb_mode"
        private const val KEY_RGB_SPEED = "rgb_speed"
        private const val KEY_RGB_BRIGHTNESS = "rgb_brightness"
        private const val KEY_KEY_PRESS_EFFECT = "key_press_effect"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_SOUND_STYLE = "sound_style"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_HAPTIC_STRENGTH = "haptic_strength"
        private const val KEY_CLIPBOARD_ENABLED = "clipboard_enabled"
        private const val KEY_CLIPBOARD_LIMIT = "clipboard_limit"

        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
