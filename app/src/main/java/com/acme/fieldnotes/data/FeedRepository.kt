package com.acme.fieldnotes.data

/**
 * Serves the home feed. The backend is stubbed with a fixed, seeded data set so every
 * run of the app (and of the benchmark) scrolls through exactly the same posts.
 */
class FeedRepository(private val clock: () -> Long = System::currentTimeMillis) {

    fun loadFeed(): List<Post> {
        val now = clock()
        return List(POST_COUNT) { i ->
            Post(
                id = 1_000L + i,
                author = AUTHORS[i % AUTHORS.size],
                place = PLACES[(i * 7) % PLACES.size],
                caption = CAPTIONS[(i * 5) % CAPTIONS.size],
                photo = "photos/trail_%02d.jpg".format(i % PHOTO_COUNT + 1),
                // The first posts are seconds old, older ones go back several days.
                postedAt = now - (i.toLong() * i * 41_000L + i * 3_000L),
                likes = (i * 37 + 11) % 400,
                likedByMe = false,
                tags = TAGS.slice(i % 3 until i % 3 + 3),
            )
        }
    }

    companion object {
        const val POST_COUNT = 120
        const val PHOTO_COUNT = 12

        private val AUTHORS = listOf(
            "Maya Chen", "Luis Ortega", "Priya Nair", "Sam Okafor", "Hannah Weiss",
            "Kenji Sato", "Amara Diallo", "Tom Becker", "Sofia Rossi", "Noah Park",
        )
        private val PLACES = listOf(
            "Angels Landing", "Half Dome", "Mount Rainier", "Glacier Point", "The Narrows",
            "Delicate Arch", "Mount Whitney", "Highline Trail", "Mist Trail", "Sky Pond",
            "Cascade Pass", "Emerald Lake",
        )
        private val CAPTIONS = listOf(
            "Started at 5am and it was worth every step.",
            "Clouds rolled in right after this shot.",
            "Trail was icy above the tree line. Bring spikes.",
            "Best sunrise of the year so far.",
            "Saw two marmots and one very bold chipmunk.",
            "Legs are done. Heart is full.",
            "Parking lot was full by 7, get there early.",
        )
        private val TAGS = listOf("hiking", "sunrise", "trailrunning", "backpacking", "alpine")
    }
}
