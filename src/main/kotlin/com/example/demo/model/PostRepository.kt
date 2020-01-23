package com.example.demo.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.*
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.data.r2dbc.query.Update
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component

@Table("posts")
data class Post(@Id val id: Long? = null,
                @Column("title") val title: String? = null,
                @Column("content") val content: String? = null
)

@Component
class PostRepository(private val client: DatabaseClient) {

    suspend fun count(): Long =
            client.execute("SELECT COUNT(*) FROM posts")
                    .asType<Long>()
                    .fetch()
                    .awaitOne()

    fun findAll(): Flow<Post> =
            client.select()
                    .from(Post::class.java)
                    .fetch()
                    .flow()

    suspend fun findOne(id: Long): Post? =
            client.select()
                    .from(Post::class.java)
                    .matching(Criteria.where("id").`is`(id))
                    .fetch()
                    .awaitOneOrNull()
//            client.execute("SELECT * FROM posts WHERE id = \$1")
//                    .bind(0, id)
//                    .asType<Post>()
//                    .fetch()
//                    .awaitOneOrNull()

    suspend fun deleteById(id: Long): Int =
            client.execute("DELETE FROM posts WHERE id = \$1")
                    .bind(0, id)
                    .fetch()
                    .rowsUpdated()
                    .awaitSingle()

    suspend fun deleteAll(): Int =
            client.delete()
                    .from(Post::class.java)
                    .fetch()
                    .rowsUpdated()
                    .awaitSingle()
    //client.execute("DELETE FROM posts").fetch().rowsUpdated().awaitSingle()

    suspend fun save(post: Post) =
            client.insert()
                    .into(Post::class.java)
                    .using(post)
                    .map { t, u ->
                        //println(t.get("id"))
                        t.get("id", Integer::class.java)?.toLong()
                    }
                    .awaitOne()


    suspend fun update(post: Post): Int =
            client.update()
                    .table("posts")
                    .using(Update.update("title", post.title)
                            .set("content", post.content))
                    .matching(Criteria.where("id").`is`(post.id!!))
                    .fetch()
                    .rowsUpdated()
                    .awaitSingle()

//            client.execute("UPDATE posts SET title = \$2, content = \$3 WHERE id = \$1")
//                    .bind(0, post.id!!)
//                    .bind(1, post.title!!)
//                    .bind(2, post.content!!)
//                    .fetch()
//                    .rowsUpdated()
//                    .awaitSingle()

    suspend fun init() {
        //client.execute().sql("CREATE TABLE IF NOT EXISTS posts (login varchar PRIMARY KEY, firstname varchar, lastname varchar);").await()
        val deletedCount = deleteAll()
        println(" $deletedCount posts deleted!")
        save(Post(title = "My first post title", content = "Content of my first post"))
        save(Post(title = "My second post title", content = "Content of my second post"))
    }
}