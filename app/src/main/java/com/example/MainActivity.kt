package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(checkIfImeEnabled(context)) }
    var isDefault by remember { mutableStateOf(checkIfImeDefault(context)) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isEnabled = checkIfImeEnabled(context)
        isDefault = checkIfImeDefault(context)
    }

    val tabs = listOf("Layout", "Appearance", "Sound & Haptic", "Clipboard", "Diagnostics")
    val tabIcons = listOf(
        Icons.Default.Keyboard,
        Icons.Default.Palette,
        Icons.Default.VolumeUp,
        Icons.Default.ContentPaste,
        Icons.Default.BugReport
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("PC Keyboard Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // IME Activation Banner if not default
            if (!isEnabled || !isDefault) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (!isEnabled) "1. Enable Keyboard in System Settings" else "2. Select PC Keyboard as Default",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (!isEnabled) {
                                    try {
                                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                } else {
                                    try {
                                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                        imm?.showInputMethodPicker()
                                    } catch (e: Exception) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (!isEnabled) "Open Keyboard Settings" else "Switch to PC Keyboard")
                        }
                    }
                }
            }

            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp) },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> LayoutSettingsTab()
                    1 -> AppearanceSettingsTab()
                    2 -> SoundHapticSettingsTab()
                    3 -> ClipboardSettingsTab()
                    4 -> DiagnosticsTab()
                }
            }
        }
    }
}

