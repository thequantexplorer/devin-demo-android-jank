# Field Notes (Devin workshop starter: android-jank)

Field Notes is a small Android app written with Jetpack Compose. It shows a feed of 120 hiking posts, each with a photo.

The feed stutters when you scroll. This repo is the starting point for the **Fix a slow Android screen** lab. The steps and the prompt are in [DEVIN_DEMO.md](DEVIN_DEMO.md).

You don't need Android Studio, an emulator or anything else on your computer. Devin builds the app, runs the emulator and takes the measurements on its own machine.

## What is in the repo

| Path | What it is |
| --- | --- |
| `app/` | The app. The feed is in `app/src/main/java/com/acme/fieldnotes/ui/`. |
| `app/src/test/` | Unit tests for the feed data (JVM, no emulator needed). |
| `benchmark/` | A macrobenchmark that scrolls the feed the same way every time and records frame times and recomposition time. |
| `scripts/setup-android.sh` | Installs the Android SDK and starts an emulator. |
| `scripts/benchmark.sh` | Runs the macrobenchmark and prints a summary. |
| `scripts/jank-report.sh` | A quicker check that prints Android's own frame stats (`dumpsys gfxinfo`). |
| `.agents/skills/android-emulator/SKILL.md` | A repo skill that tells Devin how to set up, build, measure and record this app. |
| `.github/workflows/ci.yml` | GitHub Actions: runs the unit tests and builds the app and the benchmark on every push. |

## The numbers at the start

Measured on the emulator that `scripts/setup-android.sh` creates (API 34, x86_64, no GPU):

```
$ ./scripts/benchmark.sh before
FeedScrollBenchmark.scrollFeed  (results/before-benchmarkData.json)
  frameDurationCpuMs     P50    201.2   P90    306.4   P95    374.1   P99    545.8
  frameOverrunMs         P50    330.7   P90    495.5   P95    607.0   P99    811.0
  frameCount             median     89.0
  recomposeCount         median     98.0
  recomposeSumMs         median  14153.0
```

`recomposeSumMs` is the total time the main thread spent recomposing during one scroll test. 14 seconds of recomposition in a scroll that lasts about 15 seconds means the main thread almost never gets a break.

The emulator draws with the CPU, so even a perfect feed takes about 70 ms to draw each frame there. Compare before and after on the same emulator. Don't compare these numbers with 16 ms targets for real phones.
