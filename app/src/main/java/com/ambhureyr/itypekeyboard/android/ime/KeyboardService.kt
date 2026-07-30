package com.ambhureyr.itypekeyboard.android.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import com.ambhureyr.itypekeyboard.R
import com.ambhureyr.itypekeyboard.android.view.IKeebView
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout

class KeyboardService : InputMethodService(), IKeebView.OnKeyActionListener {

    private lateinit var keebView: IKeebView

    override fun onCreateInputView(): View {
        keebView = layoutInflater.inflate(R.layout.keyboard, null) as IKeebView
        keebView.listener = this
        return keebView
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val ic: InputConnection = currentInputConnection ?: return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> ic.deleteSurroundingText(1, 0)
            KeyEvent.KEYCODE_ENTER -> ic.commitText("\n", 1)
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKey(primaryCode: Int) {
        val ic = currentInputConnection ?: return

        when (primaryCode) {
            KeyboardLayout.KEYCODE_DELETE -> {
                ic.deleteSurroundingText(1, 0)
            }
            KeyboardLayout.KEYCODE_SHIFT -> {
                keebView.isShifted = !keebView.isShifted
            }
            KeyboardLayout.KEYCODE_ENTER -> {
                ic.commitText("\n", 1)
            }
            else -> {
                var code = primaryCode
                if (keebView.isShifted && Character.isLowerCase(code)) {
                    code = Character.toUpperCase(code)
                }
                ic.commitText(code.toChar().toString(), 1)
            }
        }
    }
}
