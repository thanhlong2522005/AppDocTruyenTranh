// File: data/GenreRepository.kt
package com.example.appdoctruyentranh.data

import com.example.appdoctruyentranh.model.Genre
import com.example.appdoctruyentranh.model.Story
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GenreRepository {
    private val db = FirebaseFirestore.getInstance()


    // LẤY DANH SÁCH THỂ LOẠI (DÙNG CHO GenreViewModel)
    suspend fun getGenres(): List<Genre> {
        return try {
            val snapshot = db.collection("genres").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Genre::class.java)?.apply {
                    id = doc.id.toIntOrNull() ?: 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // LẤY TOÀN BỘ TRUYỆN
    suspend fun getAllStories(): List<Story> {
        return try {
            val snapshot = db.collection("stories").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Story::class.java)?.apply {
                    id = doc.id
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // LẤY TRUYỆN THEO THỂ LOẠI
    suspend fun getStoriesByGenre(genreId: Int): List<Story> {
        return try {
            val snapshot = db.collection("stories")
                .whereArrayContains("genreIds", genreId)

                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Story::class.java)?.apply {
                    id = doc.id
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    // Lấy tên thể loại theo ID
    suspend fun getGenreNameById(id: Int): String {
        val doc = db.collection("genres").document(id.toString()).get().await()
        return doc.getString("name") ?: ""
    }

    // Lấy truyện theo TÊN thể loại
    suspend fun getStoriesByGenreName(name: String): List<Story> {
        val snapshot = db.collection("stories")
            .whereArrayContains("genres", name)
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(Story::class.java)?.copy(id = it.id)
        }
    }

}