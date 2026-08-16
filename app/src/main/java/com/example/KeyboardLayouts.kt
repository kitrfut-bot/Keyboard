package com.example

import android.view.KeyEvent

object KeyboardLayouts {
    val pcTopRow = listOf(
        KeyData("Esc", action = KeyAction.Esc, widthWeight = 1.2f, isModifier = true),
        KeyData("F1", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F1)),
        KeyData("F2", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F2)),
        KeyData("F3", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F3)),
        KeyData("F4", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F4)),
        KeyData("F5", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F5)),
        KeyData("F6", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F6)),
        KeyData("F7", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F7)),
        KeyData("F8", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F8)),
        KeyData("F9", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F9)),
        KeyData("F10", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F10)),
        KeyData("F11", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F11)),
        KeyData("F12", action = KeyAction.KeyCode(KeyEvent.KEYCODE_F12))
    )

    val pcNumberRow = listOf(
        KeyData("`", "~"),
        KeyData("1", "!"),
        KeyData("2", "@"),
        KeyData("3", "#"),
        KeyData("4", "$"),
        KeyData("5", "%"),
        KeyData("6", "^"),
        KeyData("7", "&"),
        KeyData("8", "*"),
        KeyData("9", "("),
        KeyData("0", ")"),
        KeyData("-", "_"),
        KeyData("=", "+"),
        KeyData("⌫", action = KeyAction.Backspace, widthWeight = 1.5f, isModifier = true)
    )

    val pcQwertyRow = listOf(
        KeyData("Tab", action = KeyAction.Tab, widthWeight = 1.5f, isModifier = true),
        KeyData("q", "Q"),
        KeyData("w", "W"),
        KeyData("e", "E"),
        KeyData("r", "R"),
        KeyData("t", "T"),
        KeyData("y", "Y"),
        KeyData("u", "U"),
        KeyData("i", "I"),
        KeyData("o", "O"),
        KeyData("p", "P"),
        KeyData("[", "{"),
        KeyData("]", "}"),
        KeyData("\\", "|", widthWeight = 1.5f)
    )

    val pcHomeRow = listOf(
        KeyData("Caps", action = KeyAction.CapsLock, widthWeight = 1.8f, isModifier = true),
        KeyData("a", "A"),
        KeyData("s", "S"),
        KeyData("d", "D"),
        KeyData("f", "F"),
        KeyData("g", "G"),
        KeyData("h", "H"),
        KeyData("j", "J"),
        KeyData("k", "K"),
        KeyData("l", "L"),
        KeyData(";", ":"),
        KeyData("'", "\""),
        KeyData("Enter", action = KeyAction.Enter, widthWeight = 2f, isModifier = true)
    )

    val pcBottomLetterRow = listOf(
        KeyData("Shift", action = KeyAction.Shift, widthWeight = 2.2f, isModifier = true),
        KeyData("z", "Z"),
        KeyData("x", "X"),
        KeyData("c", "C"),
        KeyData("v", "V"),
        KeyData("b", "B"),
        KeyData("n", "N"),
        KeyData("m", "M"),
        KeyData(",", "<"),
        KeyData(".", ">"),
        KeyData("/", "?"),
        KeyData("Shift", action = KeyAction.Shift, widthWeight = 2.2f, isModifier = true)
    )

    val pcControlRow = listOf(
        KeyData("Ctrl", action = KeyAction.Ctrl, widthWeight = 1.5f, isModifier = true),
        KeyData("Fn", action = KeyAction.Fn, widthWeight = 1.2f, isModifier = true),
        KeyData("Win", action = KeyAction.Meta, widthWeight = 1.2f, isModifier = true),
        KeyData("Alt", action = KeyAction.Alt, widthWeight = 1.2f, isModifier = true),
        KeyData(" ", action = KeyAction.Space, widthWeight = 6f),
        KeyData("Alt", action = KeyAction.Alt, widthWeight = 1.2f, isModifier = true),
        KeyData("Ctrl", action = KeyAction.Ctrl, widthWeight = 1.5f, isModifier = true),
        KeyData("←", action = KeyAction.Left, isModifier = true),
        KeyData("↓", action = KeyAction.Down, isModifier = true),
        KeyData("↑", action = KeyAction.Up, isModifier = true),
        KeyData("→", action = KeyAction.Right, isModifier = true)
    )

    val fullLayout = listOf(
        pcTopRow,
        pcNumberRow,
        pcQwertyRow,
        pcHomeRow,
        pcBottomLetterRow,
        pcControlRow
    )
}
