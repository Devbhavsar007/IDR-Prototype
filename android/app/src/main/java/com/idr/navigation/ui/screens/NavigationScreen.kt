package com.idr.navigation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idr.navigation.ui.NavigationViewModel
import com.idr.navigation.ui.NavigationViewModel.NavigationMode
import com.idr.navigation.ui.NavigationViewModel.VehicleProfile
import com.idr.navigation.ui.components.AccuracyChip
import com.idr.navigation.ui.components.EngineeringOverlay
import com.idr.navigation.ui.components.NavigationBottomSheet
import com.idr.navigation.ui.components.NavigationModeBanner
import com.idr.navigation.ui.components.SpeedBadge
import com.idr.navigation.ui.theme.*

/**
 * Main Navigation Screen — Google Maps Full-Bleed Layout
 *
 * Architecture:
 * ┌─────────────────────────────────────────┐
 * │ Status Bar (transparent)                │
 * │ [Mode Banner] ← color-coded            │
 * │ ┌─────────────────────────────────────┐ │
 * │ │ Search Capsule                      │ │
 * │ │                                     │ │
 * │ │      Full-Bleed Map Area            │ │
 * │ │      (placeholder for now)          │ │
 * │ │                                     │ │
 * │ │ [Speed]  [Compass]         [FABs]   │ │
 * │ │ [Acc.]                              │ │
 * │ └─────────────────────────────────────┘ │
 * │ ┌─────────────────────────────────────┐ │
 * │ │ Bottom Sheet                        │ │
 * │ │ - Status summary                    │ │
 * │ │ - Vehicle chips                     │ │
 * │ │ - Start/Stop + Engineering          │ │
 * │ │ - Expandable telemetry              │ │
 * │ └─────────────────────────────────────┘ │
 * └─────────────────────────────────────────┘
 */
@Composable
fun NavigationScreen(
    viewModel: NavigationViewModel,
    onToggleNavigation: () -> Unit,
    onVehicleProfileSelected: (VehicleProfile) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Layer 0: Map Placeholder (full bleed) ──
        MapPlaceholder(
            mode = uiState.mode,
            latitude = uiState.latitude,
            longitude = uiState.longitude,
            headingDeg = uiState.headingDeg,
            roadLevel = uiState.roadLevel
        )

        // ── Layer 1: Top Controls ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .align(Alignment.TopCenter)
        ) {
            // Mode Banner
            NavigationModeBanner(
                mode = uiState.mode,
                drDurationSec = uiState.drDurationSec,
                horizontalAccuracyM = uiState.horizontalAccuracyM,
            )

            // Search Capsule
            SearchCapsule(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // ── Layer 2: Map Overlay Controls (Speed, Compass, FABs) ──
        // Speed Badge — bottom left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 280.dp)
        ) {
            SpeedBadge(speedKmh = uiState.speedKmh)
            Spacer(modifier = Modifier.height(8.dp))
            AccuracyChip(
                headingDeg = uiState.headingDeg,
                horizontalAccuracyM = uiState.horizontalAccuracyM
            )
        }

        // FABs — bottom right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 280.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallFloatingActionButton(
                onClick = { /* Compass bearing */ },
                containerColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Compass",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            SmallFloatingActionButton(
                onClick = { /* Map layers */ },
                containerColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Layers",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            FloatingActionButton(
                onClick = { /* Re-center map */ },
                containerColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = IdrBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ── Layer 3: Bottom Sheet ──
        NavigationBottomSheet(
            uiState = uiState,
            onToggleNavigation = onToggleNavigation,
            onVehicleProfileSelected = onVehicleProfileSelected,
            onToggleEngineering = { viewModel.toggleEngineeringMode() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // ── Layer 4: Engineering Overlay ──
        EngineeringOverlay(
            isVisible = uiState.isEngineeringMode,
            uiState = uiState,
            onDismiss = { viewModel.toggleEngineeringMode() }
        )
    }
}

/**
 * Google-style Search Capsule
 */
@Composable
private fun SearchCapsule(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .clickable { /* Open search */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Search destination...",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Map Placeholder — Full-Bleed Background
 *
 * Renders a styled placeholder representing the map area.
 * In production, this would be replaced by MapLibre/Google Maps SDK.
 * The background color shifts based on navigation mode to reinforce
 * the "IDR Signature Moment."
 */
@Composable
private fun MapPlaceholder(
    mode: NavigationMode,
    latitude: Double,
    longitude: Double,
    headingDeg: Double,
    roadLevel: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.02f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mapBgPulse"
    )

    val mapBgColor = when (mode) {
        NavigationMode.DEAD_RECKONING -> Color(0xFF1A237E) // Deep indigo for DR
        NavigationMode.DEGRADED -> Color(0xFF3E2723)       // Dark amber tone
        else -> SurfaceMapDefault
    }

    val roadLevelColor = when (roadLevel) {
        1 -> LayerFlyover.copy(alpha = 0.1f)
        -1 -> LayerUnderpass.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mapBgColor)
            .background(roadLevelColor),
        contentAlignment = Alignment.Center
    ) {
        // Grid pattern to suggest map texture
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vehicle position indicator (blue dot)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                IdrBlue,
                                IdrBlue.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(IdrBlue, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Coordinate display on map
            val textColor = if (mode == NavigationMode.DEAD_RECKONING || mode == NavigationMode.DEGRADED) {
                Color.White.copy(alpha = 0.5f)
            } else {
                TextTertiary.copy(alpha = 0.6f)
            }

            if (latitude != 0.0 || longitude != 0.0) {
                Text(
                    text = String.format("%.6f°N, %.6f°E", latitude, longitude),
                    fontSize = 11.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            }

            // DR Mode overlay text
            AnimatedVisibility(
                visible = mode == NavigationMode.DEAD_RECKONING,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text(
                        text = "DEAD RECKONING",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "GNSS OUTAGE — INS NAVIGATION ACTIVE",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
