package com.example.backgroundkiller

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val bgColor = Color(0xFF0A0A0A)
    val cardBgColor = Color(0xFF151515)
    val textColor = Color(0xFFAAAAAA)
    val neonGreen = Color(0xFF39FF14)

    val sharedPref = context.getSharedPreferences("WhitelistPrefs", Context.MODE_PRIVATE)
    val savedWhitelist: Set<String> = sharedPref.getStringSet("whitelist", HashSet()) ?: HashSet()

    var appList by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
    var whitelist by remember { mutableStateOf(savedWhitelist) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        appList = apps.filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { pm.getApplicationLabel(it).toString() }
    }

    val filteredApps = appList.filter {
        val name = context.packageManager.getApplicationLabel(it).toString().lowercase()
        val pkg = it.packageName.lowercase()
        name.contains(searchQuery.lowercase()) || pkg.contains(searchQuery.lowercase())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "< BACK",
            color = Color(0xFF00FFFF),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 16.dp)
        )

        Text(
            text = "WHITELIST APPS",
            color = neonGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = "Selected apps will NOT be killed.",
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 15.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search apps...", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = neonGreen,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps) { app ->
                val isChecked = whitelist.contains(app.packageName)
                val appName = context.packageManager.getApplicationLabel(app).toString()
                val icon = context.packageManager.getApplicationIcon(app).toBitmap().asImageBitmap()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBgColor, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isChecked) neonGreen else Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = appName, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                            Text(text = app.packageName, color = textColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                    }

                    Switch(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            val newWhitelist = if (checked) whitelist + app.packageName else whitelist - app.packageName
                            whitelist = newWhitelist
                            sharedPref.edit().putStringSet("whitelist", newWhitelist).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = neonGreen,
                            uncheckedThumbColor = textColor,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}