package com.mxw.printer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mxw.printer.model.PaperConsumption
import com.mxw.printer.model.PrintSettings
import com.mxw.printer.ui.theme.*

@Composable
fun PrintDialog(
    onDismiss: () -> Unit,
    onConfirm: (PrintSettings) -> Unit,
    initialSettings: PrintSettings = PrintSettings()
) {
    var settings by remember { mutableStateOf(initialSettings) }
    var showAdvanced by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header - Fixed at top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Yazdırma Ayarları",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Çıktı özelliklerini ayarlayın",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                    }
                }
                
                Divider(color = DividerColor, modifier = Modifier.padding(horizontal = 24.dp))
                
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Paper Size
                    SettingSection("Kağıt Boyutu") {
                        PaperSizeSelector(
                            selected = settings.paperSize,
                            onSelect = { settings = settings.copy(paperSize = it) }
                        )
                    }
                    
                    // Orientation
                    SettingSection("Yönlendirme") {
                        OrientationSelector(
                            selected = settings.orientation,
                            onSelect = { settings = settings.copy(orientation = it) }
                        )
                    }
                    
                    // Heat Level
                    SettingSection("Isı Seviyesi") {
                        HeatLevelSlider(
                            value = settings.heatLevel,
                            onValueChange = { settings = settings.copy(heatLevel = it) }
                        )
                    }
                    
                    // Copies
                    SettingSection("Kopya Sayısı") {
                        CopyCounter(
                            value = settings.copies,
                            onValueChange = { settings = settings.copy(copies = it) }
                        )
                    }
                    
                    // Advanced Settings Toggle
                    TextButton(
                        onClick = { showAdvanced = !showAdvanced },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Gelişmiş Ayarlar")
                    }
                    
                    // Advanced Settings
                    AnimatedVisibility(visible = showAdvanced) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Font Size
                            SettingSection("Yazı Boyutu") {
                                FontSizeSlider(
                                    value = settings.fontSize,
                                    onValueChange = { settings = settings.copy(fontSize = it) }
                                )
                            }
                            
                            // Text Align
                            SettingSection("Hizalama") {
                                TextAlignSelector(
                                    selected = settings.textAlign,
                                    onSelect = { settings = settings.copy(textAlign = it) }
                                )
                            }
                        }
                    }
                    
                    // Paper Consumption
                    PaperConsumptionInfo(settings)
                    
                    Spacer(Modifier.height(8.dp))
                }
                
                // Action Buttons - Fixed at bottom
                Column(
                    modifier = Modifier.padding(24.dp).padding(top = 0.dp)
                ) {
                    Divider(color = DividerColor, modifier = Modifier.padding(bottom = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("İptal")
                        }
                        Button(
                            onClick = { onConfirm(settings) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Yazdır", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
fun PaperConsumptionInfo(settings: PrintSettings) {
    val consumption = remember(settings) {
        val heightPx = settings.paperSize.getHeightPx() ?: 500 // Default for continuous
        val heightMm = settings.paperSize.heightMm ?: 100
        PaperConsumption(heightMm, heightPx, settings.copies)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Tahmini Kağıt Tüketimi",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    "${consumption.getTotalCm()} cm",
                    style = MaterialTheme.typography.titleMedium,
                    color = Accent,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Printing Progress Dialog
@Composable
fun PrintingDialog(
    progress: Float,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Animated Icon
                val infiniteTransition = rememberInfiniteTransition(label = "")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = ""
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Print,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Yazdırılıyor",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "%${(progress * 100).toInt()} tamamlandı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                // Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Primary,
                        trackColor = SurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Başladı",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                        Text(
                            "Tamamlanıyor",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
                
                if (progress < 1f) {
                    TextButton(onClick = onCancel) {
                        Text("İptal", color = Error)
                    }
                }
            }
        }
    }
}
