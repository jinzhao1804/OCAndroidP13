package com.openclassrooms.hexagonal.games.screen.comment

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.screen.add.FormError
import com.openclassrooms.hexagonal.games.screen.add.FormEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    postId: String,
    onSaveClicked: () -> Unit,
    onBackClick: ()-> Unit
) {
    val viewModel: CommentViewModel = hiltViewModel()
    var commentText by remember { mutableStateOf("") }

    val error by viewModel.error.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Comment") },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.contentDescription_go_back)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // TextField for entering the comment
            TextField(
                value = commentText,
                onValueChange = { newText ->
                    commentText = newText // Update commentText directly
                    // Pass the updated text to the ViewModel's onAction method
                    viewModel.onAction(FormEvent.ContentChanged(newText))
                },
                modifier = Modifier.fillMaxWidth(),
                isError = error is FormError.TitleError,
                label = { Text("Enter your comment") },
                placeholder = { Text("Write a comment...") },
                singleLine = false,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                maxLines = 5
            )
            if (error is FormError.ContentError) {
                Text(
                    text = stringResource(id = (error as FormError.ContentError).messageRes),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to submit the comment
            Button(
                onClick = {
                    if (commentText.isNotEmpty()) {
                        Log.e("comment adding", "up to here")
                        viewModel.addComment(postId, commentText)
                        Log.e("comment called adding", "after addcomment called")
                        onSaveClicked()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = commentText.isNotEmpty() && error == null // Disable button if there's an error or the comment is empty
            ) {
                Text("Submit")
            }
        }
    }
}

