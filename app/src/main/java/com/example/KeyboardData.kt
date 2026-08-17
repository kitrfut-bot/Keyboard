package com.example

sealed class KeyAction {
    data class Text(val text: String) : KeyAction()
    data class KeyCode(val code: Int) : KeyAction() // e.g. KeyEvent.KEYCODE_ENTER
    object Backspace : KeyAction()
    object Enter : KeyAction()
    object Shift : KeyAction()
    object CapsLock : KeyAction()
    object Ctrl : KeyAction()
    object Alt : KeyAction()
    object Meta : KeyAction()
    object Fn : KeyAction()
    object Tab : KeyAction()
    object Esc : KeyAction()
    object Space : KeyAction()
    object Left : KeyAction()
    object Right : KeyAction()
    object Up : KeyAction()
    object Down : KeyAction()
    object Insert : KeyAction()
    object DeleteKey : KeyAction()
    object Home : KeyAction()
    object End : KeyAction()
    object PageUp : KeyAction()
    object PageDown : KeyAction()
}

data class KeyData(
    val label: String,
    val shiftLabel: String? = null,
    val action: KeyAction = KeyAction.Text(label),
    val widthWeight: Float = 1f,
    val isModifier: Boolean = false
)
