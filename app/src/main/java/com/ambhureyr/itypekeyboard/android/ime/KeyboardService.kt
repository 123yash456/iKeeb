package com.ambhureyr.itypekeyboard.android.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.ambhureyr.itypekeyboard.R
import com.ambhureyr.itypekeyboard.android.view.IKeebView
import com.ambhureyr.itypekeyboard.engine.audio.SoundManager
import com.ambhureyr.itypekeyboard.engine.dictionary.Autocorrector
import com.ambhureyr.itypekeyboard.engine.dictionary.Dictionary
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout

class KeyboardService : InputMethodService(), IKeebView.OnKeyActionListener {

    private lateinit var keebView: IKeebView
    private lateinit var autocorrector: Autocorrector
    private lateinit var soundManager: SoundManager

    // Tracks the word currently being tap-typed so it can be checked/corrected
    // once the user finishes it (space, punctuation, or enter).
    private val currentWord = StringBuilder()

    override fun onCreate() {
        super.onCreate()
        soundManager = SoundManager(applicationContext)
    }

    override fun onCreateInputView(): View {
        keebView = layoutInflater.inflate(R.layout.keyboard, null) as IKeebView
        keebView.listener = this
        // Built here (not onCreate) so it can share keebView's UserDictionary and
        // SpatialKeyMap -- same personal vocabulary and real key positions used by slide typing.
        autocorrector = Autocorrector(Dictionary.get(applicationContext), keebView.userDictionary, keebView.spatialKeyMap)
        return keebView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentWord.clear()
        if (::keebView.isInitialized) {
            keebView.keyboardLayout.measure(keebView.width.toFloat(), keebView.height.toFloat(), context = keebView.context)
            keebView.refreshSpatialMap()
            keebView.invalidate()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val ic: InputConnection = currentInputConnection ?: return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                soundManager.playBackspace()
                deleteBackward(ic)
            }
            KeyEvent.KEYCODE_ENTER -> {
                soundManager.playSpaceOrReturn()
                handleReturnKey(ic)
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKey(primaryCode: Int) {
        val ic = currentInputConnection ?: return

        when (primaryCode) {
            KeyboardLayout.KEYCODE_DELETE -> {
                soundManager.playBackspace()
                deleteBackward(ic)
            }
            KeyboardLayout.KEYCODE_SHIFT -> {
                soundManager.playShift()
                // Handled in IKeebView shiftState
            }
            KeyboardLayout.KEYCODE_ENTER -> {
                soundManager.playSpaceOrReturn()
                applyAutocorrectIfNeeded(ic)
                handleReturnKey(ic)
            }
            32 -> {
                // Space bar
                soundManager.playSpaceOrReturn()
                applyAutocorrectIfNeeded(ic)
                ic.commitText(" ", 1)
                currentWord.clear()
            }
            -2, -3 -> {
                soundManager.playKeyClick()
                // "123"/"ABC": from the emoji panel this exits back to whichever
                // mode opened it; otherwise it toggles symbol <-> letters as usual.
                if (keebView.keyboardLayout.isEmojiMode) {
                    keebView.keyboardLayout.closeEmojiPanel()
                } else {
                    keebView.keyboardLayout.isSymbolMode = !keebView.keyboardLayout.isSymbolMode
                }
                keebView.keyboardLayout.measure(keebView.width.toFloat(), keebView.height.toFloat(), context = keebView.context)
                keebView.refreshSpatialMap()
                keebView.invalidate()
            }
            KeyboardLayout.KEYCODE_EMOJI -> {
                soundManager.playKeyClick()
                keebView.keyboardLayout.openEmojiPanel()
                keebView.keyboardLayout.measure(keebView.width.toFloat(), keebView.height.toFloat(), context = keebView.context)
                keebView.refreshSpatialMap()
                keebView.invalidate()
            }
            KeyboardLayout.KEYCODE_EMOJI_NEXT_PAGE -> {
                soundManager.playKeyClick()
                // Same row/key shape across pages, so no re-measure needed.
                keebView.keyboardLayout.nextEmojiPage()
                keebView.refreshSpatialMap()
                keebView.invalidate()
            }
            else -> {
                soundManager.playKeyClick()
                var code = primaryCode
                val isLetter = Character.isLetter(code)

                // Reaching a word boundary (punctuation, digit) -- correct
                // the word that was just finished before committing the boundary char.
                if (!isLetter && currentWord.isNotEmpty()) {
                    applyAutocorrectIfNeeded(ic)
                }

                val isShiftActive = keebView.shiftState != IKeebView.ShiftState.OFF
                if (isShiftActive && Character.isLowerCase(code)) {
                    code = Character.toUpperCase(code)
                }
                val finalChar = code.toChar()
                ic.commitText(finalChar.toString(), 1)

                if (isLetter) {
                    currentWord.append(finalChar)
                } else {
                    currentWord.clear()
                }

                // If shift was ON (single shift), reset to OFF after typing one character
                if (keebView.shiftState == IKeebView.ShiftState.ON) {
                    keebView.shiftState = IKeebView.ShiftState.OFF
                }
            }
        }
    }

    /**
     * Checks the word tracked in [currentWord] against the dictionary and, if a
     * confident correction exists, swaps it in place before the word boundary
     * character (space/punctuation/enter) gets committed.
     */
    private fun applyAutocorrectIfNeeded(ic: InputConnection) {
        if (currentWord.isEmpty()) return
        val word = currentWord.toString()
        val suggestion = autocorrector.correct(word)
        if (suggestion != null && suggestion != word) {
            ic.deleteSurroundingText(word.length, 0)
            ic.commitText(suggestion, 1)
        }
        currentWord.clear()
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
            soundManager.playBackspace()
            deleteWordBackward(ic)
        }
    }

    override fun onTextKey(text: String) {
        val ic = currentInputConnection ?: return
        soundManager.playKeyClick()
        // A kaomoji ends whatever word was mid-flight, same as punctuation does.
        applyAutocorrectIfNeeded(ic)
        ic.commitText(text, 1)
    }

    override fun onSlideWordCommitted(word: String) {
        val ic = currentInputConnection ?: return
        soundManager.playSpaceOrReturn()

        // A slide-typed word replaces whatever partial word was being tap-typed.
        currentWord.clear()

        val isShiftActive = keebView.shiftState != IKeebView.ShiftState.OFF
        val isCapsLock = keebView.shiftState == IKeebView.ShiftState.CAPS_LOCK

        val output = when {
            isCapsLock -> word.uppercase()
            isShiftActive -> word.replaceFirstChar { it.uppercase() }
            else -> word
        }

        ic.commitText("$output ", 1)

        // Single shift is consumed after one committed word, same as a single typed key
        if (keebView.shiftState == IKeebView.ShiftState.ON) {
            keebView.shiftState = IKeebView.ShiftState.OFF
        }
    }

    private fun deleteBackward(ic: InputConnection) {
        if (currentWord.isNotEmpty()) {
            currentWord.deleteCharAt(currentWord.length - 1)
        }

        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }
        ic.deleteSurroundingText(1, 0)
    }

    private fun deleteWordBackward(ic: InputConnection) {
        currentWord.clear()

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

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
