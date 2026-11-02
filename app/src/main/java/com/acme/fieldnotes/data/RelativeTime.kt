package com.acme.fieldnotes.data

private const val SECOND = 1_000L
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/** Short "time ago" label, like "45s", "5m", "3h" or "2d". */
fun formatRelative(now: Long, then: Long): String {
    val elapsed = (now - then).coerceAtLeast(0)
    return when {
        elapsed < 10 * SECOND -> "just now"
        elapsed < MINUTE -> "${elapsed / SECOND}s"
        elapsed < HOUR -> "${elapsed / MINUTE}m"
        elapsed < DAY -> "${elapsed / HOUR}h"
        else -> "${elapsed / DAY}d"
    }
}
