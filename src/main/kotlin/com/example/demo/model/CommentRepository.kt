package com.example.demo.model

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component


@Table("comments")
data class Comment(@Id val id: Long? = null,
                   @Column("content") val content: String? = null,
                   @Column("post_id") val postId: Long? = null)

@Component
class CommentRepository(private val client: DatabaseClient) {
    suspend fun save(comment: Comment) =
            client.insert().into<Comment>().table("comments").using(comment).await()

    suspend fun countByPostId(postId: Long): Long =
            client.execute("SELECT COUNT(*) FROM comments WHERE post_id = \$1")
                    .bind(0, postId)
                    .asType<Long>()
                    .fetch()
                    .awaitOne()

    fun findByPostId(postId: Long): Flow<Comment> =
            client.execute("SELECT * FROM comments WHERE post_id = \$1")
                    .bind(0, postId).asType<Comment>()
                    .fetch()
                    .flow()
}
