package com.example.demo.handlers

import com.example.demo.model.Post
import com.example.demo.model.PostRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import java.net.URI

@Component
class PostHandler(private val posts: PostRepository) {

    suspend fun all(req: ServerRequest): ServerResponse {
        return ServerResponse.ok().bodyAndAwait(this.posts.findAll())
    }

    suspend fun create(req: ServerRequest): ServerResponse {
        val body = req.awaitBody<Post>()
        val createdPost = this.posts.save(body)
        return ServerResponse.created(URI.create("/posts/$createdPost")).buildAndAwait()
    }

    suspend fun get(req: ServerRequest): ServerResponse {
        println("path variable::${req.pathVariable("id")}")
        val foundPost = this.posts.findOne(req.pathVariable("id").toLong())
        println("found post:$foundPost")
        return when {
            foundPost != null -> ServerResponse.ok().bodyValueAndAwait(foundPost)
            else -> ServerResponse.notFound().buildAndAwait()
        }
    }

    suspend fun update(req: ServerRequest): ServerResponse {
        val foundPost = this.posts.findOne(req.pathVariable("id").toLong())
        val body = req.awaitBody<Post>()
        return when {
            foundPost != null -> {
                this.posts.update(foundPost.copy(title = body.title, content = body.content))
                ServerResponse.noContent().buildAndAwait()
            }
            else -> ServerResponse.notFound().buildAndAwait()
        }


    }

    suspend fun delete(req: ServerRequest): ServerResponse {
        val deletedCount = this.posts.deleteById(req.pathVariable("id").toLong())
        println("$deletedCount posts deleted")
        return ServerResponse.noContent().buildAndAwait()
    }
}