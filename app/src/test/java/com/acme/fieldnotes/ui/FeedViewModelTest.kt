package com.acme.fieldnotes.ui

import com.acme.fieldnotes.data.FeedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val scheduler = dispatcher.scheduler
    private val start = 1_800_000_000_000L
    private var now = start

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = FeedViewModel(FeedRepository(clock = { start }), clock = { now })

    private fun tick(ms: Long) {
        now += ms
        scheduler.advanceTimeBy(ms + 1)
        scheduler.runCurrent()
    }

    @Test fun clockTickWithoutLabelChangeKeepsTheSameList() {
        now = start + 10 * 60 * 60_000L
        val vm = viewModel()
        scheduler.runCurrent()
        val first = vm.feed.value
        tick(1_000)
        assertSame(first, vm.feed.value)
    }

    @Test fun clockTickRefreshesLabels() {
        val vm = viewModel()
        scheduler.runCurrent()
        val first = vm.feed.value
        assertEquals("just now", first[0].postedAgo)
        tick(15_000)
        assertEquals("15s", vm.feed.value[0].postedAgo)
        assertEquals(first.map { it.id }, vm.feed.value.map { it.id })
    }

    @Test fun likeChangesOnlyThatPost() {
        now = start + 10 * 60 * 60_000L
        val vm = viewModel()
        scheduler.runCurrent()
        val before = vm.feed.value
        vm.toggleLike(before[2].id)
        scheduler.runCurrent()
        val after = vm.feed.value
        assertNotEquals(before[2], after[2])
        assertEquals(before.filterIndexed { i, _ -> i != 2 }, after.filterIndexed { i, _ -> i != 2 })
    }
}
