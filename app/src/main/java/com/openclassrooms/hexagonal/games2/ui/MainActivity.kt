package com.openclassrooms.hexagonal.games2.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.openclassrooms.hexagonal.games2.screen.Screen
import com.openclassrooms.hexagonal.games2.screen.accountManagement.AccountManagementScreen
import com.openclassrooms.hexagonal.games2.screen.add.AddScreen
import com.openclassrooms.hexagonal.games2.screen.comment.CommentScreen
import com.openclassrooms.hexagonal.games2.screen.detail.DetailScreen
import com.openclassrooms.hexagonal.games2.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games2.screen.login.LoginScreen
import com.openclassrooms.hexagonal.games2.screen.password.PasswordRecoveryScreen
import com.openclassrooms.hexagonal.games2.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games2.screen.settings.SettingsViewModel
import com.openclassrooms.hexagonal.games2.screen.signup.SignUpScreen
import com.openclassrooms.hexagonal.games2.ui.theme.HexagonalGamesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the application. This activity serves as the entry point and container for the navigation
 * fragment. It handles setting up the toolbar, navigation controller, and action bar behavior.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Abonnement à un topic de notifications
    FirebaseMessaging.getInstance().subscribeToTopic("notification")
      .addOnCompleteListener { task ->
        var msg = "Subscription successful"
        if (!task.isSuccessful) {
          msg = "Subscription failed"
        }
        Log.e("FCM", msg)
      }
    
    setContent {
      val navController = rememberNavController()
      
      HexagonalGamesTheme {
        HexagonalGamesNavHost(navHostController = navController)
      }
    }
  }
  
}

@SuppressLint("NewApi")
@Composable
fun HexagonalGamesNavHost(navHostController: NavHostController) {
  NavHost(
    navController = navHostController,
    startDestination = Screen.Login.route
  ) {

    composable(route = Screen.Login.route) {
      LoginScreen(
        onLoginSuccess = {
          navHostController.navigate(Screen.Homefeed.route)

        },
        onNavigateToSignUp = {
          navHostController.navigate(Screen.SignUp.route)

        },
        onNavigateToPasswordRecovery = {
          navHostController.navigate(Screen.Password.route)

        }
      )
    }
    composable(route = Screen.SignUp.route) {
      SignUpScreen(
        onSignUpSuccess = {
          navHostController.navigate(Screen.Homefeed.route)

        },
        onNavigateToLogin = {
          navHostController.navigate(Screen.Login.route)

        }
      )
    }
    composable(route = Screen.Password.route) {
      PasswordRecoveryScreen(
        onPasswordResetSuccess = {
          navHostController.navigate(Screen.Login.route)

        }

      )
    }
    composable(
      route = "${Screen.Detail.route}/{postId}"
    ) { backStackEntry ->
      val postId = backStackEntry.arguments?.getString("postId") ?: ""
      DetailScreen(
        postId = postId,
        openCommentScreen = {
          // Passing the postId to the CommentScreen
          navHostController.navigate("${Screen.Comment.route}/$postId")
        },
        onBackPress = {
          navHostController.navigateUp()
        }
      )
    }

    // Same for the CommentScreen route, needs to match {postId}
    composable(
      route = "${Screen.Comment.route}/{postId}"
    ) { backStackEntry ->
      val postId = backStackEntry.arguments?.getString("postId") ?: ""
      CommentScreen(
        postId = postId,
        onSaveClicked = {
          navHostController.navigateUp()
        },
        onBackClick = {
          navHostController.navigateUp()
        }
      )
    }

    composable(route = Screen.Homefeed.route) {
      HomefeedScreen(
        onPostClick = { postId ->
          // Navigate to DetailScreen with the postId
          navHostController.navigate("${Screen.Detail.route}/$postId")
        },
        onSettingsClick = {
          navHostController.navigate(Screen.Settings.route)
        },
        onDisconnectClick = {
          navHostController.navigate(Screen.AccountManagement.route)

        },
        onFABClick = {
          navHostController.navigate(Screen.AddPost.route)
        }
      )
    }
    composable(route = Screen.AddPost.route) {
      val context = LocalContext.current

      AddScreen(
        onBackClick = { navHostController.navigateUp() },
        onSaveClick = { navHostController.navigateUp() },
        context = context
      )
    }
    composable(route = Screen.Settings.route) {
      // Use hiltViewModel to inject SettingsViewModel
      val viewModel: SettingsViewModel = hiltViewModel()
      SettingsScreen(
        viewModel = viewModel,
        onBackClick = { navHostController.navigateUp() }
      )

    }
    composable(route = Screen.AccountManagement.route) {
      AccountManagementScreen(
        onLogout = {
          navHostController.navigate(Screen.Login.route)

        },
        onAccountDeleted ={
          navHostController.navigate(Screen.Login.route)

        }
      )
    }


  }
}
