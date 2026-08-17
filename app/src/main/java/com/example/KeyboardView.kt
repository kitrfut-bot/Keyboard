package com.example

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun KeyboardView(inputMethodService: PcKeyboardService) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val engine = remember { KeyboardEngine { inputMethodService } }
    val settings = remember { SettingsManager.getInstance(context) }
    val soundManager = remember { SoundManager.getInstance(context) }
    val hapticManager = remember { HapticManager.getInstance(context) }

    // State flows
    val isShifted by engine.isShifted.collectAsStateWithLifecycle()
    val isCapsLock by engine.isCapsLock.collectAsStateWithLifecycle()
    val isCtrl by engine.isCtrlActive.collectAsStateWithLifecycle()
    val isAlt by engine.isAltActive.collectAsStateWithLifecycle()
    val isMeta by engine.isMetaActive.collectAsStateWithLifecycle()
    val isFn by engine.isFnActive.collectAsStateWithLifecycle()

    // Settings flows
    val portraitLayout by settings.layoutType.collectAsStateWithLifecycle()
    val landscapeLayout by settings.landscapeLayoutType.collectAsStateWithLifecycle()
    val activeLayoutType = if (isLandscape) landscapeLayout else portraitLayout

    val heightSetting by settings.keyboardHeight.collectAsStateWithLifecycle()
    val keyHeightVal by settings.keyHeight.collectAsStateWithLifecycle()
    val keySpacingVal by settings.keySpacing.collectAsStateWithLifecycle()
    val positionSetting by settings.keyboardPosition.collectAsStateWithLifecycle()
    val themeType by settings.themeType.collectAsStateWithLifecycle()
    val backgroundType by settings.backgroundType.collectAsStateWithLifecycle()
    val transparencyVal by settings.transparency.collectAsStateWithLifecycle()
    val rgbMode by settings.rgbMode.collectAsStateWithLifecycle()
    val rgbSpeed by settings.rgbSpeed.collectAsStateWithLifecycle()
    val rgbBrightness by settings.rgbBrightness.collectAsStateWithLifecycle()
    val keyPressEffect by settings.keyPressEffect.collectAsStateWithLifecycle()

    var showClipboardPanel by remember { mutableStateOf(false) }

    // Animated RGB rainbow brush
    val infiniteTransition = rememberInfiniteTransition(label = "rgb_rainbow")
    val rgbOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (rgbMode == RgbMode.ANIMATED) rgbSpeed.durationMs else 10000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rgb_sweep"
    )

    val currentRows = remember(activeLayoutType) {
        KeyboardLayouts.getLayout(activeLayoutType)
    }

    // Effective key height based on orientation & height setting
    val heightScale = if (isLandscape) 0.85f * heightSetting.scaleFactor else heightSetting.scaleFactor
    val effectiveKeyHeight = (keyHeightVal * heightScale).dp
    val effectiveKeySpacing = keySpacingVal.dp

    // Theme Color Palette
    val themeColors = remember(themeType, backgroundType, transparencyVal, rgbBrightness, rgbMode, rgbOffset) {
        calculateThemeColors(
            themeType = themeType,
            backgroundType = backgroundType,
            transparency = transparencyVal,
            rgbMode = rgbMode,
            rgbBrightness = rgbBrightness,
            rgbOffset = rgbOffset
        )
    }

    val bottomPadding = if (positionSetting == KeyboardPosition.FLOATING) 16.dp else 0.dp
    val horizontalPadding = if (positionSetting == KeyboardPosition.FLOATING) 8.dp else 2.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding, start = horizontalPadding, end = horizontalPadding),
        color = themeColors.surfaceColor,
        shape = if (positionSetting == KeyboardPosition.FLOATING) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.backgroundBrush ?: Brush.linearGradient(listOf(themeColors.surfaceColor, themeColors.surfaceColor)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(effectiveKeySpacing)
            ) {
                // Toolbar
                KeyboardToolbar(
                    themeColors = themeColors,
                    onToggleClipboard = { showClipboardPanel = !showClipboardPanel },
                    onAction = { actionName ->
                        hapticManager.performHaptic()
                        soundManager.playKeySound(KeyAction.Esc)
                        engine.executeQuickAction(actionName)
                    },
                    onSwitchLayout = {
                        val all = KeyboardLayoutType.values()
                        val nextIdx = (activeLayoutType.ordinal + 1) % all.size
                        if (isLandscape) {
                            settings.setLandscapeLayoutType(all[nextIdx])
                        } else {
                            settings.setLayoutType(all[nextIdx])
                        }
                    },
                    onOpenSettings = {
                        try {
                            val intent = Intent(context, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    }
                )

                // Clipboard Overlay Panel if active
                AnimatedVisibility(
                    visible = showClipboardPanel,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ClipboardPanel(
                        onPasteText = { text ->
                            hapticManager.performHaptic()
                            soundManager.playKeySound(KeyAction.Enter)
                            engine.pasteText(text)
                        },
                        onClose = { showClipboardPanel = false }
                    )
                }

                // Keyboard Rows
                currentRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(effectiveKeySpacing)
                    ) {
                        row.forEach { keyData ->
                            KeyButton(
                                keyData = keyData,
                                isShifted = isShifted,
                                isCapsLock = isCapsLock,
                                isCtrl = isCtrl,
                                isAlt = isAlt,
                                isMeta = isMeta,
                                isFn = isFn,
                                themeColors = themeColors,
                                keyHeight = effectiveKeyHeight,
                                keyPressEffect = keyPressEffect,
                                modifier = Modifier.weight(keyData.widthWeight),
                                onClick = {
                                    hapticManager.performHaptic(keyData.action)
                                    soundManager.playKeySound(keyData.action)
                                    engine.onKeyPress(keyData.action)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyboardToolbar(
    themeColors: KeyboardThemeColors,
    onToggleClipboard: () -> Unit,
    onAction: (String) -> Unit,
    onSwitchLayout: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(themeColors.toolbarBackground)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarIconButton(
            icon = Icons.Default.ContentPaste,
            label = "Clipboard",
            tint = themeColors.accentColor,
            onClick = onToggleClipboard
        )

        ToolbarDivider(themeColors.borderColor)

        ToolbarIconButton(icon = Icons.Default.Undo, label = "Undo", tint = themeColors.textColor) { onAction("undo") }
        ToolbarIconButton(icon = Icons.Default.Redo, label = "Redo", tint = themeColors.textColor) { onAction("redo") }

        ToolbarDivider(themeColors.borderColor)

        ToolbarIconButton(icon = Icons.Default.ContentCopy, label = "Copy", tint = themeColors.textColor) { onAction("copy") }
        ToolbarIconButton(icon = Icons.Default.ContentCut, label = "Cut", tint = themeColors.textColor) { onAction("cut") }
        ToolbarIconButton(icon = Icons.Default.SelectAll, label = "Select All", tint = themeColors.textColor) { onAction("selectAll") }

        ToolbarDivider(themeColors.borderColor)

        ToolbarIconButton(icon = Icons.Default.Keyboard, label = "Layout", tint = themeColors.textColor, onClick = onSwitchLayout)
        ToolbarIconButton(icon = Icons.Default.Settings, label = "Settings", tint = themeColors.textColor, onClick = onOpenSettings)
    }
}

@Composable
fun ToolbarIconButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ToolbarDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(18.dp)
            .background(color.copy(alpha = 0.4f))
    )
}

@Composable
fun KeyButton(
    keyData: KeyData,
    isShifted: Boolean,
    isCapsLock: Boolean,
    isCtrl: Boolean,
    isAlt: Boolean,
    isMeta: Boolean,
    isFn: Boolean,
    themeColors: KeyboardThemeColors,
    keyHeight: Dp,
    keyPressEffect: KeyPressEffect,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val displayLabel = if ((isShifted || isCapsLock) && keyData.shiftLabel != null) {
        keyData.shiftLabel
    } else if ((isShifted || isCapsLock) && keyData.shiftLabel == null && keyData.action is KeyAction.Text) {
        keyData.label.uppercase()
    } else {
        keyData.label
    }

    val isActiveModifier = (keyData.action == KeyAction.Shift && isShifted) ||
            (keyData.action == KeyAction.CapsLock && isCapsLock) ||
            (keyData.action == KeyAction.Ctrl && isCtrl) ||
            (keyData.action == KeyAction.Alt && isAlt) ||
            (keyData.action == KeyAction.Meta && isMeta) ||
            (keyData.action == KeyAction.Fn && isFn)

    // Key Color resolution
    val keyBg = when {
        isActiveModifier -> themeColors.activeModifierColor
        isPressed -> themeColors.keyPressedColor
        keyData.isModifier -> themeColors.modifierKeyColor
        else -> themeColors.keyColor
    }

    val keyTextColor = when {
        isActiveModifier -> themeColors.activeModifierTextColor
        isPressed -> themeColors.keyPressedTextColor
        else -> themeColors.textColor
    }

    // Key Press Animation Effect
    val scale = if (keyPressEffect == KeyPressEffect.OFF) {
        1.0f
    } else {
        val targetScale = if (isPressed) {
            if (keyPressEffect == KeyPressEffect.STRONG) 0.88f else 0.94f
        } else {
            1.0f
        }
        val animDuration = if (keyPressEffect == KeyPressEffect.STRONG) 90 else 60
        animateFloatAsState(
            targetValue = targetScale,
            animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
            label = "key_scale"
        ).value
    }

    Box(
        modifier = modifier
            .height(keyHeight)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .then(
                if (themeColors.borderBrush != null) {
                    Modifier.border(1.dp, themeColors.borderBrush, RoundedCornerShape(6.dp))
                } else if (themeColors.borderColor.alpha > 0.05f) {
                    Modifier.border(1.dp, themeColors.borderColor, RoundedCornerShape(6.dp))
                } else {
                    Modifier
                }
            )
            .background(keyBg)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = themeColors.accentColor),
                onClick = onClick
            ),
    ) {
        // Shift label badge in top right corner for dual-char keys
        if (!keyData.isModifier && keyData.shiftLabel != null) {
            Text(
                text = keyData.shiftLabel,
                color = keyTextColor.copy(alpha = 0.55f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 3.dp, top = 2.dp)
            )
        }

        // Main Key Label
        Text(
            text = displayLabel,
            color = keyTextColor,
            fontSize = if (keyData.isModifier || displayLabel.length > 2) 11.sp else 15.sp,
            fontWeight = if (keyData.isModifier || isActiveModifier) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

data class KeyboardThemeColors(
    val surfaceColor: Color,
    val backgroundBrush: Brush?,
    val keyColor: Color,
    val modifierKeyColor: Color,
    val activeModifierColor: Color,
    val activeModifierTextColor: Color,
    val keyPressedColor: Color,
    val keyPressedTextColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val toolbarBackground: Color,
    val borderColor: Color,
    val borderBrush: Brush?
)

fun calculateThemeColors(
    themeType: KeyboardThemeType,
    backgroundType: BackgroundType,
    transparency: Int,
    rgbMode: RgbMode,
    rgbBrightness: Float,
    rgbOffset: Float
): KeyboardThemeColors {
    val alphaFactor = (100 - transparency.coerceIn(0, 80)) / 100f

    // Dynamic rainbow brush for RGB themes
    val rainbowColors = listOf(
        Color(0xFFFF1744).copy(alpha = rgbBrightness),
        Color(0xFFFF9100).copy(alpha = rgbBrightness),
        Color(0xFFFFEA00).copy(alpha = rgbBrightness),
        Color(0xFF00E676).copy(alpha = rgbBrightness),
        Color(0xFF00B0FF).copy(alpha = rgbBrightness),
        Color(0xFF651FFF).copy(alpha = rgbBrightness),
        Color(0xFFFF1744).copy(alpha = rgbBrightness)
    )

    val rgbBrush = when (rgbMode) {
        RgbMode.ANIMATED -> Brush.sweepGradient(rainbowColors)
        RgbMode.STATIC -> Brush.horizontalGradient(rainbowColors)
        RgbMode.OFF -> null
    }

    return when (themeType) {
        KeyboardThemeType.CLASSIC_BLACK -> {
            val bg = Color(0xFF18181A).copy(alpha = alphaFactor)
            KeyboardThemeColors(
                surfaceColor = bg,
                backgroundBrush = if (backgroundType == BackgroundType.GRADIENT) Brush.verticalGradient(listOf(Color(0xFF222226), Color(0xFF141416))) else null,
                keyColor = Color(0xFF2C2D32),
                modifierKeyColor = Color(0xFF212226),
                activeModifierColor = Color(0xFF007ACC),
                activeModifierTextColor = Color.White,
                keyPressedColor = Color(0xFF3F414A),
                keyPressedTextColor = Color.White,
                textColor = Color(0xFFEDEDED),
                accentColor = Color(0xFF29B6F6),
                toolbarBackground = Color(0xFF202124),
                borderColor = Color(0xFF3A3B40),
                borderBrush = if (rgbMode != RgbMode.OFF) rgbBrush else null
            )
        }
        KeyboardThemeType.CLASSIC_WHITE -> {
            val bg = Color(0xFFEBEBF0).copy(alpha = alphaFactor)
            KeyboardThemeColors(
                surfaceColor = bg,
                backgroundBrush = if (backgroundType == BackgroundType.GRADIENT) Brush.verticalGradient(listOf(Color(0xFFF5F5FA), Color(0xFFE0E0E6))) else null,
                keyColor = Color(0xFFFFFFFF),
                modifierKeyColor = Color(0xFFD6D7DC),
                activeModifierColor = Color(0xFF1976D2),
                activeModifierTextColor = Color.White,
                keyPressedColor = Color(0xFFECEFF1),
                keyPressedTextColor = Color(0xFF1976D2),
                textColor = Color(0xFF1A1A1E),
                accentColor = Color(0xFF1976D2),
                toolbarBackground = Color(0xFFDFE0E6),
                borderColor = Color(0xFFC4C5CC),
                borderBrush = if (rgbMode != RgbMode.OFF) rgbBrush else null
            )
        }
        KeyboardThemeType.GLASSMORPHISM -> {
            // High-grade frosted acrylic / translucent glass
            val bg = Color(0xFF0F101A).copy(alpha = 0.75f * alphaFactor)
            KeyboardThemeColors(
                surfaceColor = bg,
                backgroundBrush = Brush.verticalGradient(
                    listOf(
                        Color(0x9924283B),
                        Color(0xCC0E101B)
                    )
                ),
                keyColor = Color(0x35FFFFFF),
                modifierKeyColor = Color(0x22FFFFFF),
                activeModifierColor = Color(0x88007ACC),
                activeModifierTextColor = Color.White,
                keyPressedColor = Color(0x66FFFFFF),
                keyPressedTextColor = Color.White,
                textColor = Color(0xFFFFFFFF),
                accentColor = Color(0xFF4FC3F7),
                toolbarBackground = Color(0x30FFFFFF),
                borderColor = Color(0x55FFFFFF),
                borderBrush = if (rgbMode != RgbMode.OFF) rgbBrush else Brush.linearGradient(listOf(Color(0x70FFFFFF), Color(0x20FFFFFF)))
            )
        }
        KeyboardThemeType.RGB_BLACK -> {
            val bg = Color(0xFF0E0E12).copy(alpha = alphaFactor)
            KeyboardThemeColors(
                surfaceColor = bg,
                backgroundBrush = if (backgroundType == BackgroundType.GRADIENT) Brush.verticalGradient(listOf(Color(0xFF1A1A22), Color(0xFF0B0B0E))) else null,
                keyColor = Color(0xFF1E1E24),
                modifierKeyColor = Color(0xFF16161B),
                activeModifierColor = Color(0xFF6200EA),
                activeModifierTextColor = Color.White,
                keyPressedColor = Color(0xFF32323D),
                keyPressedTextColor = Color(0xFF00E5FF),
                textColor = Color(0xFFF0F0F5),
                accentColor = Color(0xFF00E5FF),
                toolbarBackground = Color(0xFF16161B),
                borderColor = Color(0xFF00E5FF).copy(alpha = rgbBrightness),
                borderBrush = rgbBrush ?: Brush.horizontalGradient(rainbowColors)
            )
        }
        KeyboardThemeType.RGB_WHITE -> {
            val bg = Color(0xFFF4F5FB).copy(alpha = alphaFactor)
            KeyboardThemeColors(
                surfaceColor = bg,
                backgroundBrush = null,
                keyColor = Color(0xFFFFFFFF),
                modifierKeyColor = Color(0xFFE2E4EC),
                activeModifierColor = Color(0xFF6200EA),
                activeModifierTextColor = Color.White,
                keyPressedColor = Color(0xFFF0F2FF),
                keyPressedTextColor = Color(0xFF6200EA),
                textColor = Color(0xFF141418),
                accentColor = Color(0xFF6200EA),
                toolbarBackground = Color(0xFFE8E9F2),
                borderColor = Color(0xFF6200EA).copy(alpha = rgbBrightness),
                borderBrush = rgbBrush ?: Brush.horizontalGradient(rainbowColors)
            )
        }
        KeyboardThemeType.AMOLED_BLACK -> {
            val bg = Color(0xFF000000)
            KeyboardThemeColors(
                surfaceColor = bg,
                backgroundBrush = null,
                keyColor = Color(0xFF111111),
                modifierKeyColor = Color(0xFF080808),
                activeModifierColor = Color(0xFF00E676),
                activeModifierTextColor = Color.Black,
                keyPressedColor = Color(0xFF262626),
                keyPressedTextColor = Color.White,
                textColor = Color(0xFFFFFFFF),
                accentColor = Color(0xFF00E676),
                toolbarBackground = Color(0xFF0A0A0A),
                borderColor = Color(0xFF282828),
                borderBrush = if (rgbMode != RgbMode.OFF) rgbBrush else null
            )
        }
        KeyboardThemeType.MINIMAL -> {
            val bg = Color(0xFF1A1A1E).copy(alpha = alphaFactor)
            KeyboardThemeColors(
                surfaceColor = bg,
                backgroundBrush = null,
                keyColor = Color(0x18FFFFFF),
                modifierKeyColor = Color(0x0CFFFFFF),
                activeModifierColor = Color(0xFF3D5AFE),
                activeModifierTextColor = Color.White,
                keyPressedColor = Color(0x35FFFFFF),
                keyPressedTextColor = Color.White,
                textColor = Color(0xFFE0E0E6),
                accentColor = Color(0xFF3D5AFE),
                toolbarBackground = Color(0x15FFFFFF),
                borderColor = Color(0x22FFFFFF),
                borderBrush = if (rgbMode != RgbMode.OFF) rgbBrush else null
            )
        }
    }
}
