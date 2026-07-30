package com.ambhureyr.itypekeyboard.android.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
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

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // You can update return key label/icon based on IME action (e.g. Search, Send, Go, Next, Done) if needed
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val ic: InputConnection = currentInputConnection ?: return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> deleteBackward(ic)
            KeyEvent.KEYCODE_ENTER -> handleReturnKey(ic)
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKey(primaryCode: Int) {
        val ic = currentInputConnection ?: return

        when (primaryCode) {
            KeyboardLayout.KEYCODE_DELETE -> {
                deleteBackward(ic)
            }
            KeyboardLayout.KEYCODE_SHIFT -> {
                // Handled in IKeebView shiftState
            }
            KeyboardLayout.KEYCODE_ENTER -> {
                handleReturnKey(ic)
            }
            -2, -3 -> {
                // Toggle symbol / ABC mode and re-measure layout
                keebView.keyboardLayout.isSymbolMode = !keebView.keyboardLayout.isSymbolMode
                keebView.keyboardLayout.measure(keebView.width.toFloat(), keebView.height.toFloat())
                keebView.invalidate()
            }
            else -> {
                var code = primaryCode
                val isShiftActive = keebView.shiftState != IKeebView.ShiftState.OFF
                if (isShiftActive && Character.isLowerCase(code)) {
                    code = Character.toUpperCase(code)
                }
                ic.commitText(code.toChar().toString(), 1)

                // If shift was ON (single shift), reset to OFF after typing one character
                if (keebView.shiftState == IKeebView.ShiftState.ON) {
                    keebView.shiftState = IKeebView.ShiftState.OFF
                }
            }
        }
    }

    private fun handleReturnKey(ic: InputConnection) {
        val editorInfo = currentInputEditorInfo
        val actionId = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE

        when (actionId) {
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_DONE -> {
                currentInputConnection?.performEditorAction(actionId)
            }
            else -> {
                // Default action or multi-line newline insertion
                val action = editorInfo?.imeOptions ?: 0
                if (action and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) {
                    ic.commitText("\n", 1)
                } else {
                    // Try performing whatever action is specified, or fallback to sending enter key event / newline
                    if (!ic.performEditorAction(actionId)) {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                    }
                }
            }
        }
    }

    override fun onLongPressKey(primaryCode: Int) {
        val ic = currentInputConnection ?: return
        if (primaryCode == KeyboardLayout.KEYCODE_DELETE) {
            deleteWordBackward(ic)
        }
    }

    private fun deleteBackward(ic: InputConnection) {
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }
        ic.deleteSurroundingText(1, 0)
    }

    private fun deleteWordBackward(ic: InputConnection) {
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }

        val textBefore = ic.getTextBeforeCursor(50, 0) ?: ""
        if (textBefore.isEmpty()) {
            ic.deleteSurroundingText(1, 0)
            return
        }

        var deleteCount = 0
        val length = textBefore.length
        var i = length - 1

        while (i >= 0 && textBefore[i].isWhitespace()) {
            deleteCount++
            i--
        }

        while (i >= 0 && !textBefore[i].isWhitespace()) {
            deleteCount++
            i--
        }

        if (deleteCount > 0) {
            ic.deleteSurroundingText(deleteCount, 0)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }
}
