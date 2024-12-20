package com.openclassrooms.hexagonal.games.screen.accountManagement

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AccountManagementScreen(
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var errorMessage by remember { mutableStateOf("") }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    if (user != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Account Management", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(32.dp))

            // Display User Email
            Text("Welcome, ${user.email}")

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = {
                    auth.signOut()
                    onLogout() // Trigger logout action
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Account Button
            Button(
                onClick = {
                    // Show the confirmation dialog
                    showDeleteConfirmationDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Account")
            }

            // Display error message if any
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage, color = Color.Red)
            }
        }

        // Confirmation Dialog for account deletion
        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmationDialog = false // Close dialog on dismiss
                },
                title = { Text("Confirm Account Deletion") },
                text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            user.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        onAccountDeleted() // Trigger account deleted action
                                    } else {
                                        errorMessage = task.exception?.message ?: "Unknown error"
                                    }
                                }
                            showDeleteConfirmationDialog = false // Close dialog
                        }
                    ) {
                        Text("Yes, Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmationDialog = false // Close dialog if cancelled
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    } else {
        // If the user is not logged in, show a message
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No user is logged in.")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewAccountManagementScreen() {
    AccountManagementScreen(
        onLogout = { /* Handle logout */ },
        onAccountDeleted = { /* Handle account deletion */ }
    )
}
