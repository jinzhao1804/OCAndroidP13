package com.openclassrooms.hexagonal.games2.data.usecase

import com.openclassrooms.hexagonal.games2.data.repository.PostRepository
import com.openclassrooms.hexagonal.games2.domain.model.Post

class AddPostUseCase (private val postRepository: PostRepository){

suspend fun execute(post: Post) {
    return postRepository.addPost(post)
}

}