package com.idr.navigation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * IDR Main Navigation Activity.
 *
 * Provides real-time navigation visualization, multimodal routing,
 * mode alerts, uncertainty metrics, and diagnostics for intelligent dead reckoning.
 */
class NavigationActivity : ComponentActivity() {

    private lateinit var consentManager: DpdpConsentManager
    private var navigationService: IdrNavigationService? = null
    private var isBound = false

    // UI elements
    private lateinit var tvModeBanner: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvHeading: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvCoordinates: TextView
    private lateinit var tvDiagnostics: TextView
    private lateinit var btnToggleNav: Button

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
            observeNavigationData()
            updateButtonState(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            navigationService = null
            isBound = false
            updateButtonState(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consentManager = DpdpConsentManager(this)

        setContentView(buildContentView())
        requestRequiredPermissions()
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

    private fun observeNavigationData() {
        val service = navigationService ?: return

        lifecycleScope.launch {
            service.navigationState.collectLatest { state ->
                val speedKmh = state.speedMps * 3.6
                tvSpeed.text = String.format("%.0f", speedKmh)
                tvHeading.text = String.format("Heading: %.0f°", state.headingDeg)
                tvAccuracy.text = String.format("Accuracy: ±%.1f m", state.horizontalAccuracyM)
                tvCoordinates.text = String.format("Lat: %.6f, Lon: %.6f", state.latitudeDeg, state.longitudeDeg)

                when (state.mode) {
                    3 -> { // GNSS_INS
                        tvModeBanner.text = "● GNSS + INS FUSION ACTIVE"
                        tvModeBanner.setBackgroundColor(Color.parseColor("#15803D")) // Deep Emerald
                    }
                    5 -> { // DEAD_RECKONING
                        tvModeBanner.text = "⚡ PURE DEAD RECKONING (GPS OUTAGE)"
                        tvModeBanner.setBackgroundColor(Color.parseColor("#1E66FF")) // Royal Blue
                    }
                    4 -> { // DEGRADED
                        tvModeBanner.text = "⚠ DEGRADED GNSS (MULTIPATH / INTERFERENCE)"
                        tvModeBanner.setBackgroundColor(Color.parseColor("#E65100")) // Amber
                    }
                    else -> {
                        tvModeBanner.text = "ALIGNING SENSORS..."
                        tvModeBanner.setBackgroundColor(Color.parseColor("#37474F")) // Slate Gray
                    }
                }
            }
        }

        lifecycleScope.launch {
            service.diagnostics.collectLatest { diag ->
                val alignStr = when (diag.alignmentQuality) {
                    3 -> "Converged"
                    2 -> "Coarse"
                    1 -> "Gravity"
                    else -> "None"
                }
                tvDiagnostics.text = String.format(
                    "Diagnostics: IMU Updates: %d | Alignment: %s | Stationary: %b",
                    diag.imuCount, alignStr, diag.isStationary
                )
            }
        }
    }

    private fun updateButtonState(running: Boolean) {
        btnToggleNav.text = if (running) "Stop Navigation" else "Start Navigation"
        val bg = GradientDrawable().apply {
            cornerRadius = 48f
            setColor(if (running) Color.parseColor("#EF4444") else Color.parseColor("#1E66FF"))
        }
        btnToggleNav.background = bg
    }

    private fun buildContentView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#EEF3F8")) // Modern Paris map light pastel
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Mode banner at top
        tvModeBanner = TextView(this).apply {
            text = "● GNSS + INS FUSION ACTIVE"
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(24, 16, 24, 16)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setBackgroundColor(Color.parseColor("#15803D"))
        }
        root.addView(tvModeBanner)

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
        }

        // 1. Floating Route Origin / Destination Card
        val routeCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
            val bg = GradientDrawable().apply {
                cornerRadius = 32f
                setColor(Color.WHITE)
                setStroke(1, Color.parseColor("#E2E8F0"))
            }
            background = bg
            elevation = 8f
        }

