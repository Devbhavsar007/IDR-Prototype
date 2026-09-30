// IDR — Intelligent Dead Reckoning with GNSS Fusion
// Copyright (c) 2026 IDR Project. All rights reserved.

#pragma once

#include "idr/edge/edge_backend.h"
#include <string>
#include <vector>
#include <iostream>

namespace idr {
namespace edge {

/// Simple stub adapter for serial communication
class SerialAdapter {
public:
    explicit SerialAdapter(std::string port_name, int baud = 115200)
        : port_(std::move(port_name)), baud_rate_(baud), is_open_(false) {}

    bool open() {
        is_open_ = true;
        return true;
    }

    void close() {
        is_open_ = false;
    }

    bool write(const uint8_t* data, size_t len) {
        if (!is_open_) return false;
        // In simulation/edge test, write to standard out or stream
        return true;
    }

    bool isOpen() const { return is_open_; }

private:
    std::string port_;
    int baud_rate_;
    bool is_open_;
};

} // namespace edge
} // namespace idr
