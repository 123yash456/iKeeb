package com.ambhureyr.itypekeyboard.android.settings

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.ambhureyr.itypekeyboard.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("ikeeb_settings", Context.MODE_PRIVATE)

        // 1. Volume
        val currentVolume = prefs.getInt("key_volume", 100)
        val volumeSeekBar = findViewById<SeekBar>(R.id.volumeSeekBar)
        val volumeText = findViewById<TextView>(R.id.volumeText)
        volumeSeekBar.progress = currentVolume
        volumeText.text = "$currentVolume%"
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeText.text = "$progress%"
                if (fromUser) prefs.edit().putInt("key_volume", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 2. Slide Typing Switch
        val slideSwitch = findViewById<SwitchCompat>(R.id.slideTypingSwitch)
        slideSwitch.isChecked = prefs.getBoolean("slide_typing_enabled", true)
        slideSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("slide_typing_enabled", isChecked).apply()
        }

        // 3. Keyboard Scale (80% to 130%, progress 0 to 50 -> 80 + progress)
        val currentScale = prefs.getInt("keyboard_scale", 100)
        val scaleSeekBar = findViewById<SeekBar>(R.id.scaleSeekBar)
        val scaleText = findViewById<TextView>(R.id.scaleText)
        scaleSeekBar.progress = currentScale - 80
        scaleText.text = "$currentScale%"
        scaleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualScale = 80 + progress
                scaleText.text = "$actualScale%"
                if (fromUser) prefs.edit().putInt("keyboard_scale", actualScale).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 4. Font Size (80% to 160%, progress 0 to 30 -> 80 + progress * 2.66 -> let's say 80 + progress)
        val currentFont = prefs.getInt("font_scale", 100)
        val fontSeekBar = findViewById<SeekBar>(R.id.fontSizeSeekBar)
        val fontText = findViewById<TextView>(R.id.fontSizeText)
        fontSeekBar.progress = currentFont - 80
        fontText.text = "$currentFont%"
        fontSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualFont = 80 + progress
                fontText.text = "$actualFont%"
                if (fromUser) prefs.edit().putInt("font_scale", actualFont).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
