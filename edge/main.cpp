// IDR — Intelligent Dead Reckoning with GNSS Fusion
// Copyright (c) 2026 IDR Project. All rights reserved.
//
// Edge binary entry point.

#include "idr/edge/edge_backend.h"
#include "idr/engine/navigation_state.h"
#include "adapters/serial_adapter.h"
#include <iostream>
#include <thread>
#include <chrono>

int main(int argc, char* argv[]) {
    std::cout << "=====================================================\n";
    std::cout << " IDR Edge Localization Service v0.1.0-alpha\n";
    std::cout << " Intelligent Dead Reckoning with Multi-Constellation Fusion\n";
    std::cout << "=====================================================\n";

    idr::edge::EdgeConfig config;
    config.format = idr::edge::OutputFormat::NMEA_PROPRIETARY;
    config.output_rate_hz = 10.0;

    idr::edge::EdgeBackend backend(config);
    backend.setSendCallback([](const uint8_t* data, size_t len) {
        std::cout << "[EMIT] " << std::string(reinterpret_cast<const char*>(data), len);
    });

    idr::NavigationState state;
    state.latitude_deg = 28.6139;
    state.longitude_deg = 77.2090;
    state.altitude_m = 216.0;
    state.speed_mps = 12.5;
    state.heading_deg = 45.0;
    state.mode = idr::NavigationMode::FUSION_6DOF;
    state.horizontal_accuracy_m = 0.85;
    state.is_stationary = false;
    state.alignment_quality = 3;

    std::cout << "Broadcasting initial telemetry frame...\n";
    backend.emit(state);

    std::cout << "Edge service initialized successfully.\n";
    return 0;
}
