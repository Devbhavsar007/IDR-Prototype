package com.idr.navigation.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * IDR Navigation ViewModel
 *
 * Central state holder for the navigation UI. Bridges the IdrNavigationService's
 * sensor/GNSS data flows into composable UI state.
 *
 * Architecture: Service → ViewModel → Compose UI
 * The ViewModel does NOT own the service lifecycle — the Activity does.
 */
class NavigationViewModel : ViewModel() {

    // ── Navigation State ──

    data class NavUiState(
        // Position
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val altitude: Double = 0.0,

        // Motion
        val speedKmh: Double = 0.0,
        val headingDeg: Double = 0.0,

        // Accuracy
        val horizontalAccuracyM: Double = 999.0,
        val verticalAccuracyM: Double = 999.0,
        val headingAccuracyDeg: Double = 999.0,

        // Navigation Mode
        val mode: NavigationMode = NavigationMode.INITIALIZING,
        val gnssConfidence: Double = 0.0,
        val isStationary: Boolean = false,

        // Alignment
        val alignmentQuality: AlignmentQuality = AlignmentQuality.NONE,

        // Map matching
        val matchedRoadId: Long = -1,
        val matchedRoadConfidence: Double = 0.0,
        val roadLevel: Int = 0, // -1=underpass, 0=surface, 1=flyover

        // Diagnostics
        val imuCount: Long = 0,
        val modelLoaded: Boolean = false,
        val gnssIntegrity: Int = 0,

        // Service state
        val isServiceRunning: Boolean = false,

        // Vehicle profile
        val vehicleProfile: VehicleProfile = VehicleProfile.CAR,

        // DR session tracking
        val drStartTimeMs: Long = 0,
        val drDurationSec: Int = 0,
        val drDistanceM: Double = 0.0,

        // Engineering mode
        val isEngineeringMode: Boolean = false,
    )

    enum class NavigationMode(val code: Int, val label: String, val shortLabel: String) {
        INITIALIZING(0, "Aligning Sensors", "INIT"),
        GNSS_ONLY(1, "GNSS Only", "GNSS"),
        GNSS_INS_FUSION(2, "GNSS + INS Fusion", "FUSION"),
        GNSS_INS(3, "GNSS + INS", "GNSS+INS"),
        DEGRADED(4, "Degraded GNSS", "DEGRADED"),
        DEAD_RECKONING(5, "Dead Reckoning Active", "DR"),
        RECOVERY(6, "GNSS Recovery", "RECOVERY");

        companion object {
            fun fromCode(code: Int): NavigationMode = entries.find { it.code == code } ?: INITIALIZING
        }
    }

    enum class AlignmentQuality(val code: Int, val label: String) {
        NONE(0, "None"),
        GRAVITY(1, "Gravity Only"),
        COARSE(2, "Coarse"),
        CONVERGED(3, "Converged");

        companion object {
            fun fromCode(code: Int): AlignmentQuality = entries.find { it.code == code } ?: NONE
        }
    }

    enum class VehicleProfile(val id: String, val label: String, val icon: String) {
        CAR("car", "Car", "🚗"),
        BIKE("bike", "Two-Wheeler", "🏍️"),
        AUTO_RICKSHAW("auto_rickshaw", "Auto-Rickshaw", "🛺"),
        BUS("bus", "Bus", "🚌"),
        TRUCK("truck", "Truck", "🚛");

        companion object {
            fun fromId(id: String): VehicleProfile = entries.find { it.id == id } ?: CAR
        }
    }

    private val _uiState = MutableStateFlow(NavUiState())
    val uiState: StateFlow<NavUiState> = _uiState.asStateFlow()

    // ── State Updaters (called by Activity from Service flows) ──

    fun updateNavigationState(
        lat: Double, lon: Double, alt: Double,
        speedMps: Double, headingDeg: Double,
        hAccM: Double, vAccM: Double, headingAccDeg: Double,
        modeCode: Int, gnssConf: Double, isStationary: Boolean,
        alignCode: Int, roadId: Long, roadConf: Double, roadLevel: Int,
        timestampNs: Long
    ) {
        _uiState.update { current ->
            val mode = NavigationMode.fromCode(modeCode)
            val speedKmh = speedMps * 3.6

            // Track DR session
            val drStart = if (mode == NavigationMode.DEAD_RECKONING && current.mode != NavigationMode.DEAD_RECKONING) {
                System.currentTimeMillis()
            } else if (mode == NavigationMode.DEAD_RECKONING) {
                current.drStartTimeMs
            } else {
                0L
            }

            val drDuration = if (drStart > 0) {
                ((System.currentTimeMillis() - drStart) / 1000).toInt()
            } else 0

            current.copy(
                latitude = lat,
                longitude = lon,
                altitude = alt,
                speedKmh = speedKmh,
                headingDeg = headingDeg,
                horizontalAccuracyM = hAccM,
                verticalAccuracyM = vAccM,
                headingAccuracyDeg = headingAccDeg,
                mode = mode,
                gnssConfidence = gnssConf,
                isStationary = isStationary,
                alignmentQuality = AlignmentQuality.fromCode(alignCode),
                matchedRoadId = roadId,
                matchedRoadConfidence = roadConf,
                roadLevel = roadLevel,
                drStartTimeMs = drStart,
                drDurationSec = drDuration,
            )
        }
    }

    fun updateDiagnostics(
        modeCode: Int, gnssIntegrity: Int, alignCode: Int,
        isStationary: Boolean, modelLoaded: Boolean,
        hAccM: Double, imuCount: Long
    ) {
        _uiState.update { current ->
            current.copy(
                gnssIntegrity = gnssIntegrity,
                imuCount = imuCount,
                modelLoaded = modelLoaded,
            )
        }
    }

    fun setServiceRunning(running: Boolean) {
        _uiState.update { it.copy(isServiceRunning = running) }
    }

    fun setVehicleProfile(profile: VehicleProfile) {
        _uiState.update { it.copy(vehicleProfile = profile) }
    }

    fun toggleEngineeringMode() {
        _uiState.update { it.copy(isEngineeringMode = !it.isEngineeringMode) }
    }
}
