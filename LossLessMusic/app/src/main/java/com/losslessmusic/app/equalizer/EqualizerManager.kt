package com.losslessmusic.app.equalizer

import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log

class EqualizerManager(private val audioSessionId: Int) {
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    var isEnabled: Boolean = false
        private set

    fun enable() {
        try {
            if (equalizer == null) {
                equalizer = Equalizer(0, audioSessionId)
                equalizer?.let { eq ->
                    eq.enabled = false
                    val bandCount = eq.numberOfBands
                    val presetNames = eq.numberOfPresets
                    Log.d("Equalizer", "Bands: $bandCount, Presets: $presetNames")
                }
            }
            equalizer?.enabled = true
            isEnabled = true
        } catch (e: Exception) {
            Log.e("Equalizer", "Failed to enable eq: ${e.message}")
        }
    }

    fun disable() {
        equalizer?.enabled = false
        isEnabled = false
    }

    fun setBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
        } catch (e: Exception) {
            Log.e("Equalizer", "Failed to set band level: ${e.message}")
        }
    }

    fun getBandLevel(band: Short): Short {
        return try {
            equalizer?.getBandLevel(band) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getCenterFreq(band: Short): Int {
        return try {
            equalizer?.getCenterFreq(band) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    val numberOfBands: Short
        get() = try {
            equalizer?.numberOfBands ?: 0
        } catch (e: Exception) {
            0
        }

    val bandLevelRange: ShortArray?
        get() = try {
            equalizer?.bandLevelRange
        } catch (e: Exception) {
            null
        }

    fun release() {
        try {
            equalizer?.enabled = false
            equalizer?.release()
            loudnessEnhancer?.release()
        } catch (_: Exception) {}
        equalizer = null
        loudnessEnhancer = null
    }
}
