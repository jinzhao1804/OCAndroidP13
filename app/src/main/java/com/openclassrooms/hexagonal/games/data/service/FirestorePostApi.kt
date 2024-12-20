package com.openclassrooms.hexagonal.games.data.service

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirestorePostApi @Inject constructor() : PostApi {

    private val firestore = FirebaseFirestore.getInstance()

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private var listenerRegistration: ListenerRegistration? = null

    override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> {
        return channelFlow {
            // Reference to the posts collection, ordered by timestamp in descending order
            val postsRef = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)

            // Attach a real-time listener to the posts collection
            listenerRegistration = postsRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Close the flow if there's an error
                    close(exception)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Map the documents to Post objects
                    val posts = snapshot.documents.mapNotNull { document ->
                        document.toObject(Post::class.java)
                    }

                    // Emit the updated list of posts
                    trySend(posts) // Send data to the flow observer
                } else {
                    // If snapshot is null, emit an empty list
                    trySend(emptyList<Post>())
                }
            }

            // This ensures the flow keeps listening
            awaitClose {
                listenerRegistration?.remove()  // Stop the listener when the flow is closed
            }
        }
    }



    override suspend fun addPost(post: Post) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Reference to the user document in Firestore
            val userRef = firestore.collection("users").document(currentUser.uid)

            try {
                // Fetch the user data from Firestore
                val documentSnapshot = userRef.get().await()

                if (documentSnapshot.exists()) {
                    // Map the document to a User object
                    val user = documentSnapshot.toObject(User::class.java)

                    if (user != null) {
                        // Create the post object with the current user as the author
                        val postWithAuthor = post.copy(author = user)

                        // If there is a photo URL, upload it to Firebase Storage
                        postWithAuthor.photoUrl?.let { photoUriString ->
                            val storageRef = storage.reference.child("post_photos/${UUID.randomUUID()}")
                            val photoUri: Uri = Uri.parse(photoUriString)

                            // Upload the photo to Firebase Storage
                            val uploadTask = storageRef.putFile(photoUri).await()

                            // Get the download URL for the uploaded image
                            val downloadUrl = storageRef.downloadUrl.await()

                            // Now we have the download URL, update the post with it
                            val updatedPost = postWithAuthor.copy(photoUrl = downloadUrl.toString())

                            // Convert the Post object to a Map for Firestore
                            val postMap = mapOf(
                                "id" to updatedPost.id,
                                "title" to updatedPost.title,
                                "description" to updatedPost.description,
                                "photoUrl" to updatedPost.photoUrl,
                                "timestamp" to updatedPost.timestamp,
                                "author" to mapOf(
                                    "id" to updatedPost.author?.id,
                                    "firstname" to updatedPost.author?.firstname,
                                    "lastname" to updatedPost.author?.lastname
                                )
                            )

                            // Add the post to Firestore under the "posts" collection
                            firestore.collection("posts")
                                .add(postMap)
                                .await()

                            // Log success
                            println("Post successfully added to Firestore with photo.")
                        } ?: run {
                            // If no photo URL, just add post without a photo URL
                            val postMap = mapOf(
                                "id" to postWithAuthor.id,
                                "title" to postWithAuthor.title,
                                "description" to postWithAuthor.description,
                                "photoUrl" to null,
                                "timestamp" to postWithAuthor.timestamp,
                                "author" to mapOf(
                                    "id" to postWithAuthor.author?.id,
                                    "firstname" to postWithAuthor.author?.firstname,
                                    "lastname" to postWithAuthor.author?.lastname
                                )
                            )

                            // Add the post to Firestore under the "posts" collection
                            firestore.collection("posts")
                                .add(postMap)
                                .await()

                            // Log success
                            println("Post successfully added to Firestore without photo.")
                        }
                    } else {
                        // Handle case where user data is not available
                        println("User data is not available.")
                    }
                } else {
                    // Handle case where user document doesn't exist
                    println("User document does not exist in Firestore.")
                }
            } catch (e: Exception) {
                // Handle any errors that occur during Firestore or Firebase Storage operations
                println("Error adding post: ${e.message}")
            }
        } else {
            // Handle case where user is not authenticated
            println("No authenticated user found.")
        }
    }
}
