package com.acme.fieldnotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acme.fieldnotes.data.FeedRepository
import com.acme.fieldnotes.data.formatRelative
import com.acme.fieldnotes.data.toggleLike
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class FeedItem(
    val id: Long,
    val author: String,
    val place: String,
    val caption: String,
    val photo: String,
    val postedAgo: String,
    val likes: Int,
    val likedByMe: Boolean,
    val tags: List<String>,
)

class FeedViewModel(
    repository: FeedRepository = FeedRepository(),
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val posts = MutableStateFlow(repository.loadFeed())

    // Keeps the "5m ago" labels fresh.
    private val now = flow {
        while (true) {
            emit(clock())
            delay(1_000)
        }
    }

    val feed: StateFlow<List<FeedItem>> = combine(posts, now) { posts, now ->
        posts.map { post ->
            FeedItem(
                id = post.id,
                author = post.author,
                place = post.place,
                caption = post.caption,
                photo = post.photo,
                postedAgo = formatRelative(now, post.postedAt),
                likes = post.likes,
                likedByMe = post.likedByMe,
                tags = post.tags,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleLike(id: Long) {
        posts.update { it.toggleLike(id) }
    }
}
