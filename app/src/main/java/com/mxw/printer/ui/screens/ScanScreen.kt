package com.mxw.printer.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.mxw.printer.model.PrinterViewModelNew
import com.mxw.printer.ui.components.*
import com.mxw.printer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(vm: PrinterViewModelNew, onBack: () -> Unit) {
    val scanResults by vm.scanResults.collectAsState()
    val isScanning by vm.isScanning.collectAsState()

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) vm.startScan()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Yazıcı Tara", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrinterCard {
                Text(
                    "Yakındaki Bluetooth LE cihazları aranacak. Yazıcının açık olduğundan emin olun.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { permLauncher.launch(permissions) },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Taranıyor...")
                    } else {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Taramayı Başlat", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (scanResults.isNotEmpty()) {
                SectionTitle("Bulunan Cihazlar (${scanResults.size})")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(scanResults) { result ->
                        DeviceItem(
                            name = result.device.name ?: "Bilinmeyen",
                            address = result.device.address,
                            rssi = result.rssi,
                            onClick = {
                                vm.connect(result.device.address, result.device.name ?: result.device.address)
                                onBack()
                            }
                        )
                    }
                }
            } else if (!isScanning) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.BluetoothSearching, null, tint = Color(0xFF444466), modifier = Modifier.size(48.dp))
                        Text("Henüz cihaz bulunamadı", color = Color(0xFF666666), style = MaterialTheme.typography.bodyMedium)
                        Text("Yukarıdaki butona basarak tarama başlatın", color = Color(0xFF444444), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(name: String, address: String, rssi: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(if (name.contains("MX", true) || name.contains("print", true))
                            Accent.copy(alpha = 0.2f) else Color(0xFF333355)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Print, null,
                        tint = if (name.contains("MX", true) || name.contains("print", true)) Accent
                        else Color(0xFF666688),
                        modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(name, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(address, style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusBadge("$rssi dBm",
                    if (rssi > -60) Success else if (rssi > -80) Warning else Error)
                Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF555577), modifier = Modifier.size(18.dp))
            }
        }
    }
}
