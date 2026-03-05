package com.mxw.printer.ui.screens

// ============================================================
// OLD TEXT PRINT SCREEN - DISABLED
// Use ModernTextScreen.kt instead
// ============================================================

/*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.mxw.printer.ble.BleManager
import com.mxw.printer.ble.PrinterProtocol
import com.mxw.printer.model.PrinterViewModelNew
import com.mxw.printer.ui.components.*
import com.mxw.printer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextPrintScreen(vm: PrinterViewModelNew, onBack: () -> Unit) {
    val textContent by vm.textContent.collectAsState()
    val fontSize by vm.fontSize.collectAsState()
    val isBold by vm.isBold.collectAsState()
    val textAlign by vm.textAlign.collectAsState()
    val previewBitmap by vm.previewBitmap.collectAsState()
    val connectionState by vm.connectionState.collectAsState()
    val isConnected = connectionState == BleManager.ConnectionState.CONNECTED

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Metin Yazdır", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background, titleContentColor = Color.White, navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview
            PrinterCard {
                SectionTitle("Önizleme")
                PrintPreview(previewBitmap, modifier = Modifier.heightIn(min = 60.dp, max = 200.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    "384px × ${previewBitmap?.height ?: 0}px",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666)
                )
            }

            // Text Input
            PrinterCard {
                SectionTitle("Metin")
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { vm.setTextContent(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Yazdırılacak metin...", color = Color(0xFF666666)) },
                    minLines = 2,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Accent,
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Yeni satır için Enter'a basın",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF555555)
                )
            }

            // Font Settings
            PrinterCard {
                SectionTitle("Yazı Ayarları")

                // Bold toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kalın Yazı", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Switch(
                        checked = isBold,
                        onCheckedChange = { vm.setBold(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Accent)
                    )
                }

                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                // Font size
                LabeledSlider(
                    label = "Font Boyutu",
                    value = fontSize,
                    onValueChange = { vm.setFontSize(it) },
                    valueRange = 0f..150f,
                    valueLabel = if (fontSize == 0f) "Otomatik" else "${fontSize.toInt()}px"
                )

                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                // Alignment
                Text("Hizalama", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                Spacer(Modifier.height(8.dp))
                SegmentedControl(
                    options = listOf("Sol", "Orta", "Sag"),
                    selected = when (textAlign) {
                        com.mxw.printer.model.TextAlign.LEFT   -> 0
                        com.mxw.printer.model.TextAlign.CENTER -> 1
                        com.mxw.printer.model.TextAlign.RIGHT  -> 2
                    },
                    onSelect = {
                        vm.setTextAlign(when (it) {
                            0 -> com.mxw.printer.model.TextAlign.LEFT
                            1 -> com.mxw.printer.model.TextAlign.CENTER
                            else -> com.mxw.printer.model.TextAlign.RIGHT
                        })
                    }
                )
            }

            // Quick text templates
            PrinterCard {
                SectionTitle("Hazır Şablonlar")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Merhaba!", "DUYURU", "SATILIK", "ACIL", "Tarih: 28.02.2026").forEach { template ->
                        FilterChip(
                            selected = textContent == template,
                            onClick = { vm.setTextContent(template) },
                            label = { Text(template, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Accent.copy(alpha = 0.2f),
                                selectedLabelColor = Accent,
                                containerColor = Background,
                                labelColor = Color(0xFF888888)
                            )
                        )
                    }
                }
            }

            // Divider & Spacer quick actions
            PrinterCard {
                SectionTitle("Hızlı Ekle")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlineButton("--- Cizgi ---", onClick = { vm.printDivider(PrinterProtocol.DividerStyle.SOLID) }, modifier = Modifier.weight(1f))
                    OutlineButton("Bosluk", onClick = { vm.printSpacer(3) }, modifier = Modifier.weight(1f))
                }
            }

            // Print button
            PrintButton(
                text = if (isConnected) "Yazdir" else "Yazici Bagli Degil",
                onClick = { vm.printText() },
                enabled = isConnected
            )
        }
    }
}

}
*/
