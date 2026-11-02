package com.acme.fieldnotes.data

data class Post(
    val id: Long,
    val author: String,
    val place: String,
    val caption: String,
    val photo: String,
    val postedAt: Long,
    val likes: Int,
    val likedByMe: Boolean,
    val tags: List<String>,
)

fun List<Post>.toggleLike(id: Long): List<Post> = map { post ->
    if (post.id != id) {
        post
    } else if (post.likedByMe) {
        post.copy(likes = post.likes - 1, likedByMe = false)
    } else {
        post.copy(likes = post.likes + 1, likedByMe = true)
    }
}
