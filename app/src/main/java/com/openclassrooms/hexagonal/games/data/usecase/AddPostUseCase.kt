package com.openclassrooms.hexagonal.games.data.usecase

import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post

class AddPostUseCase (private val postRepository: PostRepository){

suspend fun execute(post: Post) {
    return postRepository.addPost(post)
}

}