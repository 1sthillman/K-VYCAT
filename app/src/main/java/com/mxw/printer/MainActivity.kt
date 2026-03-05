package com.mxw.printer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mxw.printer.model.PrinterViewModelNew
import com.mxw.printer.ui.screens.*
import com.mxw.printer.ui.theme.Background
import com.mxw.printer.ui.theme.MXWPrinterTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            // User denied permissions - show message
            android.widget.Toast.makeText(
                this,
                "Bluetooth ve konum izinleri gereklidir",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request permissions
        requestPermissions()
        
        setContent {
            MXWPrinterTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Background) {
                    MXWPrinterApp()
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        // BLE permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Android 11 and below
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Storage permissions for images
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // Filter out already granted permissions
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}

@Composable
fun MXWPrinterApp() {
    val navController = rememberNavController()
    val vm: PrinterViewModelNew = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home")     { ModernHomeScreen(vm, onNavigate = { navController.navigate(it) }) }
        composable("scan")     { ScanScreen(vm, onBack = { navController.popBackStack() }) }
        composable("text")     { ModernTextScreen(vm, onBack = { navController.popBackStack() }) }
        composable("banner")   { ModernBannerScreen(vm, onBack = { navController.popBackStack() }) }
        composable("image")    { ModernImageScreen(vm, onBack = { navController.popBackStack() }) }
        composable("log")      { LogScreen(vm, onBack = { navController.popBackStack() }) }
    }
}
