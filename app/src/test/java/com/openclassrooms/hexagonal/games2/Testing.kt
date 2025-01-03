package com.openclassrooms.hexagonal.games2.screen.add

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games2.data.repository.PostRepository
import com.openclassrooms.hexagonal.games2.domain.model.Post
import com.openclassrooms.hexagonal.games2.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import java.util.UUID

@ExperimentalCoroutinesApi
class AddViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var mockPostRepository: PostRepository

    @Mock
    lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    lateinit var mockFirestore: FirebaseFirestore

    @Mock
    lateinit var mockStorage: FirebaseStorage

    @Mock
    lateinit var mockObserver: Observer<Post>

    @Captor
    lateinit var postCaptor: ArgumentCaptor<Post>

    private lateinit var viewModel: AddViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        // Mock Firebase dependencies
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(null) // Simulate no user logged in

        // Initialize the ViewModel with mocks
        viewModel = AddViewModel(mockPostRepository)
    }

    @Test
    suspend fun `test title change updates post`() {
        val newTitle = "New Post Title"

        // Trigger the title change action
        viewModel.onAction(FormEvent.TitleChanged(newTitle))

        // Verify that the post's title is updated
        viewModel.post.collect {
            assert(it.title == newTitle)
        }
    }

    @Test
    suspend fun `test description change updates post`() {
        val newDescription = "New post description"

        // Trigger the description change action
        viewModel.onAction(FormEvent.DescriptionChanged(newDescription))

        // Verify that the post's description is updated
        viewModel.post.collect {
            assert(it.description == newDescription)
        }
    }

    @Test
    fun `test verifyPost returns TitleError when title is empty`() {
        // Create a post with empty title
        viewModel._post.value = Post(
            postId = UUID.randomUUID().toString(),
            title = "",
            description = "Description",
            photoUrl = null,
            timestamp = System.currentTimeMillis(),
            author = null
        )

        // Verify the post
        val error = viewModel.verifyPost()

        // Assert that the error is TitleError
        assert(error is FormError.TitleError)
    }

    @Test
    fun `test addPost with no user logged in`(): Unit = runBlocking {
        // Simulate a scenario where no user is logged in
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Call addPost
        viewModel.addPost()

        // Verify the post is not added (no Firebase interactions)
        verify(mockFirestore, Mockito.never()).collection(any())
    }

    @Test
    fun `test addPost with user logged in and photo URL`(): Unit = runBlocking {
        // Simulate a logged-in user
        val mockUser = User(userId = "123", firstname = "John", lastname = "Doe")
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(Mockito.mock(FirebaseUser::class.java).apply {
            Mockito.`when`(uid).thenReturn("123")
        })

        // Set up mock Firestore and Storage for successful post creation
        val mockUri = Uri.parse("http://example.com/photo.jpg")
        val post = Post(
            postId = UUID.randomUUID().toString(),
            title = "Test Post",
            description = "Description",
            photoUrl = mockUri.toString(),
            timestamp = System.currentTimeMillis(),
            author = mockUser
        )

        // Assign the post to _post to simulate user interaction
        viewModel._post.value = post

        // Call addPost
        viewModel.addPost()

        // Verify the interaction with Firestore to add the post
        verify(mockFirestore.collection("posts")).add(any())
    }

    @Test
    fun `test addPost without photo URL`(): Unit = runBlocking {
        // Simulate a logged-in user
        val mockUser = User(userId = "123", firstname = "John", lastname = "Doe")
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(Mockito.mock(FirebaseUser::class.java).apply {
            Mockito.`when`(uid).thenReturn("123")
        })

        // Create post without a photo URL
        val postWithoutPhoto = Post(
            postId = UUID.randomUUID().toString(),
            title = "Test Post Without Photo",
            description = "Description",
            photoUrl = null,
            timestamp = System.currentTimeMillis(),
            author = mockUser
        )

        // Assign the post to _post
        viewModel._post.value = postWithoutPhoto

        // Call addPost
        viewModel.addPost()

        // Verify the interaction with Firestore to add the post
        verify(mockFirestore.collection("posts")).add(any())
    }
}
