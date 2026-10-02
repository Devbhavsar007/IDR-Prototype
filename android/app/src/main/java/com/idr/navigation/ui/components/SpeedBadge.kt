package com.idr.navigation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idr.navigation.ui.theme.*

/**
 * Google Maps-style Speed Badge
 *
 * Circular speed display with unit label. Positioned as a floating
 * overlay on the map, matching Google Navigation's bottom-left speed indicator.
 */
@Composable
fun SpeedBadge(
    speedKmh: Double,
    modifier: Modifier = Modifier
) {
    val displaySpeed = speedKmh.coerceAtLeast(0.0).toInt()

    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(SpeedBadgeBg)
            .border(2.5.dp, SpeedBadgeBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$displaySpeed",
                fontSize = if (displaySpeed >= 100) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 24.sp
            )
            Text(
                text = "km/h",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                lineHeight = 10.sp
            )
        }
    }
}

/**
 * Accuracy / Heading Info Chip
 *
 * Small floating chip showing heading and horizontal accuracy.
 * Positioned near the speed badge.
 */
@Composable
fun AccuracyChip(
    headingDeg: Double,
    horizontalAccuracyM: Double,
    modifier: Modifier = Modifier
) {
    val compassDir = when {
        headingDeg < 22.5 || headingDeg >= 337.5 -> "N"
        headingDeg < 67.5 -> "NE"
        headingDeg < 112.5 -> "E"
        headingDeg < 157.5 -> "SE"
        headingDeg < 202.5 -> "S"
        headingDeg < 247.5 -> "SW"
        headingDeg < 292.5 -> "W"
        else -> "NW"
    }

    val accuracyColor = when {
        horizontalAccuracyM < 2.0 -> GnssFusionGreen
        horizontalAccuracyM < 5.0 -> IdrBlue
        horizontalAccuracyM < 15.0 -> DegradedAmber
        else -> OutageCritical
    }

    Row(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${headingDeg.toInt()}° $compassDir",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(accuracyColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "±${String.format("%.1f", horizontalAccuracyM)}m",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = accuracyColor
        )
    }
}
