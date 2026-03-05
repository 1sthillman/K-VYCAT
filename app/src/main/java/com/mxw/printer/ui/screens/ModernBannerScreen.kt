package com.mxw.printer.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxw.printer.model.PrinterViewModelNew
import com.mxw.printer.ui.components.*
import com.mxw.printer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernBannerScreen(
    vm: PrinterViewModelNew,
    onBack: () -> Unit
) {
    val bannerText by vm.bannerText.collectAsState()
    val previewBitmap by vm.previewBitmap.collectAsState()
    val settings by vm.printSettings.collectAsState()
    val showPrintDialog by vm.showPrintDialog.collectAsState()
    val showPrintingDialog by vm.showPrintingDialog.collectAsState()
    val printProgress by vm.printProgress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("Pankart Yazdırma", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Surface,
                titleContentColor = TextPrimary
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Pankart Metni",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = bannerText,
                        onValueChange = { vm.setBannerText(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Büyük yazı girin...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    // Letter by letter toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariant)
                            .clickable {
                                vm.updateSettings(
                                    settings.copy(bannerLetterByLetter = !settings.bannerLetterByLetter)
                                )
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Harf-Harf Büyük Çıktı",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Her harf ayrı sayfa",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = settings.bannerLetterByLetter,
                            onCheckedChange = {
                                vm.updateSettings(settings.copy(bannerLetterByLetter = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Primary,
                                checkedTrackColor = Primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    if (settings.bannerLetterByLetter) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Warning, modifier = Modifier.size(20.dp))
                                Text(
                                    "${bannerText.length} harf = ${bannerText.length} sayfa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Warning
                                )
                            }
                        }
                    }
                }
            }

            if (previewBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Önizleme",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { vm.showPrintDialog() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Özelleştir")
                }
                Button(
                    onClick = { vm.showPrintDialog() },
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

    if (showPrintDialog) {
        PrintDialog(
            onDismiss = { vm.hidePrintDialog() },
            onConfirm = { newSettings ->
                vm.updateSettings(newSettings)
                vm.hidePrintDialog()
                vm.printBanner(newSettings)
            },
            initialSettings = settings
        )
    }

    if (showPrintingDialog) {
        PrintingDialog(
            progress = printProgress,
            onCancel = { vm.cancelPrint() }
        )
    }
}
