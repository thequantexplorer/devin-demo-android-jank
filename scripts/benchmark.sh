#!/usr/bin/env bash
# Runs the feed scroll macrobenchmark on the connected emulator and prints a frame-time summary.
# Usage: ./scripts/benchmark.sh [label]   e.g. ./scripts/benchmark.sh before
set -euo pipefail
cd "$(dirname "$0")/.."
label="${1:-run}"

./gradlew :benchmark:connectedBenchmarkAndroidTest --console=plain -q

json="$(find benchmark/build/outputs/connected_android_test_additional_output -name '*benchmarkData.json' -print -quit)"
if [ -z "$json" ]; then
  echo "No benchmark results found." >&2
  exit 1
fi
mkdir -p results
cp "$json" "results/$label-benchmarkData.json"
python3 scripts/summarize_benchmark.py "results/$label-benchmarkData.json"
