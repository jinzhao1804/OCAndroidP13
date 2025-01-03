package com.openclassrooms.hexagonal.games2.domain.model

import java.io.Serializable

data class Comment(
    val timestamp: Long = 0,
    val commentId: String = "",
    val comment: String = "",
    val author: User? = null,
    val post: Post? = null
) : Serializable
