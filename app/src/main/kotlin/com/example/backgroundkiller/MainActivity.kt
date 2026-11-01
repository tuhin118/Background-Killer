package com.example.backgroundkiller

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 1 && grantResult == PackageManager.PERMISSION_GRANTED) {
            LogManager.addLog("> Shizuku permission granted.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        setContent { MainApp() }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf("main") }
    val context = LocalContext.current

    // আপডেট চেক করার স্টেট
    var showUpdateDialog by remember { mutableStateOf(false) }
    var newVersion by remember { mutableStateOf("") }
    var downloadUrl by remember { mutableStateOf("") }

    // অ্যাপ ওপেন হলে একবার আপডেট চেক করবে
    LaunchedEffect(Unit) {
        UpdateChecker.checkForUpdate(context) { version, url ->
            newVersion = version
            downloadUrl = url
            showUpdateDialog = true
        }
    }

    // আপডেট অ্যালার্ট ডায়ালগ
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Text(
                    text = "🚀 New Update Available!",
                    color = Color(0xFF39FF14),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Version $newVersion is now available on GitHub. Please update to get the latest features.",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                        context.startActivity(intent)
                        showUpdateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39FF14))
                ) {
                    Text("UPDATE NOW", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("LATER", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF151515)
        )
    }

    when (currentScreen) {
        "main" -> MainTerminalScreen(
            onNavigateToSelector = { currentScreen = "selector" },
            onNavigateToCrosshair = { 
                if (Settings.canDrawOverlays(context)) {
                    currentScreen = "crosshair" 
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
                    context.startActivity(intent)
                }
            }
        )
        "selector" -> AppSelectorScreen(onBack = { currentScreen = "main" })
        "crosshair" -> CrosshairSettingsScreen(
            onBack = { currentScreen = "main" },
            onNavigateToAimApps = { currentScreen = "aim_apps" }
        )
        "aim_apps" -> AimAppSelectorScreen(onBack = { currentScreen = "crosshair" })
    }
}

@Composable
fun MainTerminalScreen(onNavigateToSelector: () -> Unit, onNavigateToCrosshair: () -> Unit) {
    // ... (এই ফাংশনের ভেতরের কোড আগের মতোই থাকবে, কোনো পরিবর্তন নেই)
    val context = LocalContext.current
    val bgColor = Color(0xFF0A0A0A)
    val terminalBg = Color(0xFF111111)
    val neonGreen = Color(0xFF39FF14)
    val neonCyan = Color(0xFF00FFFF)
    val textColor = Color(0xFFAAAAAA)
    val neonRed = Color(0xFFFF3131)
    val neonPurple = Color(0xFFBF00FF)
    val neonOrange = Color(0xFFFFA500)

    var isConnected by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(LogManager.logs.size) {
        if (LogManager.logs.isNotEmpty()) {
            listState.animateScrollToItem(LogManager.logs.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(bgColor).padding(12.dp)) {
        Text("BACKGROUND KILLER", color = neonGreen, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(onClick = {
                if (Shizuku.isPreV11() || Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    isConnected = true
                    LogManager.addLog("> Shizuku already connected.")
                } else {
                    LogManager.addLog("> Requesting Shizuku permission...")
                    Shizuku.requestPermission(1)
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).height(45.dp).border(1.dp, neonCyan, RoundedCornerShape(6.dp)).background(Color(0xFF001A1A), RoundedCornerShape(6.dp)), contentPadding = PaddingValues(0.dp)) {
                Text("SHIZUKU", color = neonCyan, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            }
            Button(onClick = { onNavigateToSelector() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).height(45.dp).border(1.dp, neonPurple, RoundedCornerShape(6.dp)).background(Color(0xFF1A001A), RoundedCornerShape(6.dp)), contentPadding = PaddingValues(0.dp)) {
                Text("APPS", color = neonPurple, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            }
            Button(onClick = { onNavigateToCrosshair() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).height(45.dp).border(1.dp, neonOrange, RoundedCornerShape(6.dp)).background(Color(0xFF1A0F00), RoundedCornerShape(6.dp)), contentPadding = PaddingValues(0.dp)) {
                Text("AIM", color = neonOrange, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            }
            Button(onClick = {
                val serviceIntent = Intent(context, KillerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(serviceIntent) else context.startService(serviceIntent)
                LogManager.addLog("> Initializing Boost Engine...")
            }, colors = ButtonDefaults.buttonColors(containerColor = neonGreen), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).height(45.dp), contentPadding = PaddingValues(0.dp)) {
                Text("START", color = Color.Black, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Button(onClick = {
                context.stopService(Intent(context, KillerService::class.java))
                LogManager.addLog("> Terminating Boost Engine.")
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).height(45.dp).border(1.dp, neonRed, RoundedCornerShape(6.dp)).background(Color(0xFF1A0000), RoundedCornerShape(6.dp)), contentPadding = PaddingValues(0.dp)) {
                Text("STOP", color = neonRed, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            }
        }

        Text("LIVE TERMINAL:", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(terminalBg, RoundedCornerShape(6.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp)).padding(10.dp)) {
            LazyColumn(state = listState) {
                items(LogManager.logs) { log ->
                    Text(text = log, color = if (log.contains("Killed")) neonRed else if (log.contains("Success") || log.contains("granted")) neonGreen else neonCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Developer: tuhin118", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tuhin118"))) }.padding(vertical = 2.dp))
            Text("TikTok: @error_game_ar", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@error_game_ar?_r=1&_t=ZS-992Kh3ITMwC"))) }.padding(vertical = 2.dp))
        }
    }
}