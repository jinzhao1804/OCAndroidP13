package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

/**
 * This class represents a User data object. It holds basic information about a user, including
 * their ID, first name, and last name. The class implements Serializable to allow for potential
 * serialization needs.
 */


data class User(
  val id: String = "",         // Default value for id
  val firstname: String = "",  // Default value for firstname
  val lastname: String = ""    // Default value for lastname
) : Serializable {
}
