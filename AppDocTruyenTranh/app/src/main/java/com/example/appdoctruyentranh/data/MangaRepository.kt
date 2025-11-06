package com.example.appdoctruyentranh.data

import com.example.appdoctruyentranh.model.BannerItem
import com.example.appdoctruyentranh.model.Story
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MangaRepository {

    private val db = FirebaseFirestore.getInstance()

    // ===================== Banner =====================
    suspend fun fetchBanners(): List<BannerItem> {
        val snapshot = db.collection("banners").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val imageUrl = doc.getString("imageUrl") ?: ""
            val id = doc.getLong("id")?.toInt() ?: 0
            BannerItem(id = id, title = title, imageUrl = imageUrl)
        }
    }

    // ===================== Mới cập nhật =====================
    suspend fun fetchNewStories(): List<Story> {
        val snapshot = db.collection("new_updates").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val id = doc.getLong("id")?.toInt() ?: 0
            val imageUrl = doc.getString("imageUrl") ?: ""
            Story(id = id, title = title, imageUrl = imageUrl)
        }
    }

    // ===================== Xem nhiều nhất =====================
    suspend fun fetchMostViewed(): List<Story> {
        val snapshot = db.collection("most_viewed").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val id = doc.getLong("id")?.toInt() ?: 0
            val imageUrl = doc.getString("imageUrl") ?: ""
            Story(id = id, title = title, imageUrl = imageUrl)
        }
    }

    // ===================== Truyện hoàn thành =====================
    suspend fun fetchCompletedStories(): List<Story> {
        val snapshot = db.collection("completed_stories").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val id = doc.getLong("id")?.toInt() ?: 0
            val imageUrl = doc.getString("imageUrl") ?: ""
            Story(id = id, title = title, imageUrl = imageUrl)
        }
    }
}
