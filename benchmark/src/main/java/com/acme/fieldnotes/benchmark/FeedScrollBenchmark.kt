package com.acme.fieldnotes.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.TraceSectionMetric.Mode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Scrolls the home feed the same way every time and records how long each frame took.
 *
 * Run it with `./scripts/benchmark.sh <label>` (needs a booted emulator or device).
 * Keep this file the same between the "before" and "after" runs so the numbers compare.
 */
@RunWith(AndroidJUnit4::class)
class FeedScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun scrollFeed() = rule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(
            FrameTimingMetric(),
            // Time the app's main thread spends recomposing. The emulator has no GPU, so drawing
            // each frame has a fixed cost; this number shows the app's own work.
            TraceSectionMetric("Recomposer:recompose", Mode.Sum, label = "recompose"),
        ),
        compilationMode = CompilationMode.Full(),
        iterations = 5,
        setupBlock = {
            // Start fresh every time: same first post, same clock, same scroll position.
            killProcess()
            startActivityAndWait()
            device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), 10_000)
        },
    ) {
        // Fixed swipes by screen position, so every run scrolls the same distance at the same speed.
        val x = device.displayWidth / 2
        val top = device.displayHeight / 4
        val bottom = device.displayHeight * 3 / 4
        repeat(SWIPES) {
            device.swipe(x, bottom, x, top, 12)
            device.waitForIdle(500)
        }
    }

    private companion object {
        const val PACKAGE = "com.acme.fieldnotes"
        const val SWIPES = 8
    }
}
