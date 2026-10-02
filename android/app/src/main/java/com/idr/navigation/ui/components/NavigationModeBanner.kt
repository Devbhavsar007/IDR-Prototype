package com.idr.navigation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idr.navigation.ui.NavigationViewModel.NavigationMode
import com.idr.navigation.ui.theme.*

/**
 * Navigation Mode Health Banner
 *
 * Sits at the top of the screen. Communicates the current navigation mode
 * with color-coded urgency:
 *   Green  → GNSS+INS Fusion (healthy)
 *   Blue   → Dead Reckoning Active (IDR signature moment)
 *   Amber  → Degraded GNSS (multipath/interference)
 *   Red    → Critical outage warning
 *   Gray   → Initializing / aligning sensors
 */
@Composable
fun NavigationModeBanner(
    mode: NavigationMode,
    drDurationSec: Int = 0,
    horizontalAccuracyM: Double = 999.0,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (mode) {
            NavigationMode.GNSS_INS_FUSION, NavigationMode.GNSS_INS -> GnssFusionGreen
            NavigationMode.DEAD_RECKONING -> DeadReckoningBlue
            NavigationMode.DEGRADED -> DegradedAmber
            NavigationMode.RECOVERY -> RecoveryTeal
            NavigationMode.GNSS_ONLY -> IdrBlue
            NavigationMode.INITIALIZING -> Color(0xFF5F6368)
        },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bannerColor"
    )

    val textColor = when (mode) {
        NavigationMode.DEGRADED -> TextPrimary
        else -> TextOnPrimary
    }

    // Pulse animation for Dead Reckoning mode
    val infiniteTransition = rememberInfiniteTransition(label = "drPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val bannerText = when (mode) {
        NavigationMode.GNSS_INS_FUSION, NavigationMode.GNSS_INS ->
            "● GNSS + INS Fusion Active"
        NavigationMode.DEAD_RECKONING -> {
            val accStr = if (horizontalAccuracyM < 100) String.format("±%.1fm", horizontalAccuracyM) else ""
            if (drDurationSec > 0) "⚡ Dead Reckoning • ${drDurationSec}s $accStr"
            else "⚡ Dead Reckoning Active"
        }
        NavigationMode.DEGRADED ->
            "⚠ Degraded GNSS — Multipath Detected"
        NavigationMode.RECOVERY ->
            "✓ GNSS Signal Recovered"
        NavigationMode.GNSS_ONLY ->
            "● GNSS Positioning Active"
        NavigationMode.INITIALIZING ->
            "◌ Aligning Sensors..."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Pulsing indicator dot for DR mode
            if (mode == NavigationMode.DEAD_RECKONING) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(pulseAlpha)
                        .background(Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = bannerText,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
                modifier = if (mode == NavigationMode.DEAD_RECKONING) {
                    Modifier.alpha(pulseAlpha.coerceAtLeast(0.7f))
                } else Modifier
            )
        }
    }
}
