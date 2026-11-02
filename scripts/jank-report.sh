#!/usr/bin/env bash
# Quick check without the benchmark: installs the release-like build, flings the feed and prints
# Android's own frame stats (dumpsys gfxinfo). Usage: ./scripts/jank-report.sh
set -euo pipefail
cd "$(dirname "$0")/.."
pkg=com.acme.fieldnotes

./gradlew :app:installBenchmark --console=plain -q
adb shell am force-stop "$pkg"
adb shell am start -W -n "$pkg/.MainActivity" > /dev/null
sleep 3
adb shell dumpsys gfxinfo "$pkg" reset > /dev/null

read -r w h < <(adb shell wm size | tail -1 | sed 's/.*: //; s/x/ /')
x=$((w / 2)); top=$((h / 4)); bottom=$((h * 3 / 4))
for _ in 1 2 3 4 5 6 7 8; do
  adb shell input swipe "$x" "$bottom" "$x" "$top" 120
  sleep 1
done
sleep 2

adb shell dumpsys gfxinfo "$pkg" | grep -E "Total frames rendered|Janky frames|50th percentile|90th percentile|95th percentile|99th percentile|Number Slow UI thread|Number Slow bitmap uploads|Number Frame deadline missed" | sed 's/^ *//'
