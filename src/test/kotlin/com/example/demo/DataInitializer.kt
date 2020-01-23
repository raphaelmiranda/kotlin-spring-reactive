package com.example.demo

import com.example.demo.model.Post
import com.example.demo.model.PostRepository
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Sort
import org.springframework.data.history.RevisionSort
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class DataInitializer(private val databaseClient: DatabaseClient, private val postRepository: PostRepository) {

    @EventListener(value = [ApplicationReadyEvent::class])
    fun init() {
        runBlocking {
            val deletedCount = postRepository.deleteAll()
            println(" $deletedCount posts deleted!")
            postRepository.save(Post(title = "My first post title", content = "Content of my first post"))
            postRepository.save(Post(title = "My second post title", content = "Content of my second post"))
        }
    }

}

