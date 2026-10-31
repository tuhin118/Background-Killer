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
fun AimAppSelectorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val bgColor = Color(0xFF0A0A0A)
    val cardBgColor = Color(0xFF151515)
    val textColor = Color(0xFFAAAAAA)
    val neonOrange = Color(0xFFFFA500)

    val sharedPref = context.getSharedPreferences("AimAppsPrefs", Context.MODE_PRIVATE)
    val savedAimApps: Set<String> = sharedPref.getStringSet("aim_apps", HashSet()) ?: HashSet()

    var appList by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
    var aimApps by remember { mutableStateOf(savedAimApps) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        appList = apps.filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { pm.getApplicationLabel(it).toString() }
    }

    val filteredApps = appList.filter {
        val name = context.packageManager.getApplicationLabel(it).toString().lowercase()
        name.contains(searchQuery.lowercase())
    }

    Column(modifier = Modifier.fillMaxSize().background(bgColor).padding(horizontal = 16.dp)) {
        Text(
            text = "< BACK",
            color = Color(0xFF00FFFF),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 16.dp)
        )

        Text("SELECT GAMES FOR AIM", color = neonOrange, fontFamily = FontFamily.Monospace, fontSize = 18.sp)
        Text("Crosshair will ONLY show in these apps.", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(bottom = 15.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search games...", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonOrange, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredApps) { app ->
                val isChecked = aimApps.contains(app.packageName)
                val appName = context.packageManager.getApplicationLabel(app).toString()
                val icon = context.packageManager.getApplicationIcon(app).toBitmap().asImageBitmap()

                Row(
                    modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isChecked) neonOrange else Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = appName, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                    }

                    Switch(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            val newList = if (checked) aimApps + app.packageName else aimApps - app.packageName
                            aimApps = newList
                            sharedPref.edit().putStringSet("aim_apps", newList).apply()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = neonOrange, uncheckedThumbColor = textColor, uncheckedTrackColor = Color.DarkGray)
                    )
                }
            }
        }
    }
}