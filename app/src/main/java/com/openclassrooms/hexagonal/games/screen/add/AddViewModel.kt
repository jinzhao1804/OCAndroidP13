package com.openclassrooms.hexagonal.games.screen.add

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

/**
 * This ViewModel manages data and interactions related to adding new posts in the AddScreen.
 * It utilizes dependency injection to retrieve a PostRepository instance for interacting with post data.
 */
@HiltViewModel
class AddViewModel @Inject constructor(private val postRepository: PostRepository) : ViewModel() {

  // Firestore instance
  private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val storage: FirebaseStorage = FirebaseStorage.getInstance()

  // FirebaseAuth instance
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()

  /**
   * Internal mutable state flow representing the current post being edited.
   */
  private var _post = MutableStateFlow(
    Post(
      id = UUID.randomUUID().toString(),
      title = "",
      description = "",
      photoUrl = null,
      timestamp = System.currentTimeMillis(),
      author = null
    )
  )
  
  /**
   * Public state flow representing the current post being edited.
   * This is immutable for consumers.
   */
  val post: StateFlow<Post>
    get() = _post
  
  /**
   * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
   */
  val error = post.map {
    verifyPost()
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = null,
  )
  
  /**
   * Handles form events like title and description changes.
   *
   * @param formEvent The form event to be processed.
   */
  fun onAction(formEvent: FormEvent) {
    when (formEvent) {
      is FormEvent.DescriptionChanged -> {
        _post.value = _post.value.copy(
          description = formEvent.description
        )
      }
      
      is FormEvent.TitleChanged -> {
        _post.value = _post.value.copy(
          title = formEvent.title
        )
      }

      else -> {}
    }
  }

  // Define the contract for launching the photo picker
  val photoPicker = ActivityResultContracts.GetContent()

  // Callback for the selected image
  fun onPhotoSelected(uri: Uri?) {
    if (uri != null) {
      // Here we set the photo URL in the post
      _post.value = _post.value.copy(photoUrl = uri.toString())
    }
  }

  
  /**
   * Attempts to add the current post to the repository after setting the author.
   *
   * TODO: Implement logic to retrieve the current user.
   */

  fun addPost() {
    // Retrieve the current user dynamically from FirebaseAuth
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
      val firestore = FirebaseFirestore.getInstance()

      // Reference to the user document in Firestore
      val userRef = firestore.collection("users").document(currentUser.uid)

      // Fetch the user data from Firestore
      userRef.get()
        .addOnSuccessListener { documentSnapshot ->
          if (documentSnapshot.exists()) {
            // Map the document to a User object
            val user = documentSnapshot.toObject(User::class.java)

            if (user != null) {

              // Create the post object with the current user as the author
              val postWithAuthor = _post.value.copy(author = user)

              // If there is a photo URL, upload it to Firebase Storage
              postWithAuthor.photoUrl?.let { photoUriString ->
                // Create a reference to the Firebase Storage location
                val storageRef = storage.reference.child("post_photos/${UUID.randomUUID()}")

                val photoUri: Uri = Uri.parse(photoUriString)

                // Upload the photo to Firebase Storage
                val uploadTask = storageRef.putFile(photoUri)

                uploadTask.addOnSuccessListener {
                  // Get the download URL for the uploaded image
                  storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
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
                      .addOnSuccessListener {
                        // Successfully added to Firestore
                        println("Post successfully added to Firestore.")
                      }
                      .addOnFailureListener { e ->
                        // Handle failure
                        println("Error adding post to Firestore: ${e.message}")
                      }
                  }
                }.addOnFailureListener { e ->
                  // Handle failure in uploading image
                  println("Error uploading image to Firebase Storage: ${e.message}")
                }
              } ?: run {
                // If no photo, just add post without a photo URL
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

                // Add the post to Firestore
                firestore.collection("posts")
                  .add(postMap)
                  .addOnSuccessListener {
                    // Successfully added to Firestore
                    println("Post successfully added to Firestore.")
                  }
                  .addOnFailureListener { e ->
                    // Handle failure
                    println("Error adding post to Firestore: ${e.message}")
                  }
              }
            } else {
              // Handle case where user is not authenticated
              println("No authenticated user found.")
            }
          }
        }
    }
  }


  /*
  suspend fun addPost(post: Post){
    postRepository.addPost(post)
  }

  */
  /**
   * Verifies mandatory fields of the post
   * and returns a corresponding FormError if so.
   *
   * @return A FormError.TitleError if title is empty, null otherwise.
   */
  private fun verifyPost(): FormError? {
    return if (_post.value.title.isEmpty()) {
      FormError.TitleError
    } else {
      null
    }
  }
  
}
