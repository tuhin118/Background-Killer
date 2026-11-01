package com.example.backgroundkiller

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku

@Composable
fun FpsBoostScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val bgColor = Color(0xFF0A0A0A)
    val neonCyan = Color(0xFF00FFFF)
    val neonGreen = Color(0xFF39FF14)
    val neonRed = Color(0xFFFF3131)

    val sharedPref = context.getSharedPreferences("FpsPrefs", Context.MODE_PRIVATE)
    var isBoostEnabled by remember { mutableStateOf(sharedPref.getBoolean("fps_boost", false)) }

    fun isRootAvailable(): Boolean {
        return try {
            Runtime.getRuntime().exec("su -c id").waitFor() == 0
        } catch (e: Exception) { false }
    }

    fun executeCommand(command: String): Boolean {
        try {
            val process = if (isRootAvailable()) {
                Runtime.getRuntime().exec("su -c $command")
            } else if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val method = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
                method.isAccessible = true
                method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            } else {
                return false
            }
            return process.waitFor() == 0
        } catch (e: Exception) {
            return false
        }
    }

    fun toggleFpsBoost(enabled: Boolean) {
        val cmd = if (enabled) "settings put global thermal_throttling_enabled 0" else "settings put global thermal_throttling_enabled 1"
        if (executeCommand(cmd)) {
            isBoostEnabled = enabled
            sharedPref.edit().putBoolean("fps_boost", enabled).apply()
            LogManager.addLog("> FPS Boost: ${if (enabled) "ON" else "OFF"}")
        } else {
            LogManager.addLog("> Error: Need Root or Shizuku permission!")
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(bgColor).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("< BACK", color = neonCyan, fontFamily = FontFamily.Monospace, fontSize = 14.sp, modifier = Modifier.clickable { onBack() })
            Text("FPS BOOST", color = neonGreen, fontFamily = FontFamily.Monospace, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF151515), RoundedCornerShape(8.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Disable Thermal Throttling", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Warning: This may cause your phone to overheat rapidly. Use at your own risk.", color = neonRed, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
            Switch(
                checked = isBoostEnabled,
                onCheckedChange = { toggleFpsBoost(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = neonGreen)
            )
        }
    }
}