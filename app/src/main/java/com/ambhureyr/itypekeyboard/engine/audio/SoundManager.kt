package com.ambhureyr.itypekeyboard.engine.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(private val context: Context) {
    private val soundPool: SoundPool
    private val soundMap = HashMap<String, Int>()
    private var isLoaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            val assetManager = context.assets
            soundMap["key"] = soundPool.load(assetManager.openFd("Key_boosted_170.mp3"), 1)
            soundMap["backspace"] = soundPool.load(assetManager.openFd("Backspace_boosted_170.mp3"), 1)
            soundMap["shift"] = soundPool.load(assetManager.openFd("Shift_boosted_170.mp3"), 1)
            soundMap["space_return"] = soundPool.load(assetManager.openFd("Space and Return_boosted_170.mp3"), 1)
            isLoaded = true
        } catch (e: Exception) {
            Log.e("SoundManager", "Error loading sound assets", e)
        }
    }

    fun playKeyClick() {
        play("key")
    }

    fun playBackspace() {
        play("backspace")
    }

    fun playShift() {
        play("shift")
    }

    fun playSpaceOrReturn() {
        play("space_return")
    }

    private fun play(soundKey: String) {
        if (!isLoaded) return
        val soundId = soundMap[soundKey] ?: return

        // Read user-configured volume from SharedPreferences (default 100% -> amplified multiplier 5.0x max)
        val prefs = context.getSharedPreferences("ikeeb_settings", Context.MODE_PRIVATE)
        val volumePercent = prefs.getInt("key_volume", 100)
        // Amplify volume up to 5.0x max for crisp clarity
        val volume = (volumePercent / 100f) * 5.0f

        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }
}
