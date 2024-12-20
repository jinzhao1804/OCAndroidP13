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
              // Create a post with the current user's details
              val postWithAuthor = _post.value.copy(author = user)

              // Proceed to save the post, etc.
              val postMap = mapOf(
                "id" to postWithAuthor.id,
                "title" to postWithAuthor.title,
                "description" to postWithAuthor.description,
                "photoUrl" to postWithAuthor.photoUrl,
                "timestamp" to postWithAuthor.timestamp,
                "author" to mapOf(
                  "id" to postWithAuthor.author?.id,
                  "firstname" to postWithAuthor.author?.firstname,
                  "lastname" to postWithAuthor.author?.lastname
                )
              )

              // Save post to Firestore
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
            } else {
              // Handle the case where the user object is null
              println("User data is not available in Firestore.")
            }
          } else {
            // Handle the case where the document doesn't exist
            println("User document does not exist in Firestore.")
          }
        }
        .addOnFailureListener { exception ->
          // Handle any errors that occurred while fetching user data
          println("Error fetching user data from Firestore: ${exception.message}")
        }
    } else {
      // Handle the case where there is no authenticated user
      println("No authenticated user found.")
    }
  }


  
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
