package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

data class Post(
  val postId: String = "",             // Default value for id
  val title: String = "",          // Default value for title
  val description: String? = "",   // Default value for description
  val photoUrl: String? = "",      // Default value for photoUrl
  val timestamp: Long = 0,         // Default value for timestamp
  val author: User? = null         // Default value for author
) : Serializable
