package com.openclassrooms.hexagonal.games.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.ad.AddScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.login.LoginScreen
import com.openclassrooms.hexagonal.games.screen.password.PasswordRecoveryScreen
import com.openclassrooms.hexagonal.games.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games.screen.signup.SignUpScreen
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the application. This activity serves as the entry point and container for the navigation
 * fragment. It handles setting up the toolbar, navigation controller, and action bar behavior.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    setContent {
      val navController = rememberNavController()
      
      HexagonalGamesTheme {
        HexagonalGamesNavHost(navHostController = navController)
      }
    }
  }
  
}

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
    composable(route = Screen.Homefeed.route) {
      HomefeedScreen(
        onPostClick = {
          //TODO
        },
        onSettingsClick = {
          navHostController.navigate(Screen.Settings.route)
        },
        onFABClick = {
          navHostController.navigate(Screen.AddPost.route)
        }
      )
    }
    composable(route = Screen.AddPost.route) {
      AddScreen(
        onBackClick = { navHostController.navigateUp() },
        onSaveClick = { navHostController.navigateUp() }
      )
    }
    composable(route = Screen.Settings.route) {
      SettingsScreen(
        onBackClick = { navHostController.navigateUp() }
      )
    }
  }
}
