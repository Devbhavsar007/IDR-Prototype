package com.idr.navigation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.outlined.BusAlert
import androidx.compose.material.icons.outlined.ElectricRickshaw
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idr.navigation.ui.NavigationViewModel
import com.idr.navigation.ui.NavigationViewModel.NavigationMode
import com.idr.navigation.ui.NavigationViewModel.VehicleProfile
import com.idr.navigation.ui.theme.*

/**
 * Navigation Bottom Sheet
 *
 * Google Maps-style bottom sheet showing:
 * 1. Drag handle
 * 2. Navigation status summary
 * 3. Vehicle profile selector chips
 * 4. Quick actions (Start/Stop, Engineering Mode)
 * 5. Expandable telemetry details
 */
@Composable
fun NavigationBottomSheet(
    uiState: NavigationViewModel.NavUiState,
    onToggleNavigation: () -> Unit,
    onVehicleProfileSelected: (VehicleProfile) -> Unit,
    onToggleEngineering: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .animateContentSize(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
    ) {
        // ── Drag Handle ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SheetHandle)
            )
        }

        // ── Status Summary Row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "IDR Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (uiState.mode) {
                        NavigationMode.DEAD_RECKONING ->
                            "Dead Reckoning • ${uiState.drDurationSec}s • ±${String.format("%.1f", uiState.horizontalAccuracyM)}m"
                        NavigationMode.GNSS_INS_FUSION, NavigationMode.GNSS_INS ->
                            "Fused Position • ±${String.format("%.1f", uiState.horizontalAccuracyM)}m accuracy"
                        NavigationMode.DEGRADED ->
                            "Degraded Signal • Multipath compensation active"
                        NavigationMode.RECOVERY ->
                            "Signal recovered • Re-fusing GNSS"
                        else ->
                            if (uiState.isServiceRunning) "Sensor alignment in progress..."
                            else "Tap Start to begin navigation"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mode indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        when (uiState.mode) {
                            NavigationMode.GNSS_INS_FUSION, NavigationMode.GNSS_INS -> GnssFusionGreen
                            NavigationMode.DEAD_RECKONING -> DeadReckoningBlue
                            NavigationMode.DEGRADED -> DegradedAmber
                            NavigationMode.RECOVERY -> RecoveryTeal
                            else -> TextTertiary
                        },
                        CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Vehicle Profile Chips ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VehicleChip(
                icon = Icons.Default.DirectionsCar,
                label = "Car",
                isSelected = uiState.vehicleProfile == VehicleProfile.CAR,
                onClick = { onVehicleProfileSelected(VehicleProfile.CAR) }
            )
            VehicleChip(
                icon = Icons.Default.TwoWheeler,
                label = "Bike",
                isSelected = uiState.vehicleProfile == VehicleProfile.BIKE,
                onClick = { onVehicleProfileSelected(VehicleProfile.BIKE) }
            )
            VehicleChip(
                icon = Icons.Outlined.ElectricRickshaw,
                label = "Auto",
                isSelected = uiState.vehicleProfile == VehicleProfile.AUTO_RICKSHAW,
                onClick = { onVehicleProfileSelected(VehicleProfile.AUTO_RICKSHAW) }
            )
            VehicleChip(
                icon = Icons.Outlined.BusAlert,
                label = "Bus",
                isSelected = uiState.vehicleProfile == VehicleProfile.BUS,
                onClick = { onVehicleProfileSelected(VehicleProfile.BUS) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Action Buttons ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onToggleNavigation,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isServiceRunning) OutageCritical else IdrBlue
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isServiceRunning) Icons.Default.Speed else Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isServiceRunning) "Stop" else "Start Navigation",
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onToggleEngineering,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Engineering Mode",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // ── Expandable Telemetry ──
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(300)) + fadeIn(),
            exit = shrinkVertically(tween(300)) + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Divider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                TelemetryRow("Coordinates",
                    String.format("%.6f°N, %.6f°E", uiState.latitude, uiState.longitude))
                TelemetryRow("Altitude",
                    String.format("%.1f m MSL", uiState.altitude))
                TelemetryRow("Heading",
                    String.format("%.0f° (±%.1f°)", uiState.headingDeg, uiState.headingAccuracyDeg))
                TelemetryRow("H. Accuracy",
                    String.format("±%.2f m", uiState.horizontalAccuracyM))
                TelemetryRow("V. Accuracy",
                    String.format("±%.2f m", uiState.verticalAccuracyM))
                TelemetryRow("Alignment",
                    uiState.alignmentQuality.label)
                TelemetryRow("IMU Samples",
                    "${uiState.imuCount}")
                TelemetryRow("Road Level",
                    when (uiState.roadLevel) {
                        1 -> "Flyover (+)"
                        -1 -> "Underpass (-)"
                        else -> "Surface (0)"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun VehicleChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) IdrBlue else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
