package com.example.backgroundkiller

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    // তোমার GitHub রিপোজিটরির API লিংক
    private const val API_URL = "https://api.github.com/repos/tuhin118/Background-Killer/releases/latest"

    fun checkForUpdate(context: Context, onUpdateFound: (String, String) -> Unit) {
        Thread {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val response = inputStream.bufferedReader().use { it.readText() }
                    
                    val json = JSONObject(response)
                    val latestVersion = json.getString("tag_name").replace("v", "") // "v1.0.0" থেকে "v" বাদ দেওয়া
                    val downloadUrl = json.getString("html_url") // রিলিজ পেজের লিংক

                    // বর্তমান অ্যাপের ভার্সন বের করা
                    val currentVersion = getCurrentVersion(context)

                    // ভার্সন কম্পেয়ার করা
                    if (isNewerVersion(currentVersion, latestVersion)) {
                        Handler(Looper.getMainLooper()).post {
                            onUpdateFound(latestVersion, downloadUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    // ফোনের ইনস্টল করা অ্যাপের ভার্সন বের করার ফাংশন
    private fun getCurrentVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    // কোনটি নতুন ভার্সন তা চেক করার ফাংশন (যেমন: 1.1.0 > 1.0.0)
    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}