@Composable
fun LayoutSettingsTab() {
    val context = LocalContext.current
    val settings = remember { SettingsManager.getInstance(context) }

    val layoutType by settings.layoutType.collectAsStateWithLifecycle()
    val landscapeLayoutType by settings.landscapeLayoutType.collectAsStateWithLifecycle()
    val keyboardHeight by settings.keyboardHeight.collectAsStateWithLifecycle()
    val keyHeight by settings.keyHeight.collectAsStateWithLifecycle()
    val keySpacing by settings.keySpacing.collectAsStateWithLifecycle()
    val keyboardPosition by settings.keyboardPosition.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.Keyboard, title = "Keyboard Layout")

        // Portrait Layout
        Text("Portrait Layout Type", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        KeyboardLayoutType.values().forEach { type ->
            RadioButtonOption(
                label = type.displayName,
                selected = layoutType == type,
                onClick = { settings.setLayoutType(type) }
            )
        }

        HorizontalDivider()

        // Landscape Layout
        Text("Landscape Layout Type", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        KeyboardLayoutType.values().forEach { type ->
            RadioButtonOption(
                label = type.displayName,
                selected = landscapeLayoutType == type,
                onClick = { settings.setLandscapeLayoutType(type) }
            )
        }

        HorizontalDivider()

        // Height selector
        Text("Keyboard Height Preset", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyboardHeight.values().forEach { height ->
                FilterChip(
                    selected = keyboardHeight == height,
                    onClick = { settings.setKeyboardHeight(height) },
                    label = { Text(height.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Key Height fine slider
        Text("Key Height: ${keyHeight.toInt()} dp", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Slider(
            value = keyHeight,
            onValueChange = { settings.setKeyHeight(it) },
            valueRange = 38f..54f,
            steps = 8
        )

        // Key Spacing fine slider
        Text("Key Spacing: ${"%.1f".format(keySpacing)} dp", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Slider(
            value = keySpacing,
            onValueChange = { settings.setKeySpacing(it) },
            valueRange = 1f..5f,
            steps = 8
        )

        HorizontalDivider()

        // Position selector
        Text("Keyboard Position", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyboardPosition.values().forEach { pos ->
                FilterChip(
                    selected = keyboardPosition == pos,
                    onClick = { settings.setKeyboardPosition(pos) },
                    label = { Text(pos.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AppearanceSettingsTab() {
    val context = LocalContext.current
    val settings = remember { SettingsManager.getInstance(context) }

    val themeType by settings.themeType.collectAsStateWithLifecycle()
    val backgroundType by settings.backgroundType.collectAsStateWithLifecycle()
    val transparency by settings.transparency.collectAsStateWithLifecycle()
    val rgbMode by settings.rgbMode.collectAsStateWithLifecycle()
    val rgbSpeed by settings.rgbSpeed.collectAsStateWithLifecycle()
    val rgbBrightness by settings.rgbBrightness.collectAsStateWithLifecycle()
    val keyPressEffect by settings.keyPressEffect.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.Palette, title = "Theme & Colors")

        // Themes Grid / List
        Text("Built-in Themes", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        KeyboardThemeType.values().forEach { theme ->
            RadioButtonOption(
                label = theme.displayName,
                selected = themeType == theme,
                onClick = { settings.setThemeType(theme) }
            )
        }

        HorizontalDivider()

        // Background Style
        Text("Background Style", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BackgroundType.values().forEach { bg ->
                FilterChip(
                    selected = backgroundType == bg,
                    onClick = { settings.setBackgroundType(bg) },
                    label = { Text(bg.displayName, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Transparency Slider
        Text("Keyboard Transparency: $transparency%", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Slider(
            value = transparency.toFloat(),
            onValueChange = { settings.setTransparency(it.toInt()) },
            valueRange = 0f..80f,
            steps = 16
        )

        HorizontalDivider()

        SectionHeader(icon = Icons.Default.Flare, title = "RGB Lighting")

        // RGB Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RgbMode.values().forEach { mode ->
                FilterChip(
                    selected = rgbMode == mode,
                    onClick = { settings.setRgbMode(mode) },
                    label = { Text(mode.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (rgbMode != RgbMode.OFF) {
            // Speed
            Text("RGB Animation Speed", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RgbSpeed.values().forEach { speed ->
                    FilterChip(
                        selected = rgbSpeed == speed,
                        onClick = { settings.setRgbSpeed(speed) },
                        label = { Text(speed.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Brightness
            Text("RGB Glow Brightness: ${(rgbBrightness * 100).toInt()}%", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Slider(
                value = rgbBrightness,
                onValueChange = { settings.setRgbBrightness(it) },
                valueRange = 0.2f..1.0f,
                steps = 8
            )
        }

        HorizontalDivider()

        SectionHeader(icon = Icons.Default.TouchApp, title = "Key Press Response")

        Text("Key Press Visual Effect", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        KeyPressEffect.values().forEach { effect ->
            RadioButtonOption(
                label = effect.displayName,
                selected = keyPressEffect == effect,
                onClick = { settings.setKeyPressEffect(effect) }
            )
        }
    }
}

@Composable
fun SoundHapticSettingsTab() {
    val context = LocalContext.current
    val settings = remember { SettingsManager.getInstance(context) }
    val soundManager = remember { SoundManager.getInstance(context) }
    val hapticManager = remember { HapticManager.getInstance(context) }

    val isSoundEnabled by settings.isSoundEnabled.collectAsStateWithLifecycle()
    val soundStyle by settings.soundStyle.collectAsStateWithLifecycle()
    val soundVolume by settings.soundVolume.collectAsStateWithLifecycle()
    val isHapticEnabled by settings.isHapticEnabled.collectAsStateWithLifecycle()
    val hapticStrength by settings.hapticStrength.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.VolumeUp, title = "Key Sounds")

        SwitchOption(
            title = "Key Click Sounds",
            subtitle = "Zero-latency audio synthesis on key presses",
            checked = isSoundEnabled,
            onCheckedChange = { settings.setSoundEnabled(it) }
        )

        if (isSoundEnabled) {
            Text("Sound Style", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            SoundStyle.values().forEach { style ->
                RadioButtonOption(
                    label = style.displayName,
                    selected = soundStyle == style,
                    onClick = {
                        settings.setSoundStyle(style)
                        soundManager.playKeySound(KeyAction.Text("a"))
                    }
                )
            }

            Text("Sound Volume: ${(soundVolume * 100).toInt()}%", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Slider(
                value = soundVolume,
                onValueChange = { settings.setSoundVolume(it) },
                valueRange = 0.0f..1.0f,
                steps = 10
            )
        }

        HorizontalDivider()

        SectionHeader(icon = Icons.Default.Vibration, title = "Haptic Feedback")

        SwitchOption(
            title = "Haptic Vibration",
            subtitle = "Tactile response on every key press",
            checked = isHapticEnabled,
            onCheckedChange = { settings.setHapticEnabled(it) }
        )

        if (isHapticEnabled) {
            Text("Vibration Strength", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HapticStrength.values().forEach { str ->
                    FilterChip(
                        selected = hapticStrength == str,
                        onClick = {
                            settings.setHapticStrength(str)
                            hapticManager.performHaptic()
                        },
                        label = { Text(str.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Preview Button
        OutlinedButton(
            onClick = {
                hapticManager.performHaptic()
                soundManager.playKeySound(KeyAction.Space)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Sound & Haptic Response")
        }
    }
}

@Composable
fun ClipboardSettingsTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val settings = remember { SettingsManager.getInstance(context) }

    val isClipboardEnabled by settings.isClipboardEnabled.collectAsStateWithLifecycle()
    val clipboardLimit by settings.clipboardLimit.collectAsStateWithLifecycle()

    val clipboardItems by db.clipboardDao().getAll().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.ContentPaste, title = "Clipboard Manager")

        SwitchOption(
            title = "Clipboard History",
            subtitle = "Quickly paste recent snippets from keyboard toolbar",
            checked = isClipboardEnabled,
            onCheckedChange = { settings.setClipboardEnabled(it) }
        )

        Text("History Limit", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(20, 35, 50).forEach { limit ->
                FilterChip(
                    selected = clipboardLimit == limit,
                    onClick = { settings.setClipboardLimit(limit) },
                    label = { Text("$limit items") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Privacy Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Strict Privacy Guarantee", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "Clipboard data stays 100% on your device and is NEVER sent to any server or cloud.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Saved Items (${clipboardItems.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (clipboardItems.isNotEmpty()) {
                TextButton(
                    onClick = {
                        scope.launch { db.clipboardDao().clearAll() }
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (clipboardItems.isEmpty()) {
            Text(
                "No saved clipboard items.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            clipboardItems.take(10).forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.text,
                            fontSize = 13.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                scope.launch { db.clipboardDao().deleteById(item.id) }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticsTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val crashLogs by db.crashLogDao().getAllCrashLogs().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(icon = Icons.Default.BugReport, title = "Crash Logs (${crashLogs.size})")

            if (crashLogs.isNotEmpty()) {
                Button(
                    onClick = {
                        scope.launch { db.crashLogDao().clearCrashLogs() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Logs")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (crashLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No crashes recorded",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "The keyboard service is running smoothly.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(crashLogs, key = { it.id }) { log ->
                    CrashLogCard(log)
                }
            }
        }
    }
}

@Composable
fun CrashLogCard(log: CrashLog) {
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${log.throwableName}: ${log.message}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Time: $dateStr | Thread: ${log.threadName}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = log.stackTrace.take(600) + if (log.stackTrace.length > 600) "..." else "",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun RadioButtonOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SwitchOption(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

fun checkIfImeEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
    val enabledMethods = imm.enabledInputMethodList
    return enabledMethods.any { it.packageName == context.packageName }
}

fun checkIfImeDefault(context: Context): Boolean {
    val currentIme = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD
    )
    return currentIme?.contains(context.packageName) == true
}
