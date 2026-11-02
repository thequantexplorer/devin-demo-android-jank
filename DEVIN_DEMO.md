# Lab: Fix a slow Android screen

Field Notes is a Jetpack Compose app with a feed of photo posts. The feed stutters when you scroll. You ask Devin to measure the stutter on an Android emulator, find the causes, fix them, and show before/after numbers and screen recordings.

You only use your browser. Devin installs the Android SDK, runs the emulator and takes the measurements on its own machine.

## What you need

- A GitHub account
- A Devin account at [app.devin.ai](https://app.devin.ai)

## Steps

1. **Make your copy.** Open [github.com/thequantexplorer/devin-demo-android-jank](https://github.com/thequantexplorer/devin-demo-android-jank). Click **Use this template**, then **Create a new repository**. Choose your account as the owner, keep the name `devin-demo-android-jank`, and click **Create repository**.
2. **Give Devin access.** Open [Settings → Connections → GitHub](https://app.devin.ai/settings/connections/github) in Devin. Make sure Devin can access your new `devin-demo-android-jank` repo. If you have no GitHub connection yet, click **Add Connection** and pick the repo.
3. **Start the session.** Go to [app.devin.ai](https://app.devin.ai), paste the prompt below, and send it. Replace `<you>` with your GitHub user name.
4. **Watch.** Open the **Desktop** tab in the session to see the emulator. The first setup takes about 10 minutes, because Devin downloads the Android SDK and an emulator image.
5. **Check the result.** Open the pull request Devin links in the session. Watch the two recordings Devin sends you in the session.

The whole lab takes about 30 to 40 minutes.

## Prompt

```text
The home feed in https://github.com/<you>/devin-demo-android-jank stutters when you scroll. Measure it, fix it, and prove it.

1. Follow .agents/skills/android-emulator/SKILL.md to set up the Android emulator and build the app.
2. Measure before you change any code. On the emulator, run ./scripts/benchmark.sh before and ./scripts/jank-report.sh, and save the output. Record a short video of the feed scrolling.
3. Find the causes in the code and in the Perfetto traces from the benchmark. Check for recomposition on every scroll frame or clock tick, photos decoded on the main thread, and LazyColumn keys that change.
4. Fix the causes. Keep the feed looking and working the same. Do not change the benchmark, the scripts or the emulator settings.
5. Run the same two measurements again (./scripts/benchmark.sh after) and record the same scroll.
6. Make sure ./gradlew :app:testDebugUnitTest passes.
7. Open a pull request. In the description, add a before/after table with frameDurationCpuMs P50/P90/P99, frameOverrunMs P50, recomposeSumMs and recomposeCount from the benchmark, and janky frames and slow UI thread frames from jank-report. List each cause, the file it was in, and the change that fixed it. Send me the before and after recordings in this session.
```

## What to watch for

- Devin measures and records the stutter before it changes any code.
- It points at real causes in the code and the traces, not guesses.
- It runs the exact same benchmark again after the fix.

## You are done when

- The pull request has a before/after table, and `recomposeSumMs` dropped from about 14 seconds to well under 1 second.
- You have a "before" and an "after" recording of the same scroll.
- The unit tests pass and the feed looks the same.

## Why the numbers look high

The emulator has no GPU. It draws every frame with the CPU, so even a perfect feed takes about 70 ms per frame there. Frame times will not get down to the 16 ms of a real phone. Compare before and after on the same emulator.

Here is what the starter measures on that emulator:

```
$ ./scripts/benchmark.sh before
FeedScrollBenchmark.scrollFeed  (results/before-benchmarkData.json)
  frameDurationCpuMs     P50    201.2   P90    306.4   P95    374.1   P99    545.8
  frameOverrunMs         P50    330.7   P90    495.5   P95    607.0   P99    811.0
  frameCount             median     89.0
  recomposeCount         median     98.0
  recomposeSumMs         median  14153.0
```

## Optional: save the setup for next time

If Devin suggests saving the Android setup as an environment blueprint, review the suggestion card in the session and click **Approve**. Later sessions then start with the SDK and emulator ready. You can skip this; the lab works without it.
