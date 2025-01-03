package com.openclassrooms.hexagonal.games2.screen.add

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games2.R
import com.openclassrooms.hexagonal.games2.ui.theme.HexagonalGamesTheme
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
  modifier: Modifier = Modifier,
  viewModel: AddViewModel = hiltViewModel(),
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  context: Context  // Add the context parameter to use it for starting an intent
) {
  val post by viewModel.post.collectAsStateWithLifecycle()
  val error by viewModel.error.collectAsStateWithLifecycle()

  // Remember the launcher for the photo picker (Android 11+ devices)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri: Uri? ->
      // Handle the selected image URI
      viewModel.onPhotoSelected(uri)
    }
  )

  // Handle older Android versions (below Android 11)
  val usePhotoPicker = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.add_fragment_label))
        },
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
    CreatePost(
      modifier = Modifier.padding(contentPadding),
      error = error,
      title = post.title,
      onTitleChanged = { viewModel.onAction(FormEvent.TitleChanged(it)) },
      description = post.description ?: "",
      onDescriptionChanged = { viewModel.onAction(FormEvent.DescriptionChanged(it)) },
      onSaveClicked = {
        viewModel.addPost()
        onSaveClick()
      },
      onPhotoClicked = {
        if (usePhotoPicker) {
          // Launch the new photo picker for Android 11+
          photoPickerLauncher.launch("image/*")
        } else {
          // For older Android versions, use the standard file picker
          val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
          intent.type = "image/*"
          val chooserIntent = Intent.createChooser(intent, "Select an Image")
          context.startActivity(chooserIntent)
        }
      },
      photoUrl = post.photoUrl
    )
  }
}

@Composable
private fun CreatePost(
  modifier: Modifier = Modifier,
  title: String,
  onTitleChanged: (String) -> Unit,
  description: String,
  onDescriptionChanged: (String) -> Unit,
  onSaveClicked: () -> Unit,
  error: FormError?,
  onPhotoClicked: () -> Unit,
  photoUrl: String?
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .padding(16.dp)
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .weight(1f)
        .verticalScroll(scrollState)
    ) {
      OutlinedTextField(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        value = title,
        isError = error is FormError.TitleError,
        onValueChange = { onTitleChanged(it) },
        label = { Text(stringResource(id = R.string.hint_title)) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
        singleLine = true
      )
      if (error is FormError.TitleError) {
        Text(
          text = stringResource(id = error.messageRes),
          color = MaterialTheme.colorScheme.error,
        )
      }

      OutlinedTextField(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        value = description,
        onValueChange = { onDescriptionChanged(it) },
        label = { Text(stringResource(id = R.string.hint_description)) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
      )

      // Display the selected photo if available
      photoUrl?.let {
        Text(
          text = "Photo selected: $it",
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
          modifier = Modifier.padding(top = 16.dp)
        )
      }

      Button(
        modifier = Modifier.padding(top = 16.dp),
        onClick = { onPhotoClicked() }
      ) {
        Text("Add Photo")
      }
    }

    Button(
      enabled = error == null,
      onClick = { onSaveClicked() }
    ) {
      Text(
        modifier = Modifier.padding(8.dp),
        text = stringResource(id = R.string.action_save)
      )
    }
  }
}

@Preview
@Composable
fun CreatePostPreview() {
  HexagonalGamesTheme {
    CreatePost(
      title = "test",
      onTitleChanged = {},
      description = "description",
      onDescriptionChanged = {},
      onSaveClicked = {},
      error = null,
      onPhotoClicked = {},
      photoUrl = null
    )
  }
}
