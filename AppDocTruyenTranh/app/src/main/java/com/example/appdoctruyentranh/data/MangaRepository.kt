package com.example.appdoctruyentranh.data

import com.example.appdoctruyentranh.model.BannerItem
import com.example.appdoctruyentranh.model.Genre
import com.example.appdoctruyentranh.model.Story
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MangaRepository {

    private val db = FirebaseFirestore.getInstance()

    // ===================== Banner =====================
    suspend fun fetchBanners(): List<BannerItem> {
        return try {
            val snapshot = db.collection("banners").get().await()
            snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val imageUrl = doc.getString("imageUrl") ?: ""
                val id = doc.getLong("id")?.toInt() ?: 0
                BannerItem(id = id, title = title, imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            emptyList() // Trả về rỗng nếu lỗi
        }
    }

    // ===================== Mới cập nhật =====================
    suspend fun fetchNewStories(): List<Story> {
        return try {
            val snapshot = db.collection("new_updates").get().await()
            snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val id = doc.getLong("id")?.toInt() ?: 0
                val imageUrl = doc.getString("imageUrl") ?: ""
                Story(id = id, title = title, imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===================== Xem nhiều nhất =====================
    suspend fun fetchMostViewed(): List<Story> {
        return try {
            val snapshot = db.collection("most_viewed").get().await()
            snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val id = doc.getLong("id")?.toInt() ?: 0
                val imageUrl = doc.getString("imageUrl") ?: ""
                Story(id = id, title = title, imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===================== Truyện hoàn thành =====================
    suspend fun fetchCompletedStories(): List<Story> {
        return try {
            val snapshot = db.collection("completed_stories").get().await()
            snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val id = doc.getLong("id")?.toInt() ?: 0
                val imageUrl = doc.getString("imageUrl") ?: ""
                Story(id = id, title = title, imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===================== THỂ LOẠI (10 thể loại mẫu + Firestore) =====================
    suspend fun fetchGenres(): List<Genre> {
        return try {
            val snapshot = db.collection("genres").get().await()
            val genresFromFirestore = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                val name = doc.getString("name") ?: return@mapNotNull null
                val icon = doc.getString("icon") ?: ""
                Genre(id = id, name = name, icon = icon)
            }

            // Nếu Firestore có dữ liệu → dùng
            if (genresFromFirestore.isNotEmpty()) {
                genresFromFirestore
            } else {
                // Nếu rỗng → dùng dữ liệu mẫu
                getDefaultGenres()
            }
        } catch (e: Exception) {
            // Lỗi mạng → vẫn trả về dữ liệu mẫu
            getDefaultGenres()
        }
    }

    // Dữ liệu thể loại mặc định (10 thể loại)
    private fun getDefaultGenres(): List<Genre> {
        return listOf(
            Genre(id = 1, name = "Hành động", icon = "https://img.icons8.com/fluency/48/sword.png"),
            Genre(id = 2, name = "Lãng mạn", icon = "https://img.icons8.com/fluency/48/two-hearts.png"),
            Genre(id = 3, name = "Kinh dị", icon = "https://img.icons8.com/fluency/48/ghost.png"),
            Genre(id = 4, name = "Hài hước", icon = "https://img.icons8.com/fluency/48/smiling.png"),
            Genre(id = 5, name = "Khoa học", icon = "https://img.icons8.com/fluency/48/atom.png"),
            Genre(id = 6, name = "Phiêu lưu", icon = "https://img.icons8.com/fluency/48/compass.png"),
            Genre(id = 7, name = "Tâm lý", icon = "https://img.icons8.com/fluency/48/brain.png"),
            Genre(id = 8, name = "Học đường", icon = "https://img.icons8.com/fluency/48/school.png"),
            Genre(id = 9, name = "Siêu nhiên", icon = "https://img.icons8.com/fluency/48/magic.png"),
            Genre(id = 10, name = "Viễn tưởng", icon = "https://img.icons8.com/fluency/48/rocket.png")
        )
    }
}