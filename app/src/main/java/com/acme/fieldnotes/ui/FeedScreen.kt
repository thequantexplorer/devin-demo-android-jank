package com.acme.fieldnotes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel) {
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val scrollOffset = listState.firstVisibleItemScrollOffset
    val scrolled = listState.firstVisibleItemIndex > 0 || scrollOffset > 0
    val showBackToTop = listState.firstVisibleItemIndex > 3

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = { Text("Field Notes", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (scrolled) Color(0xFFF1F3F4) else Color.White,
                ),
            )
        },
        floatingActionButton = {
            if (showBackToTop) {
                SmallFloatingActionButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                    Text("Top", modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("feed"),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(feed, key = { it.hashCode() }) { item ->
                PostCard(
                    item = item,
                    parallax = scrollOffset,
                    onLike = { viewModel.toggleLike(item.id) },
                )
            }
        }
    }
}

@Composable
fun PostCard(item: FeedItem, parallax: Int, onLike: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(item) { PhotoDecoder.decode(context, item.photo) }
    val background = PhotoDecoder.averageColor(bitmap)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post-${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(background),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.author.take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(item.author, fontWeight = FontWeight.SemiBold)
                Text(
                    "${item.place} · ${item.postedAgo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5F6368),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RectangleShape)
                .background(background),
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = item.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-(parallax % 400) / 40).dp),
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.caption)
            Spacer(Modifier.size(4.dp))
            Text(
                item.tags.joinToString(" ") { "#$it" },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1A73E8),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onLike, modifier = Modifier.testTag("like-${item.id}")) {
                    Text(if (item.likedByMe) "♥ Liked" else "♡ Like")
                }
                Text("${item.likes} likes", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
