package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun KeyboardView(inputMethodService: PcKeyboardService) {
    val engine = remember { KeyboardEngine { inputMethodService } }
    
    val isShifted by engine.isShifted.collectAsStateWithLifecycle()
    val isCtrl by engine.isCtrlActive.collectAsStateWithLifecycle()
    val isAlt by engine.isAltActive.collectAsStateWithLifecycle()
    val isMeta by engine.isMetaActive.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    Surface(
        color = Color(0xFF1E1E1E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Text Editing Toolbar
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp).background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarButton("Copy") { engine.onKeyPress(KeyAction.KeyCode(android.view.KeyEvent.KEYCODE_COPY)) }
                ToolbarButton("Cut") { engine.onKeyPress(KeyAction.KeyCode(android.view.KeyEvent.KEYCODE_CUT)) }
                ToolbarButton("Paste") { engine.onKeyPress(KeyAction.KeyCode(android.view.KeyEvent.KEYCODE_PASTE)) }
                ToolbarButton("Undo") { engine.onKeyPress(KeyAction.KeyCode(android.view.KeyEvent.KEYCODE_Z)) /* simplified */ }
            }

            KeyboardLayouts.fullLayout.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    row.forEach { keyData ->
                        KeyButton(
                            keyData = keyData,
                            isShifted = isShifted,
                            isCtrl = isCtrl,
                            isAlt = isAlt,
                            isMeta = isMeta,
                            modifier = Modifier.weight(keyData.widthWeight),
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                engine.onKeyPress(keyData.action) 
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color(0xFFE0E0E0), fontSize = 14.sp)
    }
}

@Composable
fun KeyButton(
    keyData: KeyData,
    isShifted: Boolean,
    isCtrl: Boolean,
    isAlt: Boolean,
    isMeta: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val displayLabel = if (isShifted && keyData.shiftLabel != null) {
        keyData.shiftLabel
    } else if (isShifted && keyData.shiftLabel == null && keyData.action is KeyAction.Text) {
        keyData.label.uppercase()
    } else {
        keyData.label
    }

    var isActiveModifier = false
    if (keyData.action == KeyAction.Shift && isShifted) isActiveModifier = true
    if (keyData.action == KeyAction.Ctrl && isCtrl) isActiveModifier = true
    if (keyData.action == KeyAction.Alt && isAlt) isActiveModifier = true
    if (keyData.action == KeyAction.Meta && isMeta) isActiveModifier = true

    val backgroundColor = if (isActiveModifier) {
        Color(0xFF007ACC) // Accent color
    } else if (keyData.isModifier) {
        Color(0xFF2D2D2D) // Darker modifier key
    } else {
        Color(0xFF3E3E3E) // Normal key
    }

    val textColor = if (isActiveModifier) Color.White else Color(0xFFE0E0E0)

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(
                onClick = onClick
            ),
    ) {
        if (!keyData.isModifier && keyData.shiftLabel != null) {
            Text(
                text = keyData.shiftLabel,
                color = textColor.copy(alpha = 0.5f),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 4.dp, top = 2.dp)
            )
        }
        
        Text(
            text = displayLabel,
            color = textColor,
            fontSize = if (keyData.isModifier) 12.sp else 16.sp,
            fontWeight = if (keyData.isModifier) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
