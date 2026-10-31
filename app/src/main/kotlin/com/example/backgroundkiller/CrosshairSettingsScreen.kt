package com.example.backgroundkiller

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CrosshairSettingsScreen(onBack: () -> Unit, onNavigateToAimApps: () -> Unit) {
    val context = LocalContext.current
    val bgColor = Color(0xFF0A0A0A)
    val neonGreen = Color(0xFF39FF14)
    val textColor = Color(0xFFAAAAAA)
    
    val sharedPref = context.getSharedPreferences("CrosshairPrefs", Context.MODE_PRIVATE)
    
    var posXPercent by remember { mutableStateOf(sharedPref.getFloat("pos_x_pct", 0.5f)) }
    var posYPercent by remember { mutableStateOf(sharedPref.getFloat("pos_y_pct", 0.5f)) }
    
    var outerColor by remember { mutableStateOf(sharedPref.getInt("outer_color", AndroidColor.WHITE)) }
    var selectedShape by remember { mutableStateOf(sharedPref.getInt("shape", 0)) }
    
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Column(modifier = Modifier.fillMaxSize().background(bgColor).padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("< BACK", color = Color(0xFF00FFFF), fontFamily = FontFamily.Monospace, fontSize = 14.sp,
                modifier = Modifier.clickable { onBack() })
            Text("CROSSHAIR SETUP", color = neonGreen, fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                modifier = Modifier.weight(1f).height(35.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("1. USAGE ACCESS", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            }
            Button(
                onClick = { onNavigateToAimApps() },
                modifier = Modifier.weight(1f).height(35.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A0F00)),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFA500)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("2. SELECT GAMES", color = Color(0xFFFFA500), fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Shape:", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val shapes = listOf("+", "O", "△", "□", "•")
            shapes.forEachIndexed { index, symbol ->
                Box(modifier = Modifier.weight(1f).height(35.dp).background(if(selectedShape == index) neonGreen else Color(0xFF151515), RoundedCornerShape(4.dp))
                    .border(1.dp, if(selectedShape == index) neonGreen else Color.Gray, RoundedCornerShape(4.dp))
                    .clickable { selectedShape = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(symbol, color = if(selectedShape == index) Color.Black else Color.White, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Text("Color:", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Row(modifier = Modifier.padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val colors = listOf(AndroidColor.WHITE, AndroidColor.GREEN, AndroidColor.RED, AndroidColor.CYAN, AndroidColor.YELLOW)
            colors.forEach { colorInt ->
                Box(modifier = Modifier.size(28.dp).background(Color(colorInt), CircleShape)
                    .border(2.dp, if (outerColor == colorInt) Color.White else Color.Transparent, CircleShape)
                    .clickable { outerColor = colorInt })
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                .onSizeChanged { boxSize = it }
        ) {
            if (boxSize.width > 0 && boxSize.height > 0) {
                val currentX = (boxSize.width * posXPercent) - 40
                val currentY = (boxSize.height * posYPercent) - 40

                Box(
                    modifier = Modifier
                        .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
                        .size(80.dp)
                        .pointerInput(boxSize) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = posXPercent + (dragAmount.x / boxSize.width)
                                val newY = posYPercent + (dragAmount.y / boxSize.height)
                                posXPercent = newX.coerceIn(0f, 1f)
                                posYPercent = newY.coerceIn(0f, 1f)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val outlineColor = Color(outerColor)
                        val shadowColor = Color.Black.copy(alpha = 0.6f)
                        
                        val stroke = 2.dp.toPx() // ছোট স্ট্রোক
                        val shadowStroke = stroke + 2.dp.toPx() 
                        val r = 8.dp.toPx() // ছোট রেডিয়াস
                        
                        // ছোট লাইনের মাপ (PUBG স্টাইল)
                        val gap = 2.dp.toPx()
                        val len = 6.dp.toPx()

                        val path = Path()

                        when (selectedShape) {
                            0 -> { // PLUS
                                drawLine(shadowColor, Offset(center.x, center.y - gap - len), Offset(center.x, center.y - gap), strokeWidth = shadowStroke, cap = StrokeCap.Round)
                                drawLine(shadowColor, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + len), strokeWidth = shadowStroke, cap = StrokeCap.Round)
                                drawLine(shadowColor, Offset(center.x - gap - len, center.y), Offset(center.x - gap, center.y), strokeWidth = shadowStroke, cap = StrokeCap.Round)
                                drawLine(shadowColor, Offset(center.x + gap, center.y), Offset(center.x + gap + len, center.y), strokeWidth = shadowStroke, cap = StrokeCap.Round)

                                drawLine(outlineColor, Offset(center.x, center.y - gap - len), Offset(center.x, center.y - gap), strokeWidth = stroke, cap = StrokeCap.Round)
                                drawLine(outlineColor, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + len), strokeWidth = stroke, cap = StrokeCap.Round)
                                drawLine(outlineColor, Offset(center.x - gap - len, center.y), Offset(center.x - gap, center.y), strokeWidth = stroke, cap = StrokeCap.Round)
                                drawLine(outlineColor, Offset(center.x + gap, center.y), Offset(center.x + gap + len, center.y), strokeWidth = stroke, cap = StrokeCap.Round)
                            }
                            1 -> { // CIRCLE
                                drawCircle(shadowColor, r, center, style = Stroke(width = shadowStroke))
                                drawCircle(outlineColor, r, center, style = Stroke(width = stroke))
                            }
                            2 -> { // TRIANGLE
                                path.moveTo(center.x, center.y - r)
                                path.lineTo(center.x + r, center.y + r)
                                path.lineTo(center.x - r, center.y + r)
                                path.close()
                                drawPath(path, shadowColor, style = Stroke(width = shadowStroke))
                                drawPath(path, outlineColor, style = Stroke(width = stroke))
                            }
                            3 -> { // SQUARE
                                path.moveTo(center.x - r, center.y - r)
                                path.lineTo(center.x + r, center.y - r)
                                path.lineTo(center.x + r, center.y + r)
                                path.lineTo(center.x - r, center.y + r)
                                path.close()
                                drawPath(path, shadowColor, style = Stroke(width = shadowStroke))
                                drawPath(path, outlineColor, style = Stroke(width = stroke))
                            }
                            4 -> { // DOT
                                drawCircle(shadowColor, r/1.5f, center)
                                drawCircle(outlineColor, r/2, center)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                sharedPref.edit()
                    .putFloat("pos_x_pct", posXPercent)
                    .putFloat("pos_y_pct", posYPercent)
                    .putInt("outer_color", outerColor)
                    .putInt("shape", selectedShape)
                    .apply()
                context.startService(Intent(context, CrosshairService::class.java))
                onBack()
            },
            modifier = Modifier.fillMaxWidth().height(45.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("SAVE & START AIM", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}