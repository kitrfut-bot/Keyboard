package com.example

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class HapticManager private constructor(private val context: Context) {

    private val settings = SettingsManager.getInstance(context)

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.e("HapticManager", "Failed to obtain Vibrator service", e)
            null
        }
    }

    fun performHaptic(action: KeyAction? = null) {
        if (!settings.isHapticEnabled.value) return

        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            val strength = settings.hapticStrength.value

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (strength) {
                    HapticStrength.LIGHT -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        } else {
                            VibrationEffect.createOneShot(8, 60)
                        }
                    }
                    HapticStrength.MEDIUM -> {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    }
                    HapticStrength.STRONG -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                        } else {
                            VibrationEffect.createOneShot(22, 220)
                        }
                    }
                }
                vib.vibrate(effect)
            } else {
                val durationMs = when (strength) {
                    HapticStrength.LIGHT -> 8L
                    HapticStrength.MEDIUM -> 18L
                    HapticStrength.STRONG -> 32L
                }
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.e("HapticManager", "Error executing haptic feedback", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HapticManager? = null

        fun getInstance(context: Context): HapticManager {
            return INSTANCE ?: synchronized(this) {
                val instance = HapticManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
