package com.example

import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class KeyboardEngine(private val getService: () -> PcKeyboardService) {

    private val _isShifted = MutableStateFlow(false)
    val isShifted: StateFlow<Boolean> = _isShifted.asStateFlow()

    private val _isCapsLock = MutableStateFlow(false)
    val isCapsLock: StateFlow<Boolean> = _isCapsLock.asStateFlow()

    private val _isCtrlActive = MutableStateFlow(false)
    val isCtrlActive: StateFlow<Boolean> = _isCtrlActive.asStateFlow()

    private val _isAltActive = MutableStateFlow(false)
    val isAltActive: StateFlow<Boolean> = _isAltActive.asStateFlow()

    private val _isMetaActive = MutableStateFlow(false)
    val isMetaActive: StateFlow<Boolean> = _isMetaActive.asStateFlow()

    fun onKeyPress(action: KeyAction) {
        try {
            val service = try { getService() } catch (e: Exception) { null } ?: return
            val ic = service.currentInputConnection ?: return
            
            when (action) {
                is KeyAction.Text -> {
                    val text = if (_isShifted.value || _isCapsLock.value) {
                        action.text.uppercase()
                    } else {
                        action.text
                    }
                    
                    if (_isCtrlActive.value) {
                        handleCtrlShortcut(action.text.lowercase(), ic)
                    } else {
                        ic.commitText(text, 1)
                    }
                    
                    // Turn off one-shot modifiers
                    if (_isShifted.value && !_isCapsLock.value) {
                        _isShifted.update { false }
                    }
                }
                is KeyAction.KeyCode -> {
                    sendKeyChar(ic, action.code)
                }
                KeyAction.Backspace -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_DEL)
                }
                KeyAction.Enter -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_ENTER)
                }
                KeyAction.Space -> {
                    ic.commitText(" ", 1)
                }
                KeyAction.Shift -> {
                    _isShifted.update { !it }
                }
                KeyAction.CapsLock -> {
                    _isCapsLock.update { !it }
                    if (_isCapsLock.value) {
                        _isShifted.update { true }
                    } else {
                        _isShifted.update { false }
                    }
                }
                KeyAction.Ctrl -> {
                    _isCtrlActive.update { !it }
                }
                KeyAction.Alt -> {
                    _isAltActive.update { !it }
                }
                KeyAction.Meta -> {
                    _isMetaActive.update { !it }
                }
                KeyAction.Fn -> {
                    // Toggle Fn layer
                }
                KeyAction.Tab -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_TAB)
                }
                KeyAction.Esc -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_ESCAPE)
                }
                KeyAction.Left -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_DPAD_LEFT)
                }
                KeyAction.Right -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_DPAD_RIGHT)
                }
                KeyAction.Up -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_DPAD_UP)
                }
                KeyAction.Down -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_DPAD_DOWN)
                }
            }
        } catch (e: Exception) {
            Log.e("KeyboardEngine", "Error handling key press", e)
        }
    }

    private fun handleCtrlShortcut(char: String, ic: InputConnection) {
        try {
            when (char) {
                "a" -> ic.performContextMenuAction(android.R.id.selectAll)
                "c" -> ic.performContextMenuAction(android.R.id.copy)
                "v" -> ic.performContextMenuAction(android.R.id.paste)
                "x" -> ic.performContextMenuAction(android.R.id.cut)
                else -> {}
            }
        } catch (e: Exception) {
            Log.e("KeyboardEngine", "Error handling shortcut", e)
        }
        _isCtrlActive.update { false }
    }

    private fun sendKeyChar(ic: InputConnection, keyCode: Int) {
        try {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        } catch (e: Exception) {
            Log.e("KeyboardEngine", "Error sending key event", e)
        }
    }
}
