package com.example.backgroundkiller

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class KillerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val CHANNEL_ID = "BackgroundKillerChannel"
    private val NOTIFICATION_ID = 1001
    private val MY_PACKAGE_NAME = "com.example.backgroundkiller"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        LogManager.addLog("> Boost Engine Online.")
        startKillerLoop()

        return START_STICKY
    }

    private fun startKillerLoop() {
        serviceScope.launch {
            while (true) {
                try {
                    val sharedPref: SharedPreferences = getSharedPreferences("WhitelistPrefs", MODE_PRIVATE)
                    val whitelist: Set<String> = sharedPref.getStringSet("whitelist", HashSet<String>()) ?: HashSet()

                    val runningApps = getThirdPartyPackages()
                    var killedCount = 0

                    for (pkg in runningApps) {
                        if (pkg != MY_PACKAGE_NAME && !whitelist.contains(pkg)) {
                            killApp(pkg)
                            killedCount++
                        }
                    }
                    
                    if (killedCount > 0) {
                         // অপশনাল: চাইলে এখানে লগ দিতে পারেন যে কতগুলো অ্যাপ কিল হলো
                    }

                } catch (e: Exception) {
                    Log.e("KillerService", "Error: ${e.message}")
                }

                delay(3000) // প্রতি ৩ সেকেন্ড পর পর
            }
        }
    }

    private fun getThirdPartyPackages(): List<String> {
        val packages = mutableListOf<String>()
        try {
            val process = if (isRootAvailable()) {
                Runtime.getRuntime().exec("su -c pm list packages -3")
            } else {
                val method = Shizuku::class.java.getDeclaredMethod(
                    "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java
                )
                method.isAccessible = true
                method.invoke(null, arrayOf("sh", "-c", "pm list packages -3"), null, null) as Process
            }

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                packages.add(line!!.replace("package:", "").trim())
            }
        } catch (e: Exception) {
            Log.e("KillerService", "Failed packages: ${e.message}")
        }
        return packages
    }

    private fun killApp(packageName: String) {
        try {
            if (isRootAvailable()) {
                Runtime.getRuntime().exec("su -c am force-stop $packageName")
                LogManager.addLog("> Killed (Root): $packageName")
            } else if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val method = Shizuku::class.java.getDeclaredMethod(
                    "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java
                )
                method.isAccessible = true
                method.invoke(null, arrayOf("sh", "-c", "am force-stop $packageName"), null, null)
                LogManager.addLog("> Killed (Shizuku): $packageName")
            }
        } catch (e: Exception) {
            Log.e("KillerService", "Failed kill: ${e.message}")
        }
    }

    private fun isRootAvailable(): Boolean {
        return try {
            Runtime.getRuntime().exec("su -c id").waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Background Killer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setContentTitle("Background Killer is Active")
        builder.setContentText("Killing background apps to boost your game.")
        builder.setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
        builder.setPriority(NotificationCompat.PRIORITY_LOW)
        builder.setOngoing(true)
        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}