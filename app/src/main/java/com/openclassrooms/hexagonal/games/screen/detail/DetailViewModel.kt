package com.openclassrooms.hexagonal.games.screen.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    // Fetch Post and Comments
    fun fetchPostAndComments(postId: String) {
        viewModelScope.launch {
            try {
                val fetchedPost = getPostById(postId)
                val fetchedComments = getCommentsByPostId(postId)


                Log.e("fetchedPost" ,"$fetchedPost")

                _post.value = fetchedPost
                _comments.value = fetchedComments

            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error fetching data: ${e.message}")
            }
        }
    }

    private suspend fun getPostById(postId: String): Post? {
        // Fetch post from Firestore
        return try {
            val postSnapshot = firestore.collection("posts")
                .whereEqualTo("postId", postId)  // Query by the field "postId"
                .get()
                .await()

            Log.e("post getpostbyid", "Snapshot size: ${postSnapshot.size()}") // Log the number of documents returned

            // Check if documents exist in the snapshot
            if (postSnapshot.isEmpty) {
                Log.e("post getpostbyid", "No document found with postId: $postId")
                return null
            }

            // If documents exist, convert the first document to Post
            val post = postSnapshot.documents.firstOrNull()?.toObject(Post::class.java)

            if (post == null) {
                Log.e("post getpostbyid", "Failed to convert document to Post")
            }

            post  // Return the Post object or null if conversion fails

        } catch (e: Exception) {
            Log.e("DetailViewModel", "Error fetching post: ${e.message}")
            null
        }
    }


    private suspend fun getCommentsByPostId(postId: String): List<Comment> {
        // Fetch comments for the post from Firestore
        return try {
            val commentsSnapshot = firestore.collection("comments")
                .whereEqualTo("post.postId", postId)  // Query where postId is nested inside the 'posts' field
                .get()
                .await()

            Log.e("getCommentsByPostId", "Snapshot size: ${commentsSnapshot.size()}") // Log the number of documents returned

            commentsSnapshot.documents.mapNotNull {
                it.toObject(Comment::class.java)
            }
        } catch (e: Exception) {
            Log.e("DetailViewModel", "Error fetching comments: ${e.message}")
            emptyList()
        }
    }
}
