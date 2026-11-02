---
name: android-emulator
description: How to set up the Android SDK and emulator for Field Notes, build the app, run the unit tests and the scroll benchmark, and record the screen. Use before building, testing or measuring this app.
---

# Field Notes on the Android emulator

## Set up (once per machine)

```sh
./scripts/setup-android.sh
```

It installs the Android SDK into `~/android-sdk`, writes `local.properties`, adds a swap file when the
machine has none, creates an API 34 emulator called `fieldnotes` (720x1600) and boots it with a window
on the desktop so the screen can be recorded. It takes about 5 minutes the first time and is safe to
run again. Open a new shell afterwards, or run
`export PATH=$HOME/android-sdk/platform-tools:$HOME/android-sdk/emulator:$PATH`, to get `adb`.

- If `adb devices` shows nothing or `offline`, run the script again. It only boots an emulator when
  none is running.
- If a "System UI isn't responding" dialog appears, tap **Wait**. It happens on slow machines right
  after boot.
- Do not run two Gradle builds at the same time. The machine is small and the emulator freezes when it
  runs out of memory.

## Build and test

```sh
./gradlew :app:testDebugUnitTest      # JVM unit tests, no emulator needed
./gradlew :app:installBenchmark       # release-like build (not debuggable), the one to measure
adb shell am start -n com.acme.fieldnotes/.MainActivity
```

Measure the `benchmark` build type, never the debug build. Debug builds of Compose are much slower and
the numbers mean nothing.

## Measure scrolling

- `./scripts/benchmark.sh <label>` is the main measurement. It runs the macrobenchmark in
  `benchmark/src/main/java/com/acme/fieldnotes/benchmark/FeedScrollBenchmark.kt` (5 iterations, each
  starts the app fresh and makes the same 8 swipes). It copies the JSON to
  `results/<label>-benchmarkData.json` and prints:
  - `frameDurationCpuMs` and `frameOverrunMs` P50/P90/P95/P99 (`FrameTimingMetric`)
  - `recomposeSumMs` and `recomposeCount`: total time and number of `Recomposer:recompose` passes on
    the main thread during the swipes (`TraceSectionMetric`)
  It takes about 3 minutes. It leaves one Perfetto trace per iteration under
  `benchmark/build/outputs/connected_android_test_additional_output/`. Open them with
  `pip install perfetto` and `perfetto.trace_processor.TraceProcessor` to see which slices are slow
  and on which thread.
- `./scripts/jank-report.sh` installs the benchmark build, makes the same 8 swipes and prints
  `dumpsys gfxinfo` (janky frames, frame-time percentiles, slow UI thread frames). It takes about a
  minute.
- Do not change `FeedScrollBenchmark.kt`, `jank-report.sh` or the emulator settings between a
  "before" and an "after" run. Otherwise the numbers do not compare.
- The emulator has no GPU and draws every frame with the CPU, so even a perfect feed takes about
  70 ms per frame here. Frame times will not reach 16 ms. Judge a fix by the change in
  `recomposeSumMs`, frame time P50, and slow UI thread frames, measured on the same emulator.

## Record the screen

```sh
adb shell screenrecord --time-limit 15 /sdcard/feed.mp4 &
# ...scroll the feed (adb shell input swipe 360 1200 360 400 300) ...
wait; adb pull /sdcard/feed.mp4 results/feed.mp4
```

The emulator window is also on the desktop, so a desktop screen recording works too.
