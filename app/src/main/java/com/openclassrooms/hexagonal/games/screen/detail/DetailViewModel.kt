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
                .document(postId)
                .get()
                .await()

            postSnapshot.toObject(Post::class.java)
        } catch (e: Exception) {
            Log.e("DetailViewModel", "Error fetching post: ${e.message}")
            null
        }
    }

    private suspend fun getCommentsByPostId(postId: String): List<Comment> {
        // Fetch comments for the post from Firestore
        return try {
            val commentsSnapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId) // Assuming you store postId in each comment
                .get()
                .await()

            commentsSnapshot.documents.mapNotNull {
                it.toObject(Comment::class.java)
            }
        } catch (e: Exception) {
            Log.e("DetailViewModel", "Error fetching comments: ${e.message}")
            emptyList()
        }
    }
}
