package com.openclassrooms.hexagonal.games.screen.comment

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    postId: String,
    onSaveClicked: () -> Unit
) {
    val viewModel: CommentViewModel = hiltViewModel()
    var commentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Comment") }
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
                onValueChange = { commentText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter your comment") },
                placeholder = { Text("Write a comment...") },
                singleLine = false,
                maxLines = 5
            )

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
                enabled = commentText.isNotEmpty()
            ) {
                Text("Submit")
            }
        }
    }
}