        // Origin row
        val originRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val originDot = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(24, 24).apply { setMargins(0, 0, 16, 0) }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#1E66FF"))
            }
        }
        val originTextCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val tvName = TextView(this@NavigationActivity).apply {
                text = "Eiffel Tower"
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#0F172A"))
            }
            val tvDesc = TextView(this@NavigationActivity).apply {
                text = "Your location"
                textSize = 12f
                setTextColor(Color.parseColor("#64748B"))
            }
            addView(tvName)
            addView(tvDesc)
        }
        originRow.addView(originDot)
        originRow.addView(originTextCol)
        routeCard.addView(originRow)

        // Divider
        val routeDivider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(40, 16, 0, 16)
            }
            setBackgroundColor(Color.parseColor("#E2E8F0"))
        }
        routeCard.addView(routeDivider)

        // Destination row
        val destRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val destDot = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(24, 24).apply { setMargins(0, 0, 16, 0) }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#EF4444"))
            }
        }
        val destTextCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val tvName = TextView(this@NavigationActivity).apply {
                text = "Arc de Triomphe"
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#0F172A"))
            }
            val tvDesc = TextView(this@NavigationActivity).apply {
                text = "Finish point"
                textSize = 12f
                setTextColor(Color.parseColor("#64748B"))
            }
            addView(tvName)
            addView(tvDesc)
        }
        destRow.addView(destDot)
        destRow.addView(destTextCol)
        routeCard.addView(destRow)
        content.addView(routeCard)

        // 2. Telemetry HUD Stage (Speed & Coordinates)
        val hudCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 20, 0, 20) }
            background = GradientDrawable().apply {
                cornerRadius = 28f
                setColor(Color.WHITE)
                setStroke(1, Color.parseColor("#E2E8F0"))
            }
            elevation = 6f
        }

        val speedRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        tvSpeed = TextView(this).apply {
            text = "48"
            setTextColor(Color.parseColor("#0F172A"))
            textSize = 56f
            typeface = Typeface.DEFAULT_BOLD
        }
        val tvSpeedUnit = TextView(this).apply {
            text = " km/h"
            setTextColor(Color.parseColor("#64748B"))
            textSize = 18f
            setPadding(8, 0, 0, 16)
        }
        speedRow.addView(tvSpeed)
        speedRow.addView(tvSpeedUnit)
        hudCard.addView(speedRow)

        tvHeading = TextView(this).apply {
            text = "Heading: 324° NW"
            setTextColor(Color.parseColor("#334155"))
            textSize = 14f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
        tvAccuracy = TextView(this).apply {
            text = "Accuracy: ±0.4 m (IDR EKF 95%)"
            setTextColor(Color.parseColor("#15803D"))
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 4, 0, 4)
        }
        tvCoordinates = TextView(this).apply {
            text = "Paris: 48.8584° N, 2.2945° E (Pont d'Iéna)"
            setTextColor(Color.parseColor("#64748B"))
            textSize = 12f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
        hudCard.addView(tvHeading)
        hudCard.addView(tvAccuracy)
        hudCard.addView(tvCoordinates)

        tvDiagnostics = TextView(this).apply {
            text = "Diagnostics: IMU: 200Hz | ESKF Nominal | Covariance sub-1.1m"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 11f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 8, 0, 0)
        }
        hudCard.addView(tvDiagnostics)
        content.addView(hudCard)

        // 3. Curved Royal Blue Shelf & Transit Bottom Sheet
        val transitShelf = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = 36f
                setColor(Color.parseColor("#1E66FF"))
            }
            setPadding(0, 16, 0, 0)
        }

        val whiteCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadii = floatArrayOf(36f, 36f, 36f, 36f, 0f, 0f, 0f, 0f)
                setColor(Color.WHITE)
            }
            setPadding(28, 24, 28, 28)
        }

        val tvTransitTitle = TextView(this).apply {
            text = "Public transport"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#0F172A"))
        }
        whiteCard.addView(tvTransitTitle)

        // Route details row
        val statRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 12, 0, 12)
            gravity = Gravity.CENTER_VERTICAL
        }
        val tvDuration = TextView(this).apply {
            text = "17 min  "
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#0F172A"))
        }
        val tvDetails = TextView(this).apply {
            text = "17 km • 5:20-5:40"
            textSize = 13f
            setTextColor(Color.parseColor("#64748B"))
        }
        val tvFare = TextView(this).apply {
            text = "2,99 $"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#0F172A"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { weight = 1.0f }
            gravity = Gravity.END
        }
        statRow.addView(tvDuration)
        statRow.addView(tvDetails)
        statRow.addView(tvFare)
        whiteCard.addView(statRow)

        val tvTransitSteps = TextView(this).apply {
            text = "🚶 1 min  ▶  [Bus 56]  [Bus 24]  ▶  🚶 16 min"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#15803D"))
            setPadding(0, 4, 0, 16)
        }
        whiteCard.addView(tvTransitSteps)

        // Toggle / Action Button
        btnToggleNav = Button(this).apply {
            text = "Start Navigation"
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            val bg = GradientDrawable().apply {
                cornerRadius = 48f
                setColor(Color.parseColor("#1E66FF"))
            }
            background = bg
            setOnClickListener {
                if (isBound) {
                    unbindService(serviceConnection)
                    stopService(Intent(this@NavigationActivity, IdrNavigationService::class.java))
                    isBound = false
                    updateButtonState(false)
                } else {
                    bindNavigationService()
                }
            }
        }
        whiteCard.addView(btnToggleNav)
        transitShelf.addView(whiteCard)
        content.addView(transitShelf)

        scroll.addView(content)
        root.addView(scroll)
        return root
    }

    override fun onDestroy() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onDestroy()
    }
}
