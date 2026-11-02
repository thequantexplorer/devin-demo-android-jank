package com.acme.fieldnotes.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FeedRepositoryTest {
    private val repository = FeedRepository(clock = { 1_800_000_000_000L })

    @Test fun feedIsDeterministic() = assertEquals(repository.loadFeed(), repository.loadFeed())

    @Test fun feedHasAllPostsWithUniqueIds() {
        val feed = repository.loadFeed()
        assertEquals(FeedRepository.POST_COUNT, feed.size)
        assertEquals(feed.size, feed.map { it.id }.toSet().size)
    }

    @Test fun everyPhotoIsBundled() {
        repository.loadFeed().map { it.photo }.toSet().forEach { photo ->
            assertTrue("missing asset $photo", File("src/main/assets/$photo").isFile)
        }
    }

    @Test fun toggleLikeLikesThenUnlikes() {
        val feed = repository.loadFeed()
        val id = feed[3].id
        val liked = feed.toggleLike(id)
        assertEquals(feed[3].likes + 1, liked[3].likes)
        assertTrue(liked[3].likedByMe)
        assertEquals(feed[3], liked.toggleLike(id)[3])
        assertEquals(feed.filterIndexed { i, _ -> i != 3 }, liked.filterIndexed { i, _ -> i != 3 })
    }
}
