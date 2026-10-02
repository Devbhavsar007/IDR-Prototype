package com.idr.navigation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idr.navigation.ui.NavigationViewModel
import com.idr.navigation.ui.NavigationViewModel.AlignmentQuality
import com.idr.navigation.ui.NavigationViewModel.NavigationMode
import com.idr.navigation.ui.theme.*

/**
 * Engineering Mode Overlay
 *
 * Advanced diagnostics panel for navigation engineers. Shows raw
 * sensor data, EKF state, alignment quality, GNSS integrity, and
 * Dead Reckoning session metrics.
 *
 * This is NOT for end users — it's the "Flight Deck" that makes
 * the prototype's engineering depth visible.
 */
@Composable
fun EngineeringOverlay(
    isVisible: Boolean,
    uiState: NavigationViewModel.NavUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(tween(400)) { it } + fadeIn(tween(300)),
        exit = slideOutVertically(tween(400)) { it } + fadeOut(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .clickable(enabled = false) { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(SurfaceDark)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = DeadReckoningBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Engineering Diagnostics",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextOnDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "IDR Sensor Fusion Flight Deck",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9AA0A6)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextOnDark
                        )
                    }
                }

                Divider(color = DarkDivider)

                // ── EKF State Card ──
                DiagCard(
                    title = "Extended Kalman Filter",
                    icon = Icons.Default.Timeline,
                    iconTint = DeadReckoningBlue
                ) {
                    MonoRow("Mode", uiState.mode.label)
                    MonoRow("Mode Code", "${uiState.mode.code}")
                    MonoRow("GNSS Confidence", String.format("%.2f%%", uiState.gnssConfidence * 100))
                    MonoRow("GNSS Integrity", "${uiState.gnssIntegrity}")
                    MonoRow("H. Accuracy (68%)", String.format("±%.3f m", uiState.horizontalAccuracyM))
                    MonoRow("V. Accuracy (68%)", String.format("±%.3f m", uiState.verticalAccuracyM))
                    MonoRow("Heading Accuracy", String.format("±%.2f°", uiState.headingAccuracyDeg))
                    MonoRow("Stationary", if (uiState.isStationary) "YES (ZUPT)" else "NO")
                }

                // ── Sensor Status Card ──
                DiagCard(
                    title = "Sensor Pipeline",
                    icon = Icons.Default.Sensors,
                    iconTint = GnssFusionGreen
                ) {
                    MonoRow("IMU Sample Count", "${uiState.imuCount}")
                    MonoRow("IMU Rate", "100 Hz (target)")
                    MonoRow("Alignment", uiState.alignmentQuality.label)
                    MonoRow("Model Loaded", if (uiState.modelLoaded) "YES (ONNX)" else "NO")
                    MonoRow("Vehicle Profile", uiState.vehicleProfile.label)
                }

                // ── Position Card ──
                DiagCard(
                    title = "Fused Position",
                    icon = Icons.Default.Timeline,
                    iconTint = RecoveryTeal
                ) {
                    MonoRow("Latitude", String.format("%.8f°", uiState.latitude))
                    MonoRow("Longitude", String.format("%.8f°", uiState.longitude))
                    MonoRow("Altitude MSL", String.format("%.2f m", uiState.altitude))
                    MonoRow("Speed", String.format("%.1f km/h (%.2f m/s)", uiState.speedKmh, uiState.speedKmh / 3.6))
                    MonoRow("Heading", String.format("%.2f° True", uiState.headingDeg))
                    MonoRow("Road Level", when (uiState.roadLevel) {
                        1 -> "FLYOVER (+1)"
                        -1 -> "UNDERPASS (-1)"
                        else -> "SURFACE (0)"
                    })
                    MonoRow("Road Match", if (uiState.matchedRoadId >= 0)
                        "ID:${uiState.matchedRoadId} (${String.format("%.0f", uiState.matchedRoadConfidence * 100)}%)"
                    else "—")
                }

                // ── DR Session Card (visible during Dead Reckoning) ──
                if (uiState.mode == NavigationMode.DEAD_RECKONING || uiState.drDurationSec > 0) {
                    DiagCard(
                        title = "Dead Reckoning Session",
                        icon = Icons.Default.Timeline,
                        iconTint = DegradedAmber
                    ) {
                        MonoRow("Duration", "${uiState.drDurationSec}s")
                        MonoRow("Drift Estimate",
                            String.format("±%.2f m", uiState.horizontalAccuracyM))
                        MonoRow("Status", if (uiState.mode == NavigationMode.DEAD_RECKONING)
                            "ACTIVE — Pure INS" else "ENDED — Re-fused")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DiagCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDarkElevated
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextOnDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MonoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF9AA0A6),
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextOnDark,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
