package com.acme.fieldnotes.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeTimeTest {
    private val now = 1_800_000_000_000L

    @Test fun recentPostsSayJustNow() = assertEquals("just now", formatRelative(now, now - 4_000))
    @Test fun seconds() = assertEquals("45s", formatRelative(now, now - 45_000))
    @Test fun minutes() = assertEquals("5m", formatRelative(now, now - 5 * 60_000))
    @Test fun hours() = assertEquals("3h", formatRelative(now, now - 3 * 3_600_000))
    @Test fun days() = assertEquals("2d", formatRelative(now, now - 2 * 86_400_000L))
    @Test fun futureTimestampsClampToNow() = assertEquals("just now", formatRelative(now, now + 30_000))
}
