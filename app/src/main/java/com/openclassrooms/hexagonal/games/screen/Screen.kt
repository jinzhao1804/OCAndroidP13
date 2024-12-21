package com.openclassrooms.hexagonal.games.screen

import androidx.navigation.NamedNavArgument

sealed class Screen(
  val route: String,
  val navArguments: List<NamedNavArgument> = emptyList()
) {
  data object Homefeed : Screen("homefeed")
  
  data object AddPost : Screen("addPost")
  
  data object Settings : Screen("settings")

  data object Login : Screen("login")
  data object SignUp : Screen("signup")
  data object Password : Screen("password")
  data object AccountManagement : Screen("disconnect")
  data object Detail : Screen("detail")
  data object Comment : Screen("comment")



}