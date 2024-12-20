package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

data class Comment (
    val id: String = "",
    val comment: String = "",
    val author: User? = null
) : Serializable
