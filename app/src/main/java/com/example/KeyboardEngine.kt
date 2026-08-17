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

    private val _isFnActive = MutableStateFlow(false)
    val isFnActive: StateFlow<Boolean> = _isFnActive.asStateFlow()

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
                KeyAction.DeleteKey -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_FORWARD_DEL)
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
                    _isFnActive.update { !it }
                }
                KeyAction.Tab -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_TAB)
                }
                KeyAction.Esc -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_ESCAPE)
                }
                KeyAction.Insert -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_INSERT)
                }
                KeyAction.Home -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_MOVE_HOME)
                }
                KeyAction.End -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_MOVE_END)
                }
                KeyAction.PageUp -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_PAGE_UP)
                }
                KeyAction.PageDown -> {
                    sendKeyChar(ic, KeyEvent.KEYCODE_PAGE_DOWN)
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
                "z" -> ic.performContextMenuAction(android.R.id.undo)
                "y" -> ic.performContextMenuAction(android.R.id.redo)
                else -> {}
            }
        } catch (e: Exception) {
            Log.e("KeyboardEngine", "Error handling shortcut", e)
        }
        _isCtrlActive.update { false }
    }

    fun executeQuickAction(actionType: String) {
        try {
            val service = try { getService() } catch (e: Exception) { null } ?: return
            val ic = service.currentInputConnection ?: return

            when (actionType) {
                "copy" -> ic.performContextMenuAction(android.R.id.copy)
                "paste" -> ic.performContextMenuAction(android.R.id.paste)
                "cut" -> ic.performContextMenuAction(android.R.id.cut)
                "selectAll" -> ic.performContextMenuAction(android.R.id.selectAll)
                "undo" -> {
                    if (!ic.performContextMenuAction(android.R.id.undo)) {
                        sendKeyChar(ic, KeyEvent.KEYCODE_Z)
                    }
                }
                "redo" -> {
                    if (!ic.performContextMenuAction(android.R.id.redo)) {
                        sendKeyChar(ic, KeyEvent.KEYCODE_Y)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("KeyboardEngine", "Error executing quick action $actionType", e)
        }
    }

    fun pasteText(text: String) {
        try {
            val service = try { getService() } catch (e: Exception) { null } ?: return
            val ic = service.currentInputConnection ?: return
            ic.commitText(text, 1)
        } catch (e: Exception) {
            Log.e("KeyboardEngine", "Error pasting text", e)
        }
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
