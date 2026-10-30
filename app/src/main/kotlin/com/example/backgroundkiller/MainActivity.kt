package com.example.backgroundkiller

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    
    // Shizuku পারমিশন রেজাল্ট লিসেনার
    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 1) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                println("Shizuku Permission Granted")
            } else {
                println("Shizuku Permission Denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // লিসেনার অ্যাড করা
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        setContent {
            HackerThemeApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }
}

@Composable
fun HackerThemeApp() {
    // কালার প্যালেট (হ্যাকিং স্টাইল)
    val bgColor = Color(0xFF0F0F0F) // ঘোর কালো
    val neonGreen = Color(0xFF39FF14) // নিয়ন গ্রিন
    val neonCyan = Color(0xFF00FFFF) // নিয়ন সায়ান
    val textColor = Color(0xFFCCCCCC)

    var logMessage by remember { mutableStateOf("> System Ready...\n> Waiting for connection...") }
    var isConnected by remember { mutableStateOf(false) }

    // Shizuku কানেক্ট করার ফাংশন
    fun connectShizuku() {
        if (Shizuku.isPreV11() || Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            isConnected = true
            logMessage = "> Shizuku Connected Successfully.\n> Ready to kill apps."
        } else {
            logMessage = "> Requesting Shizuku permission..."
            Shizuku.requestPermission(1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // হেডার
        Text(
            text = "BACKGROUND KILLER",
            color = neonGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 20.dp, bottom = 30.dp)
        )

        // স্ট্যাটাস
        Text(
            text = if (isConnected) "STATUS: ACTIVE" else "STATUS: OFFLINE",
            color = if (isConnected) neonGreen else Color.Red,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Shizuku কানেক্ট বাটন
        Button(
            onClick = { connectShizuku() },
            colors = ButtonDefaults.buttonColors(containerColor = neonCyan),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                text = "CONNECT SHIZUKU",
                color = Color.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // কিল অ্যাপস বাটন
        Button(
            onClick = {
                if (isConnected) {
                    logMessage = "> Killing background apps..."
                    // পরবর্তী ধাপে এখানে আসল কিলিং লজিক বসবে
                } else {
                    logMessage = "> Error: Connect Shizuku first!"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                text = "KILL BACKGROUND APPS",
                color = Color.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // টার্মিনাল লগ এরিয়া
        Text(
            text = "TERMINAL LOG:",
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Text(
                text = logMessage,
                color = neonGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}