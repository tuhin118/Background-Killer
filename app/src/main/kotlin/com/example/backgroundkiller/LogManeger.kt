package com.example.backgroundkiller

import androidx.compose.runtime.mutableStateListOf

object LogManager {
    // UI-তে লাইভ লগ দেখানোর জন্য
    val logs = mutableStateListOf<String>("> System Initializing...", "> Waiting for connection.")
    
    fun addLog(message: String) {
        logs.add(message)
        // লিস্ট অনেক বড় হয়ে গেলে পুরনো লগ মুছে ফেলা (যাতে অ্যাপ স্লো না হয়)
        if (logs.size > 50) {
            logs.removeAt(0)
        }
    }
}