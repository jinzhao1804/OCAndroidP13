package com.openclassrooms.hexagonal.games.screen.detail

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(postId: String) {

    val viewModel: DetailViewModel = hiltViewModel()
    // Fetch post and comments when the composable is first launched
    viewModel.fetchPostAndComments(postId)

    // Collect the post and comments state from the ViewModel
    val post = viewModel.post.collectAsState().value
    val comments = viewModel.comments.collectAsState().value

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Post Details") }
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(16.dp)
            ) {
                // Post Details
                post?.let { PostDetails(post = it) }

                Log.e("post detail","$post")

                Spacer(modifier = Modifier.height(16.dp))

                // Comment Section
                CommentSection(comments = comments)
            }
        }
    }


@Composable
fun PostDetails(post: Post) {
    // Author (Required)
    Text(
        text = "Author: ${post.author?.firstname}  ${post.author?.lastname}",
        style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Title (Required)
    Text(
        text = "Title: ${post.title}",
        style = MaterialTheme.typography.titleSmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Description (Optional)
    post.description?.let {
        Text(
            text = "Description: $it",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Image (Optional) using Glide
    post.photoUrl?.let {
        GlideImage(url = it, modifier = Modifier
            .fillMaxWidth()
            .height(200.dp))
    }
}

@Composable
fun GlideImage(url: String, modifier: Modifier = Modifier) {
    val painter = // Enable smooth transitions when the image loads
        rememberAsyncImagePainter(ImageRequest.Builder  // Optional: Show a placeholder image
        // Optional: Show a fallback error image
            (LocalContext.current).data(data = url).apply(block = fun ImageRequest.Builder.() {
            crossfade(true)  // Enable smooth transitions when the image loads
            placeholder(R.drawable.ic_launcher_background)  // Optional: Show a placeholder image
            error(R.drawable.ic_notifications)  // Optional: Show a fallback error image
        }).build()
        )

    Image(
        painter = painter,
        contentDescription = "Post Image",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
@Composable
fun CommentSection(comments: List<Comment>) {
    Text(
        text = "Comments:",
        style = MaterialTheme.typography.titleSmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    // List of Comments
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(comments) { comment ->
            CommentItem(comment = comment)
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Comment Author
        Text(
            text = "Comment by: ${comment.author}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Comment Content
        Text(
            text = comment.comment,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Preview(showBackground = true)
@Composable
fun PreviewDetailScreen() {
    HexagonalGamesTheme {
        DetailScreen(postId = "1")
    }
}
