package com.idr.navigation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.idr.navigation.ui.NavigationViewModel
import com.idr.navigation.ui.NavigationViewModel.VehicleProfile
import com.idr.navigation.ui.screens.NavigationScreen
import com.idr.navigation.ui.theme.IdrNavigationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * IDR Main Navigation Activity.
 *
 * Provides the Google Maps-inspired navigation interface with full-bleed map,
 * mode-aware health banner, speed badge, bottom sheet controls, and
 * engineering diagnostics overlay.
 *
 * Architecture:
 *   Activity → owns Service lifecycle (bind/unbind)
 *   ViewModel → bridges Service StateFlows to Compose UI
 *   Compose → renders reactive UI from ViewModel state
 *
 * The C++ JNI bridge, Foreground Service, Sensor Manager, and DPDP
 * consent flow are preserved from the original prototype.
 */
class NavigationActivity : ComponentActivity() {

    private lateinit var consentManager: DpdpConsentManager
    private val viewModel: NavigationViewModel by viewModels()

    private var navigationService: IdrNavigationService? = null
    private var isBound = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocationGranted) {
            checkConsentAndStartService()
        } else {
            Toast.makeText(this, "Location permission is required for navigation", Toast.LENGTH_LONG).show()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as IdrNavigationService.LocalBinder
            navigationService = localBinder.getService()
            isBound = true
            viewModel.setServiceRunning(true)
            observeNavigationData()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            navigationService = null
            isBound = false
            viewModel.setServiceRunning(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        consentManager = DpdpConsentManager(this)

        setContent {
            IdrNavigationTheme {
                NavigationScreen(
                    viewModel = viewModel,
                    onToggleNavigation = { toggleNavigation() },
                    onVehicleProfileSelected = { profile ->
                        viewModel.setVehicleProfile(profile)
                        navigationService?.idrNative?.setVehicleProfile(profile.id)
                    },
                )
            }
        }

        requestRequiredPermissions()
    }

    private fun toggleNavigation() {
        if (isBound) {
            unbindService(serviceConnection)
            stopService(Intent(this, IdrNavigationService::class.java))
            isBound = false
            viewModel.setServiceRunning(false)
        } else {
            bindNavigationService()
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            checkConsentAndStartService()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun checkConsentAndStartService() {
        if (!consentManager.getConsentState().isValid) {
            DpdpConsentDialog(this, consentManager) { granted ->
                if (granted) {
                    bindNavigationService()
                } else {
                    Toast.makeText(this, "Consent is required under DPDP Act 2023", Toast.LENGTH_SHORT).show()
                }
            }.show()
        } else {
            bindNavigationService()
        }
    }

    private fun bindNavigationService() {
        val serviceIntent = Intent(this, IdrNavigationService::class.java).apply {
            action = IdrNavigationService.ACTION_START
        }
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Observe navigation state and diagnostics from the foreground service
     * and forward them to the ViewModel for Compose consumption.
     */
    private fun observeNavigationData() {
        val service = navigationService ?: return

        lifecycleScope.launch {
            service.navigationState.collectLatest { state ->
                viewModel.updateNavigationState(
                    lat = state.latitudeDeg,
                    lon = state.longitudeDeg,
                    alt = state.altitudeM,
                    speedMps = state.speedMps,
                    headingDeg = state.headingDeg,
                    hAccM = state.horizontalAccuracyM,
                    vAccM = state.verticalAccuracyM,
                    headingAccDeg = state.headingAccuracyDeg,
                    modeCode = state.mode,
                    gnssConf = state.gnssConfidence,
                    isStationary = state.isStationary,
                    alignCode = state.alignmentQuality,
                    roadId = state.matchedRoadId,
                    roadConf = state.matchedRoadConfidence,
                    roadLevel = state.roadLevel,
                    timestampNs = state.timestampNs
                )
            }
        }

        lifecycleScope.launch {
            service.diagnostics.collectLatest { diag ->
                viewModel.updateDiagnostics(
                    modeCode = diag.mode,
                    gnssIntegrity = diag.gnssIntegrity,
                    alignCode = diag.alignmentQuality,
                    isStationary = diag.isStationary,
                    modelLoaded = diag.modelLoaded,
                    hAccM = diag.horizontalAccuracyM,
                    imuCount = diag.imuCount
                )
            }
        }
    }

    override fun onDestroy() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onDestroy()
    }
}
