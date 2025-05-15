package com.duvijan.pranaapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duvijan.pranaapp.R
import com.duvijan.pranaapp.model.BreathingStage
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.clickable

@Composable
fun CircularTimer(
    currentStage: BreathingStage,
    remainingSeconds: Int,
    currentCount: Int,
    totalCountInCycle: Int,
    cycleCount: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 16.dp.toPx()
            
            // Background track
            drawCircle(
                color = Color(0xFF272727),
                radius = radius,
                center = center,
                style = Stroke(width = 24.dp.toPx())
            )
            
            // Progress arc
            drawArc(
                color = Color(0xFF4AEDB6),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw breathing phase indicators around the circle
            val stageRadius = radius - 12.dp.toPx()
            val stages = listOf(
                BreathingStage.INHALE to 0f,
                BreathingStage.HOLD to 90f,
                BreathingStage.EXHALE to 180f,
                BreathingStage.SILENCE to 270f
            )
            
            stages.forEach { (stage, angle) ->
                val radians = Math.toRadians(angle.toDouble())
                val x = center.x + cos(radians) * stageRadius
                val y = center.y + sin(radians) * stageRadius
                val dotRadius = if (stage == currentStage) 12.dp.toPx() else 8.dp.toPx()
                val dotColor = if (stage == currentStage) Color(0xFF4AEDB6) else Color(0xFF666666)
                
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }
        
        // Center diamond icon
        Image(
            painter = painterResource(id = R.drawable.diamond_center),
            contentDescription = "Timer center",
            modifier = Modifier.size(100.dp)
        )
        
        // Digital time display at the top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(remainingSeconds),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = stringResource(R.string.base_count_label),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Text(
                    text = stringResource(R.string.tour),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Cycle indicators at the bottom
        Text(
            text = "$cycleCount / $totalCountInCycle",
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}

@Composable
fun BreathingPhaseIndicator(
    stage: BreathingStage,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val iconRes = when (stage) {
        BreathingStage.INHALE -> R.drawable.inhale_icon
        BreathingStage.HOLD -> R.drawable.hold_icon
        BreathingStage.EXHALE -> R.drawable.exhale_icon
        BreathingStage.SILENCE -> R.drawable.silence_icon
    }
    
    Column(
        modifier = modifier
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF2A2A2A) else Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = stage.displayName,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = stage.displayName,
            fontSize = 12.sp,
            color = if (isActive) Color(0xFF4AEDB6) else Color.Gray
        )
    }
}

@Composable
fun VoiceControlSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.voice_cycle),
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF4AEDB6),
                activeTrackColor = Color(0xFF4AEDB6),
                inactiveTrackColor = Color(0xFF272727)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(Color(0xFF2CCFB0), Color(0xFF4AEDB6))
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
    }
}

@Composable
fun BackgroundSoundSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Background Sound",
            color = Color.White,
            fontSize = 14.sp
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VolumeDown,
                contentDescription = "Volume Low",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF4AEDB6),
                    activeTrackColor = Color(0xFF4AEDB6),
                    inactiveTrackColor = Color.Gray
                ),
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Volume High",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    val tenths = 0 // We don't have tenths precision in our timer
    
    return "$hours:$minutes:$secs:$tenths"
}
