// File: data/MangaRepository.kt
package com.example.appdoctruyentranh.data

import com.example.appdoctruyentranh.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class MangaRepository {

    private val db = FirebaseFirestore.getInstance()

    // ===================== Lấy banner =====================
    suspend fun fetchBanners(): List<Story> {
        return try {
            val bannerDocs = db.collection("banners").get().await()
            bannerDocs.documents.mapNotNull { bannerDoc ->
                val storyId = bannerDoc.getString("storyId") ?: return@mapNotNull null
                val storySnapshot = db.collection("stories").document(storyId).get().await()
                storySnapshot.toObject(Story::class.java)?.copy(id = storySnapshot.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ===================== Mới cập nhật =====================
    suspend fun fetchNewStories(): List<Story> = fetchStoriesFromRef("new_updates")

    // ===================== Xem nhiều nhất =====================
    suspend fun fetchMostViewed(): List<Story> = fetchStoriesFromRef("most_viewed")

    // ===================== Truyện hoàn thành =====================
    suspend fun fetchCompletedStories(): List<Story> = fetchStoriesFromRef("completed_stories")

    // ===================== Hàm phụ dùng chung =====================
    private suspend fun fetchStoriesFromRef(collection: String): List<Story> {
        return try {
            val refDocs = db.collection(collection).get().await()
            refDocs.documents.mapNotNull { doc ->
                val storyId = doc.getString("storyId") ?: return@mapNotNull null
                val storySnapshot = db.collection("stories").document(storyId).get().await()
                storySnapshot.toObject(Story::class.java)?.copy(id = storySnapshot.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ===================== Chi tiết truyện =====================
    suspend fun fetchMangaDetail(mangaId: String): Story? {
        return try {
            val docRef = db.collection("stories").document(mangaId)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) return null

            val story = snapshot.toObject(Story::class.java) ?: return null

            val chaptersSnapshot = docRef.collection("chapters").get().await()
            val chapters = chaptersSnapshot.documents.mapNotNull {
                it.toObject(Chapter::class.java)
            }.sortedByDescending { it.number }

            story.copy(id = mangaId, chapters = chapters)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ===================== TÌM KIẾM TRUYỆN – ĐÃ SỬA ĐÚNG VỊ TRÍ =====================
    suspend fun searchStories(query: String, genreId: Int? = null): List<Story> {
        return try {
            var firestoreQuery: Query = db.collection("stories")

            if (genreId != null) {
                firestoreQuery = firestoreQuery.whereArrayContains("genreIds", genreId)
            }

            val snapshot = firestoreQuery.get().await()
            val lowerQuery = query.trim().lowercase()

            snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(Story::class.java)?.copy(id = doc.id)
                }
                .filter { story ->
                    lowerQuery.isBlank() ||
                            story.title.lowercase().contains(lowerQuery) ||
                            story.author.lowercase().contains(lowerQuery)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ===================== LẤY DANH SÁCH THỂ LOẠI =====================
    suspend fun getGenres(): List<Genre> {
        return try {
            val snapshot = db.collection("genres").get().await()
            snapshot.documents.mapNotNull { doc ->
                val idString = doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                Genre(id = idString.toInt(), name = name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}