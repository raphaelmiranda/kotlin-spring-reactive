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
        println("start data initialization  ...")
        this.databaseClient.execute {
            """ CREATE TABLE posts (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    content VARCHAR(255) NOT NULL
                );
            """ }.then().and({
            this.databaseClient.insert()
                    .into("posts")
                    //.nullValue("id", Long::class.java)
                    .value("title", "First post title")
                    .value("content", "Content of my first post")
                    .then()
                    .log()
                    .thenMany(
                            this.databaseClient.select()
                                    .from("posts")
                                    .orderBy(Sort.Order.by("id").with(Sort.Direction.DESC))
                                    .`as`(Post::class.java)
                                    .fetch()
                                    .all()
                                    .log()
                    )
        }).subscribe(null, null, { println("initialization is done...") })
        runBlocking {
            postRepository.deleteAll()
            postRepository.init()
        }

    }

}

