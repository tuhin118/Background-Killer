package com.example.backgroundkiller

import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CrosshairService : Service() {

    private lateinit var windowManager: WindowManager
    private var crosshairView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private var posXPercent = 0.5f
    private var posYPercent = 0.5f

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val sharedPref = getSharedPreferences("CrosshairPrefs", Context.MODE_PRIVATE)
        posXPercent = sharedPref.getFloat("pos_x_pct", 0.5f)
        posYPercent = sharedPref.getFloat("pos_y_pct", 0.5f)
        
        createCrosshairView()
        startMonitoringApps()
    }

    private fun createCrosshairView() {
        val sharedPref = getSharedPreferences("CrosshairPrefs", Context.MODE_PRIVATE)
        val outerColor = sharedPref.getInt("outer_color", android.graphics.Color.WHITE)
        val shape = sharedPref.getInt("shape", 0)

        crosshairView = object : View(this) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                
                // শ্যাডোর জন্য পেইন্ট (কালো)
                val shadowPaint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    style = Paint.Style.STROKE
                    strokeWidth = 6f // শ্যাডো স্ট্রোক
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                    alpha = 150
                }

                // মূল কালার পেইন্ট
                val outlinePaint = Paint().apply {
                    color = outerColor
                    style = Paint.Style.STROKE
                    strokeWidth = 3f // মূল স্ট্রোক
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }

                val cx = width / 2f
                val cy = height / 2f
                val r = 8f // ছোট রেডিয়াস
                val path = Path()

                // ছোট লাইনের মাপ (PUBG স্টাইল)
                val gap = 4f
                val len = 10f

                when (shape) {
                    0 -> { // PLUS (PUBG স্টাইল)
                        // ১. শ্যাডো আঁকা
                        canvas.drawLine(cx, cy - gap - len, cx, cy - gap, shadowPaint)
                        canvas.drawLine(cx, cy + gap, cx, cy + gap + len, shadowPaint)
                        canvas.drawLine(cx - gap - len, cy, cx - gap, cy, shadowPaint)
                        canvas.drawLine(cx + gap, cy, cx + gap + len, cy, shadowPaint)

                        // ২. আসল কালার আঁকা
                        canvas.drawLine(cx, cy - gap - len, cx, cy - gap, outlinePaint)
                        canvas.drawLine(cx, cy + gap, cx, cy + gap + len, outlinePaint)
                        canvas.drawLine(cx - gap - len, cy, cx - gap, cy, outlinePaint)
                        canvas.drawLine(cx + gap, cy, cx + gap + len, cy, outlinePaint)
                    }
                    1 -> { // CIRCLE
                        canvas.drawCircle(cx, cy, r, shadowPaint)
                        canvas.drawCircle(cx, cy, r, outlinePaint)
                    }
                    2 -> { // TRIANGLE
                        path.moveTo(cx, cy - r); path.lineTo(cx + r, cy + r); path.lineTo(cx - r, cy + r); path.close()
                        canvas.drawPath(path, shadowPaint)
                        canvas.drawPath(path, outlinePaint)
                    }
                    3 -> { // SQUARE
                        path.moveTo(cx - r, cy - r); path.lineTo(cx + r, cy - r)
                        path.lineTo(cx + r, cy + r); path.lineTo(cx - r, cy + r); path.close()
                        canvas.drawPath(path, shadowPaint)
                        canvas.drawPath(path, outlinePaint)
                    }
                    4 -> { // DOT
                        val fillPaint = Paint().apply { color = outerColor; style = Paint.Style.FILL; isAntiAlias = true }
                        val shadowFill = Paint().apply { color = android.graphics.Color.BLACK; style = Paint.Style.FILL; isAntiAlias = true; alpha = 150 }
                        canvas.drawCircle(cx, cy, r/1.5f, shadowFill)
                        canvas.drawCircle(cx, cy, r/2, fillPaint)
                    }
                }
            }
        }.apply {
            visibility = View.GONE
        }

        // ভিউ সাইজ ছোট করে ৮০x৮০ করা হলো
        layoutParams = WindowManager.LayoutParams(
            80, 80, 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            updatePosition(this)
        }

        windowManager.addView(crosshairView, layoutParams)
    }

    private fun updatePosition(params: WindowManager.LayoutParams) {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // ভিউয়ের সাইজ ৮০, তাই অর্ধেক (৪০) বিয়োগ করতে হবে সেন্টারে রাখার জন্য
        params.x = (screenWidth * posXPercent).toInt() - 40
        params.y = (screenHeight * posYPercent).toInt() - 40

        crosshairView?.let {
            if (it.isAttachedToWindow) {
                windowManager.updateViewLayout(it, params)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        layoutParams?.let { updatePosition(it) }
    }

    private fun startMonitoringApps() {
        serviceScope.launch {
            while (true) {
                val currentApp = getForegroundApp(this@CrosshairService)
                val sharedPref = getSharedPreferences("AimAppsPrefs", Context.MODE_PRIVATE)
                val aimApps: Set<String> = sharedPref.getStringSet("aim_apps", HashSet()) ?: HashSet()

                val shouldShow = currentApp != null && aimApps.contains(currentApp)

                Handler(Looper.getMainLooper()).post {
                    crosshairView?.visibility = if (shouldShow) View.VISIBLE else View.GONE
                }
                delay(1000)
            }
        }
    }

    private fun getForegroundApp(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        
        if (stats.isNullOrEmpty()) return null

        var recentApp: UsageStats? = null
        for (stat in stats) {
            val lastUsed = recentApp?.lastTimeUsed ?: 0L
            if (stat.lastTimeUsed > lastUsed) {
                recentApp = stat
            }
        }
        return recentApp?.packageName
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        crosshairView?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        crosshairView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}