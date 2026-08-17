package com.example

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager private constructor(private val context: Context) {

    private val settings = SettingsManager.getInstance(context)
    private var soundPool: SoundPool? = null
    
    // Map of SoundStyle to SoundPool sound IDs
    private val soundIds = mutableMapOf<SoundStyle, Int>()
    private var isLoaded = false

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    isLoaded = true
                }
            }

            // Generate WAV sound files in cache dir for SoundPool
            val cacheDir = context.cacheDir
            SoundStyle.values().forEach { style ->
                try {
                    val file = File(cacheDir, "kbd_sound_${style.name.lowercase()}.wav")
                    if (!file.exists() || file.length() == 0L) {
                        val wavBytes = generateWavData(style)
                        FileOutputStream(file).use { it.write(wavBytes) }
                    }
                    val soundId = pool.load(file.absolutePath, 1)
                    soundIds[style] = soundId
                } catch (e: Exception) {
                    Log.e("SoundManager", "Error generating/loading sound for $style", e)
                }
            }

            soundPool = pool
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to initialize SoundPool", e)
        }
    }

    fun playKeySound(action: KeyAction) {
        if (!settings.isSoundEnabled.value) return

        try {
            val pool = soundPool ?: return
            val style = settings.soundStyle.value
            val soundId = soundIds[style] ?: return
            val baseVolume = settings.soundVolume.value

            // Custom pitch and volume modulations for realistic PC keyboard typing feel
            val (pitch, volumeMult) = when (action) {
                is KeyAction.Space -> Pair(0.78f, 1.15f) // Deep physical spacebar thock
                is KeyAction.Enter -> Pair(0.88f, 1.1f)  // Solid tactile enter press
                is KeyAction.Backspace -> Pair(1.18f, 1.0f) // Crisp reset click
                is KeyAction.Shift, is KeyAction.CapsLock, is KeyAction.Ctrl, is KeyAction.Alt, is KeyAction.Meta -> Pair(1.25f, 0.9f) // High snappy click
                is KeyAction.Esc, is KeyAction.Tab -> Pair(1.1f, 0.95f)
                else -> Pair(0.98f + (Math.random().toFloat() * 0.05f), 1.0f) // Slight subtle pitch variation
            }

            val finalVolume = (baseVolume * volumeMult).coerceIn(0.0f, 1.0f)
            pool.play(soundId, finalVolume, finalVolume, 1, 0, pitch)
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing key sound", e)
        }
    }

    /**
     * Synthesizes realistic 16-bit PCM audio WAV for each keyboard sound profile
     */
    private fun generateWavData(style: SoundStyle): ByteArray {
        val sampleRate = 44100
        val durationMs = when (style) {
            SoundStyle.SOFT -> 35
            SoundStyle.CLICK -> 40
            SoundStyle.MECHANICAL -> 65
            SoundStyle.CLASSIC_PC -> 55
            SoundStyle.TYPEWRITER -> 85
        }
        val totalSamples = (sampleRate * durationMs) / 1000
        val pcmData = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples

            val sample: Double = when (style) {
                SoundStyle.MECHANICAL -> {
                    // Sharp high-transient click followed by a warm tactile body thock
                    val click = sin(2.0 * PI * 3200.0 * t) * exp(-progress * 45.0)
                    val body = sin(2.0 * PI * 420.0 * t) * exp(-progress * 18.0) * 0.7
                    val sub = sin(2.0 * PI * 180.0 * t) * exp(-progress * 12.0) * 0.4
                    (click * 0.6 + body + sub)
                }
                SoundStyle.TYPEWRITER -> {
                    // Metal hammer strike with high metallic chirp and resonance
                    val strike = (Math.random() * 2.0 - 1.0) * exp(-progress * 30.0) * 0.5
                    val metallic = sin(2.0 * PI * 4800.0 * t) * exp(-progress * 35.0) * 0.5
                    val carriage = sin(2.0 * PI * 650.0 * t) * exp(-progress * 14.0) * 0.6
                    (strike + metallic + carriage)
                }
                SoundStyle.CLASSIC_PC -> {
                    // Classic IBM spring buckler & square-sine click
                    val click = sin(2.0 * PI * 2400.0 * t) * exp(-progress * 28.0)
                    val tone = sin(2.0 * PI * 850.0 * t) * exp(-progress * 20.0) * 0.5
                    (click * 0.7 + tone * 0.5)
                }
                SoundStyle.CLICK -> {
                    // Modern subtle crisp switch
                    val high = sin(2.0 * PI * 4000.0 * t) * exp(-progress * 50.0) * 0.8
                    val pop = sin(2.0 * PI * 900.0 * t) * exp(-progress * 30.0) * 0.4
                    (high + pop)
                }
                SoundStyle.SOFT -> {
                    // Gentle damped membrane tap
                    val tap = sin(2.0 * PI * 300.0 * t) * exp(-progress * 25.0) * 0.7
                    val softPop = sin(2.0 * PI * 150.0 * t) * exp(-progress * 15.0) * 0.5
                    (tap + softPop)
                }
            }

            val clamped = (sample * 24000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            pcmData[i] = clamped.toShort()
        }

        return createWavFileBytes(pcmData, sampleRate)
    }

    private fun createWavFileBytes(pcmData: ShortArray, sampleRate: Int): ByteArray {
        val byteRate = sampleRate * 2 // 16-bit mono = 2 bytes per sample
        val dataSize = pcmData.size * 2
        val totalSize = 36 + dataSize

        val buffer = ByteBuffer.allocate(44 + dataSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF Header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalSize)
        buffer.put("WAVE".toByteArray())

        // Format chunk
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Subchunk1Size (16 for PCM)
        buffer.putShort(1) // AudioFormat (1 for PCM)
        buffer.putShort(1) // NumChannels (1 = mono)
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(2) // BlockAlign
        buffer.putShort(16) // BitsPerSample

        // Data chunk
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)

        for (sample in pcmData) {
            buffer.putShort(sample)
        }

        return buffer.array()
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {
            Log.e("SoundManager", "Error releasing SoundPool", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SoundManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
