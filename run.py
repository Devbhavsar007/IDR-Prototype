"""
IDR (Intelligent Dead Reckoning) — Project Launcher & Services Runner.

Usage:
    python run.py                   # Launch interactive Web Dashboard + Cloud API at http://127.0.0.1:8000
    python run.py --port 8080       # Run on custom port
    python run.py --benchmark       # Run automated multi-scenario benchmark suite
    python run.py --replay          # Run C++ replay engine demonstration
    python run.py --test            # Run all verification tests
"""

import argparse
import os
import subprocess
import sys
import webbrowser
from pathlib import Path

_REPO_ROOT = Path(__file__).resolve().parent
if str(_REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(_REPO_ROOT))


def run_server(host: str = "127.0.0.1", port: int = 8000, open_browser: bool = True):
    print("=" * 65)
    print("  [*] Starting DrishtiAI / IDR Prototype Navigation Platform")
    print("=" * 65)
    print(f"  * Interactive Web Dashboard : http://{host}:{port}/")
    print(f"  * OpenAPI / Swagger Docs    : http://{host}:{port}/docs")
    print(f"  * Cloud Health Check        : http://{host}:{port}/health")
    print("=" * 65)
    print("Press Ctrl+C to terminate the server.\n")

    if open_browser:
        try:
            webbrowser.open(f"http://{host}:{port}/")
        except Exception:
            pass

    import uvicorn
    uvicorn.run("cloud.api.app:app", host=host, port=port, reload=False)


def run_benchmark():
    print("\n--- Running IDR Automated Benchmark Suite ---")
    from tools.benchmark_runner.benchmark_runner import BenchmarkRunner

    candidates = [
        _REPO_ROOT / "build" / "simulation" / "idr-replay.exe",
        _REPO_ROOT / "build" / "simulation" / "idr-replay",
    ]
    binary = None
    for c in candidates:
        if c.is_file():
            binary = c
            break

    runner = BenchmarkRunner(replay_binary_path=binary)
    results = runner.run_suite(output_dir=_REPO_ROOT / "benchmark_results")
    print("\nResults Summary:")
    for r in results:
        status = "PASSED" if r.passed_targets else "FAILED"
        print(f"  [{status}] {r.scenario} ({r.vehicle}): ATE RMS = {r.ate_rms_m}m, Drift = {r.drift_pct_per_km} %/km")


def run_replay():
    print("\n--- Running C++ Replay Simulation ---")
    bin_path = _REPO_ROOT / "build" / "simulation" / "idr-replay.exe"
    if not bin_path.is_file():
        print(f"Error: {bin_path} not found. Please build the project first.")
        sys.exit(1)

    import tempfile
    from tools.data_collector.data_collector import SyntheticDriveGenerator

    with tempfile.NamedTemporaryFile(suffix=".csv", delete=False) as tmp:
        tmp_csv = Path(tmp.name)

    try:
        print("Generating synthetic drive trajectory...")
        gen = SyntheticDriveGenerator(duration_sec=30.0)
        gen.generate(tmp_csv)

        env = os.environ.copy()
        if "C:\\msys64\\mingw64\\bin" not in env.get("PATH", ""):
            env["PATH"] = "C:\\msys64\\mingw64\\bin;" + env.get("PATH", "")

        cmd = [
            str(bin_path),
            "-i", str(tmp_csv),
            "-v", "car",
            "--outage-start", "10.0",
            "--outage-end", "20.0",
        ]
        print(f"Executing: {' '.join(cmd)}\n")
        proc = subprocess.run(cmd, env=env)
        if proc.returncode == 0:
            print("\nReplay completed successfully!")
    finally:
        if tmp_csv.is_file():
            tmp_csv.unlink()


def run_tests():
    print("\n--- Running Project Test Suite ---")
    ret = subprocess.call([sys.executable, "-m", "pytest", "-v"])
    sys.exit(ret)


def main():
    parser = argparse.ArgumentParser(description="IDR Prototype Launcher")
    parser.add_argument("--host", default="127.0.0.1", help="Host address (default: 127.0.0.1)")
    parser.add_argument("--port", type=int, default=8000, help="Port (default: 8000)")
    parser.add_argument("--no-browser", action="store_true", help="Don't open browser automatically")
    parser.add_argument("--benchmark", action="store_true", help="Run automated benchmarks")
    parser.add_argument("--replay", action="store_true", help="Run C++ replay simulation")
    parser.add_argument("--test", action="store_true", help="Run pytest suite")

    args = parser.parse_args()

    if args.test:
        run_tests()
    elif args.benchmark:
        run_benchmark()
    elif args.replay:
        run_replay()
    else:
        run_server(host=args.host, port=args.port, open_browser=not args.no_browser)


if __name__ == "__main__":
    main()
