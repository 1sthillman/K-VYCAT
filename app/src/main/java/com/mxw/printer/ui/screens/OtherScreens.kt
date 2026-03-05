package com.mxw.printer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.mxw.printer.model.PrinterViewModelNew
import com.mxw.printer.ui.theme.*

// ============================================================
// LOG EKRANI
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(vm: PrinterViewModelNew, onBack: () -> Unit) {
    val logs by vm.logMessages.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Sistem Günlüğü", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Henüz log yok", color = Color(0xFF666666))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs.size) { i ->
                    val log = logs[logs.size - 1 - i]
                    Text(
                        log,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        color = when {
                            log.contains("HATA", true) || log.contains("ERROR", true) -> Error
                            log.contains("Bağlandı") || log.contains("hazır")         -> Success
                            log.contains("Bağlanıyor") || log.contains("Taranıyor")   -> Warning
                            else                                                        -> Color(0xFF888888)
                        }
                    )
                }
            }
        }
    }
}
