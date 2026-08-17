package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClipboardPanel(
    onPasteText: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val settings = remember { SettingsManager.getInstance(context) }

    val limit by settings.clipboardLimit.collectAsState()
    val clipboardList by db.clipboardDao().getWithLimit(limit).collectAsState(initial = emptyList())

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp),
        color = Color(0xFF1E1E24),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "📋 Clipboard History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0x334CAF50), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "On-Device Only",
                            fontSize = 10.sp,
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (clipboardList.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                scope.launch { db.clipboardDao().clearUnpinned() }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Clear Unpinned", fontSize = 11.sp, color = Color(0xFFFF8A80))
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFB0B0B0),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (clipboardList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Clipboard is empty",
                            color = Color(0xFF888888),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Copied items will appear here for 1-tap pasting.",
                            color = Color(0xFF666666),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(clipboardList, key = { it.id }) { item ->
                        ClipboardItemRow(
                            item = item,
                            onPaste = {
                                onPasteText(item.text)
                                onClose()
                            },
                            onTogglePin = {
                                scope.launch {
                                    db.clipboardDao().setPinned(item.id, !item.isPinned)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    db.clipboardDao().deleteById(item.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClipboardItemRow(
    item: ClipboardItem,
    onPaste: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(item.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (item.isPinned) Color(0xFF2C2E3E) else Color(0xFF282830))
            .clickable(onClick = onPaste)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.text,
                color = Color(0xFFEEEEEE),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateStr,
                color = Color(0xFF7E7E8E),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Pin Button
        IconButton(
            onClick = onTogglePin,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (item.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (item.isPinned) "Unpin" else "Pin",
                tint = if (item.isPinned) Color(0xFFFFD54F) else Color(0xFF888899),
                modifier = Modifier.size(16.dp)
            )
        }

        // Delete Button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color(0xFF888899),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
