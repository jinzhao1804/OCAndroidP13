package com.openclassrooms.hexagonal.games.screen.comment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommentViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()  // Firebase Authentication instance

    // Function to add a comment to Firestore
    fun addComment(postId: String, commentText: String) {
        val currentUser = auth.currentUser

        Log.e("addComment", "Starting comment addition process")

        if (currentUser != null) {
            // Reference to the user document in Firestore
            val userRef = firestore.collection("users").document(currentUser.uid)

            // Launching a coroutine to fetch user data and the post data
            viewModelScope.launch {
                try {
                    Log.e("addComment", "Fetching user data from Firestore")
                    // Fetch the user data from Firestore
                    val documentSnapshot = userRef.get().await()

                    if (documentSnapshot.exists()) {
                        // Map the document to a User object
                        val user = documentSnapshot.toObject(User::class.java)

                        Log.e("addComment", "Fetching post data from Firestore")
                        // Fetch the post data from Firestore
                        val postRef = firestore.collection("posts")
                            .whereEqualTo("postId", postId)
                            .get()
                            .await()

                        // Check if post exists
                        if (postRef.isEmpty) {
                            Log.e("addComment", "Post not found in Firestore for postId: $postId")
                            return@launch
                        }

                        // Map the first document to a Post object
                        val post = postRef.documents.firstOrNull()?.toObject(Post::class.java)

                        if (user != null && post != null) {
                            Log.e("addComment", "Creating comment object")
                            // Create a comment with the current user as the author and the associated post
                            val comment = Comment(
                                commentId = UUID.randomUUID().toString(),
                                comment = commentText,
                                author = user,
                                post = post  // Associate the comment with the post
                            )

                            // Reference to the comments collection in Firestore
                            val commentsRef = firestore.collection("comments")

                            Log.e("addComment", "Adding comment to Firestore")
                            // Add the comment to Firestore and Firestore will automatically generate an ID
                            commentsRef.add(comment)
                                .addOnSuccessListener {
                                    Log.e("addComment", "Comment added successfully")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("addComment", "Error adding comment: ${exception.message}")
                                }
                        } else {
                            Log.e("addComment", "User or Post data is null")
                        }
                    } else {
                        Log.e("addComment", "User data not found in Firestore for userId: ${currentUser.uid}")
                    }
                } catch (e: Exception) {
                    Log.e("addComment", "Error fetching data: ${e.message}")
                }
            }
        } else {
            // Handle case when the user is not logged in
            Log.e("addComment", "No user is signed in")
        }
    }
}
