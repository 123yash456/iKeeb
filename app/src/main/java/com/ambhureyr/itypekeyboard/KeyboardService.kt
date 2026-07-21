package com.ambhureyr.itypekeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection

class KeyboardService : InputMethodService() {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard, null) as IKeebView
        keyboard = Keyboard(this, R.xml.qwerty)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(listener)
        return keyboardView
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val ic: InputConnection = currentInputConnection!!
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> ic.deleteSurroundingText(1, 0)
            KeyEvent.KEYCODE_ENTER -> ic.commitText("\n", 1)
            else -> ic.sendKeyEvent(event!!)
        }
        return true
    }

    private val listener = object : KeyboardView.OnKeyboardActionListener {
        override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
            val ic = currentInputConnection!!

            when (primaryCode) {
                Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1, 0)
                Keyboard.KEYCODE_SHIFT -> {
                    keyboard.isShifted = !keyboard.isShifted
                    keyboardView.invalidateAllKeys()
                }
                10, Keyboard.KEYCODE_DONE -> ic.commitText("\n", 1)
                else -> {
                    var code = primaryCode
                    if (keyboard.isShifted && Character.isLowerCase(code)) {
                        code = Character.toUpperCase(code)
                    }
                    ic.commitText(code.toChar().toString(), 1)
                }
            }
        }

        override fun onPress(keyCode: Int) {}
        override fun onRelease(keyCode: Int) {}
        override fun onText(text: CharSequence?) {}
        override fun swipeLeft() {}
        override fun swipeRight() {}
        override fun swipeDown() {}
        override fun swipeUp() {}
    }
}