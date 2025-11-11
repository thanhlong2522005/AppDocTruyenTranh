package com.example.appdoctruyentranh.data


import com.example.appdoctruyentranh.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateListOf // Cần thiết nếu Chapter model dùng mutableStateListOf
import com.google.firebase.firestore.FieldValue
class MangaRepository {

    private val db = FirebaseFirestore.getInstance()
    //  LƯU LỊCH SỬ ĐỌC
    suspend fun saveReadHistory(userId: String, storyId: String, chapterId: String) {
        val historyRef = db.collection("read_history")
            .document(userId)
            .collection("reads")
            .document(storyId) // Sử dụng storyId làm Document ID để chỉ lưu 1 bản ghi cho mỗi truyện

        // Lưu thông tin chương cuối cùng và timestamp
        val data = hashMapOf(
            "storyId" to storyId,
            "chapterId" to chapterId,
            "timestamp" to FieldValue.serverTimestamp() // Thời gian đọc gần nhất
        )

        historyRef.set(data).await()
    }
    suspend fun toggleFavoriteStatus(userId: String, storyId: String, isFavorite: Boolean) {
        val userFavoritesRef = db.collection("favorites")
            .document(userId)
            .collection("stories")
            .document(storyId)

        // 1. Cập nhật lượt thích (likesCount) trên Story Document
        db.collection("stories").document(storyId)
            .update("likes", if (isFavorite) FieldValue.increment(1) else FieldValue.increment(-1))
            .await()

        // 2. Thêm hoặc xóa document yêu thích của người dùng
        if (isFavorite) {
            // Thêm vào danh sách yêu thích
            userFavoritesRef.set(hashMapOf("timestamp" to FieldValue.serverTimestamp())).await()
        } else {
            // Xóa khỏi danh sách yêu thích
            userFavoritesRef.delete().await()
        }
    }
    /**
     * Tải danh sách Chapters chi tiết cho một Story.
     * Dùng cho HomeScreen (để tính totalChapters) và MangaDetailScreen.
     */
    private suspend fun fetchChaptersForStory(storyId: String): MutableList<Chapter> {
        return try {
            val chaptersSnapshot = db.collection("stories").document(storyId)
                .collection("chapters")
                .get().await()

            chaptersSnapshot.documents.mapNotNull { doc ->
                // Đảm bảo Chapter model có thể deserialize từ Firebase
                doc.toObject(Chapter::class.java)
            }.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            mutableStateListOf() // Trả về danh sách rỗng để tránh lỗi
        }
    }

    // ===================== Lấy banner (Đã sửa) =====================
    suspend fun fetchBanners(): List<Story> {
        return try {
            val bannerDocs = db.collection("banners").get().await()
            bannerDocs.documents.mapNotNull { bannerDoc ->
                val storyId = bannerDoc.getString("storyId") ?: return@mapNotNull null
                val storySnapshot = db.collection("stories").document(storyId).get().await()

                val story = storySnapshot.toObject(Story::class.java)?.copy(id = storySnapshot.id)
                if (story == null) return@mapNotNull null

                // ⭐️ Tải chapters cho Story này
                val chapters = fetchChaptersForStory(storyId)

                story.copy(chapters = chapters) // Gán chapters đã tải vào Story
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

    // ===================== Hàm phụ dùng chung (Đã sửa) =====================
    private suspend fun fetchStoriesFromRef(collection: String): List<Story> {
        return try {
            val refDocs = db.collection(collection).get().await()
            refDocs.documents.mapNotNull { doc ->
                val storyId = doc.getString("storyId") ?: return@mapNotNull null
                val storySnapshot = db.collection("stories").document(storyId).get().await()

                val story = storySnapshot.toObject(Story::class.java)?.copy(id = storySnapshot.id)
                if (story == null) return@mapNotNull null

                // ⭐️ Tải chapters cho Story này
                val chapters = fetchChaptersForStory(storyId)

                story.copy(chapters = chapters) // Cập nhật chapters
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ===================== Chi tiết truyện (Đã sửa toMutableList()) =====================
    suspend fun fetchMangaDetail(mangaId: String): Story? {
        return try {
            val docRef = db.collection("stories").document(mangaId)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) return null

            val story = snapshot.toObject(Story::class.java) ?: return null

            val chaptersSnapshot = docRef.collection("chapters").get().await()
            val chapters = chaptersSnapshot.documents.mapNotNull {
                it.toObject(Chapter::class.java)
            }.sortedByDescending { it.number }.toMutableList() // Đảm bảo là MutableList

            story.copy(id = mangaId, chapters = chapters)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ===================== TÌM KIẾM TRUYỆN =====================
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
                    val story = doc.toObject(Story::class.java)?.copy(id = doc.id)
                    if (story == null) return@mapNotNull null

                    // ⭐️ Tải chapters (hoặc ít nhất là số đếm) cho kết quả tìm kiếm
                    val chapters = fetchChaptersForStory(doc.id)
                    story.copy(chapters = chapters)
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

    // ===================== LẤY DANH SÁCH THỂ LOẠI (Giữ nguyên) =====================
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


    // ===================== Tăng lượt thích =====================
    suspend fun toggleLike(mangaId: String, isLiked: Boolean) {
        try {
            val storyRef = db.collection("stories").document(mangaId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(storyRef)
                val currentLikes = snapshot.getDouble("likesCount") ?: 0.0
                val newLikes = if (isLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0.0)
                transaction.update(storyRef, "likesCount", newLikes)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun checkFavoriteStatus(userId: String, storyId: String): Boolean {
        return try {
            val docRef = db.collection("favorites")
                .document(userId)
                .collection("stories")
                .document(storyId)

            val snapshot = docRef.get().await()
            return snapshot.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ===================== Tăng lượt xem =====================
    suspend fun increaseViews(mangaId: String) {
        try {
            val storyRef = db.collection("stories").document(mangaId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(storyRef)
                val currentViews = snapshot.getDouble("viewsCount") ?: 0.0
                transaction.update(storyRef, "viewsCount", currentViews + 1)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ===================== Cập nhật rating (điểm sao) =====================
    suspend fun updateRating(mangaId: String, newRating: Float) {
        try {
            val storyRef = db.collection("stories").document(mangaId)
            storyRef.update("rating", newRating).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun incrementViewCount(mangaId: String, currentViews: Int) {
        // Có thể dùng Coroutines và Task API để xử lý
        db.collection("stories")
            .document(mangaId)
            .update("views", currentViews + 1)
            .await() // Sử dụng await() nếu bạn dùng thư viện kotlinx-coroutines-play-services
    }
}

