from pathlib import Path


def test_web_dashboard_html_structure():
    dashboard_path = Path(__file__).resolve().parent / "index.html"
    assert dashboard_path.is_file(), f"Dashboard not found at {dashboard_path}"

    with open(dashboard_path, "r", encoding="utf-8") as f:
        html = f.read()

    # Structural assertions
    assert "<!DOCTYPE html>" in html
    assert "<canvas id=\"trajectoryCanvas\"></canvas>" in html
    assert "DPDP Act 2023 Compliant" in html
    assert "Barapullah Elevated" in html
    assert "Invariant EKF on SE₂(3)" in html
    assert "Standard Error-State EKF" in html
    assert "Lightweight Neural Model" in html
    assert "speedDisplay" in html
    assert "togglePlayBtn" in html

    # Blueprint section coverage assertions
    assert "5-Layer Pipeline Status" in html
    assert "TCN Block 1" in html
    assert "14-Class Motion" in html
    assert "Nominal State vs. Error State" in html
    assert "Error Covariance Matrix P" in html
    assert "Adaptive Non-Holonomic Constraints" in html
    assert "SQLite Spatial R-tree Schema" in html
    assert "Probabilistic HMM Map Matching" in html
    assert "GNSS Integrity State Machine" in html
    assert "Complete Failure-Mode Matrix" in html
    assert "SIH 2026 Target Benchmarks" in html
    assert "20-Phase Production Roadmap" in html
    assert "bootOverlay" in html
    assert "imuOscilloscope" in html

    # SevaSetu design integration assertions
    assert "data-theme" in html
    assert "themeToggleBtn" in html
    assert "btnToggle3D" in html
    assert "sevasetuSearchInput" in html
    assert "sevasetu-stats-panel" in html
    assert "sevasetu-floating-legend" in html
    assert "weatherAlertBanner" in html
    assert "maplibre-gl" in html

    # Map Visual Enhancements & HUD Widgets
    assert "satRadarCanvas" in html
    assert "elevationProfileCanvas" in html
    assert "mapScenarioBadge" in html
    assert "compassNeedle" in html
    assert "compassDegText" in html

    # System Architecture & Interactive Evaluator Walkthrough
    assert "explainerToolbar" in html
    assert "explainerStepIndicator" in html
    assert "exp-chip-1" in html
    assert "exp-chip-5" in html
    assert "autoTourBtn" in html

    # Global Telemetry Synchronization across all Tabs
    assert "neuralTensorVal" in html
    assert "nominalPosCell" in html
    assert "rtreeQueryVal" in html
    assert "matchedEdgeVal" in html
    assert "liveIntegState" in html
    assert "liveSih002Drift" in html


