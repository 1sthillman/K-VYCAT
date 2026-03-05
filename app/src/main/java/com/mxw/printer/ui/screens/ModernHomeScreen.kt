package com.mxw.printer.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxw.printer.ble.BleManager
import com.mxw.printer.model.PrinterViewModelNew
import com.mxw.printer.ui.theme.*

@Composable
fun ModernHomeScreen(
    vm: PrinterViewModelNew,
    onNavigate: (String) -> Unit
) {
    val connectionState by vm.connectionState.collectAsState()
    val connectedDevice by vm.connectedDevice.collectAsState()
    val lastDeviceInfo by vm.lastDeviceInfo.collectAsState()

    LaunchedEffect(Unit) {
        if (connectionState == BleManager.ConnectionState.DISCONNECTED && lastDeviceInfo != null) {
            kotlinx.coroutines.delay(1000)
            vm.autoReconnect()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "MXW Printer",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Profesyonel Termal Yazdırma",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            IconButton(
                onClick = { onNavigate("settings") },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = TextSecondary)
            }
        }

        // Connection Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when (connectionState) {
                                    BleManager.ConnectionState.CONNECTED -> Success.copy(alpha = 0.15f)
                                    BleManager.ConnectionState.CONNECTING,
                                    BleManager.ConnectionState.DISCOVERING -> Warning.copy(alpha = 0.15f)
                                    else -> Color(0xFF374151).copy(alpha = 0.5f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            tint = when (connectionState) {
                                BleManager.ConnectionState.CONNECTED -> Success
                                BleManager.ConnectionState.CONNECTING,
                                BleManager.ConnectionState.DISCOVERING -> Warning
                                else -> Color(0xFF6B7280)
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            when (connectionState) {
                                BleManager.ConnectionState.CONNECTED -> connectedDevice ?: "Yazıcı"
                                BleManager.ConnectionState.CONNECTING -> "Bağlanıyor..."
                                BleManager.ConnectionState.DISCOVERING -> "Keşfediliyor..."
                                else -> "Bağlı Değil"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (connectionState == BleManager.ConnectionState.DISCONNECTED && lastDeviceInfo != null) {
                            Text(
                                "Son: ${lastDeviceInfo!!.second}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        } else {
                            Text(
                                when (connectionState) {
                                    BleManager.ConnectionState.CONNECTED -> "Hazır"
                                    else -> "Bağlantı bekleniyor"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                if (connectionState == BleManager.ConnectionState.CONNECTED) {
                    OutlinedButton(
                        onClick = { vm.disconnect() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                        border = BorderStroke(1.dp, Error)
                    ) {
                        Text("Kes", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (lastDeviceInfo != null) {
                            IconButton(
                                onClick = { vm.autoReconnect() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Success.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Success, modifier = Modifier.size(20.dp))
                            }
                        }
                        Button(
                            onClick = { onNavigate("scan") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Bağlan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Print Modes Grid
        Text(
            "Yazdırma Modları",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernNavCard(
                icon = Icons.Default.TextFields,
                title = "Metin",
                subtitle = "Yazı yazdır",
                gradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("text") }
            )
            ModernNavCard(
                icon = Icons.Default.Campaign,
                title = "Pankart",
                subtitle = "Büyük afiş",
                gradient = Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E))),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("banner") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernNavCard(
                icon = Icons.Default.Image,
                title = "Görsel",
                subtitle = "Fotoğraf yazdır",
                gradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF06B6D4))),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("image") }
            )
            ModernNavCard(
                icon = Icons.Default.FormatListBulleted,
                title = "Sıra",
                subtitle = "Çoklu yazdırma",
                gradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFF97316))),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("queue") }
            )
        }

        // Quick Actions
        Text(
            "Hızlı Erişim",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.BugReport,
                label = "Günlük",
                onClick = { onNavigate("log") },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.Settings,
                label = "Ayarlar",
                onClick = { onNavigate("settings") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ModernNavCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
