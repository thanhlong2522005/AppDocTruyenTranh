package com.example.appdoctruyentranh.data


import com.example.appdoctruyentranh.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.SetOptions
import java.util.Date // ⭐️ CẦN THIẾT cho Comment Data Class
import kotlin.io.path.exists


private data class UserProfile(
    val name: String = "",
    val imageUrl: String = ""
)

data class UserProfileData(
    val name: String = "",
    val email: String = "",
    val imageUrl: String = "",
    val gender: String = ""
)

class MangaRepository {

    private val db = FirebaseFirestore.getInstance()
    // ... (các hàm khác giữ nguyên)

    // ===================== CHỨC NĂNG BÌNH LUẬN MỚI =====================

    /**
     * Tải danh sách 50 Comments mới nhất cho một Story.
     */
    suspend fun fetchComments(storyId: String): List<Comment> {
        return try {
            // Bước 1: Lấy danh sách các document bình luận
            val commentsSnapshot = db.collection("stories").document(storyId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get().await()

            val comments = commentsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)?.copy(id = doc.id)
            }

            if (comments.isEmpty()) {
                return emptyList() // Không có bình luận thì trả về luôn
            }

            // Bước 2: Thu thập tất cả các userId duy nhất từ danh sách bình luận
            val userIds = comments.map { it.userId }.distinct().filter { it.isNotEmpty() }

            if (userIds.isEmpty()) {
                return comments // Trả về bình luận mà không có thông tin user nếu không có userId nào
            }

            // Bước 3: Lấy thông tin của tất cả user chỉ với một truy vấn "whereIn"
            // Giả định bạn có collection "users" với document ID là UID của người dùng
            val usersSnapshot = db.collection("users").whereIn(FieldPath.documentId(), userIds).get().await()

            // Tạo một map để tra cứu nhanh thông tin user từ userId: Map<String, UserProfile>
            val userProfilesMap = usersSnapshot.documents.associate { doc ->
                doc.id to (doc.toObject(UserProfile::class.java) ?: UserProfile())
            }

            // Bước 4: Ghép thông tin người dùng vào mỗi bình luận
            comments.forEach { comment ->
                val profile = userProfilesMap[comment.userId]
                comment.userInfo = UserInfo(
                    name = profile?.name ?: "Người dùng ẩn danh",
                    avatarUrl = profile?.imageUrl ?: ""
                )
            }

            comments
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    /**
     * Gửi một Comment mới lên Firestore.
     */
    suspend fun postComment(storyId: String, comment: Comment) {
        db.collection("stories").document(storyId)
            .collection("comments")
            .add(comment) // Firestore sẽ tự động tạo ID và ServerTimestamp
            .await()
    }

    // ... (các hàm cũ từ đây)

    suspend fun createUserInFirestore(user: FirebaseUser, name: String) {
        // 1. Lấy một avatar ngẫu nhiên từ collection "default_avatars"
        val avatarsSnapshot = db.collection("default_avatars").get().await()
        val defaultAvatarUrl = avatarsSnapshot.documents.randomOrNull()?.getString("imageUrl") ?: ""

        // 2. Tạo đối tượng người dùng mới
        val newUser = hashMapOf(
            "name" to name, // Sử dụng tên người dùng nhập vào
            "email" to user.email,
            "imageUrl" to defaultAvatarUrl, // Gán avatar ngẫu nhiên
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // 3. Lưu vào collection "users" với ID là UID của người dùng
        db.collection("users").document(user.uid).set(newUser).await()
    }

    suspend fun getUserProfile(userId: String): UserProfileData? {
        return try {
            val document = db.collection("users").document(userId).get().await()
            document.toObject(UserProfileData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    suspend fun deleteComment(storyId: String, commentId: String) {
        try {
            db.collection("stories").document(storyId)
                .collection("comments").document(commentId)
                .delete()
                .await() // Dùng await để đảm bảo hoạt động bất đồng bộ hoàn tất
        } catch (e: Exception) {
            // Ném ra ngoại lệ để ViewModel có thể bắt và xử lý lỗi
            throw Exception("Lỗi khi xóa bình luận trên Firestore: ${e.message}")
        }
    }

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
    suspend fun deleteStory(storyId: String) {
        try {
            db.collection("stories").document(storyId).delete().await()
            // Có thể xóa chapter con hoặc các liên kết khác nếu muốn
        } catch (e: Exception) {
            e.printStackTrace()
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
    suspend fun fetchAllStories(): List<Story> {
        return try {
            val snapshot = db.collection("stories").get().await()

            snapshot.documents.mapNotNull { doc ->
                val story = doc.toObject(Story::class.java)?.copy(id = doc.id)
                if (story != null) {
                    val chapters = fetchChaptersForStory(doc.id)
                    story.copy(chapters = chapters)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
    // ⭐ Lấy truyện được yêu thích
    suspend fun fetchFavorites(): List<Story> = fetchStoriesFromRef("favorites_list")

    // ⭐ Lấy truyện trending
    suspend fun fetchTrending(): List<Story> = fetchStoriesFromRef("trending_list")

    // ⭐ Lấy truyện mới ra mắt
    suspend fun fetchNewReleases(): List<Story> = fetchStoriesFromRef("new_releases")


    // ===================== Hàm phụ dùng chung (Đã sửa) =====================
    suspend fun fetchStoriesFromRef(collection: String): List<Story> {
        return try {
            println("Đang tải từ collection: $collection")
            val refDocs = db.collection(collection).get().await()
            println("Số document tìm được trong $collection: ${refDocs.size()}")

            refDocs.documents.mapNotNull { doc ->
                val storyId = doc.getString("storyId") ?: run {
                    println("Document không có storyId: ${doc.id}")
                    return@mapNotNull null
                }
                println("Tìm thấy storyId: $storyId")

                val storySnapshot = db.collection("stories").document(storyId).get().await()
                if (!storySnapshot.exists()) {
                    println("Story không tồn tại: $storyId")
                    return@mapNotNull null
                }

                // IN RA DỮ LIỆU GỐC ĐỂ DEBUG
                println("DỮ LIỆU GỐC của $storyId: ${storySnapshot.data}")

                val story = storySnapshot.toObject(Story::class.java)?.copy(id = storySnapshot.id)
                if (story == null) {
                    println("KHÔNG PARSE ĐƯỢC Story: $storyId")
                    println("Lý do: Có field không map được trong model")
                    return@mapNotNull null
                }

                println("PARSE THÀNH CÔNG: $story")

                val chapters = fetchChaptersForStory(storyId)
                story.copy(chapters = chapters)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Lỗi khi tải $collection: ${e.message}")
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
    suspend fun updateRating(mangaId: String, userId: String, newRating: Float) {
        require(newRating in 1f..5f) { "Rating phải từ 1 đến 5" }

        try {
            val storyRef = db.collection("stories").document(mangaId)
            val userRatingRef = storyRef.collection("ratings").document(userId)

            db.runTransaction { transaction ->
                val storySnap = transaction.get(storyRef)
                if (!storySnap.exists()) {
                    throw IllegalStateException("Truyện không tồn tại: $mangaId")
                }

                val userSnap = transaction.get(userRatingRef)

                // Lấy giá trị hiện tại (phải dùng getLong/getDouble cẩn thận)
                var currentTotal = storySnap.get("ratingTotal") as? Number
                var currentCount = storySnap.get("ratingCount") as? Number

                // Nếu chưa có field nào → khởi tạo = 0
                if (currentTotal == null || currentCount == null) {
                    currentTotal = 0.0
                    currentCount = 0
                }

                val oldRating = if (userSnap.exists()) {
                    userSnap.get("rating") as? Number
                } else null

                // Tính toán mới
                val (newTotal, newCount) = if (oldRating == null) {
                    // Người dùng chấm lần đầu
                    currentTotal.toDouble() + newRating.toDouble() to currentCount.toLong() + 1
                } else {
                    // Người dùng sửa đánh giá
                    currentTotal.toDouble() - oldRating.toDouble() + newRating.toDouble() to currentCount.toLong()
                }

                val newAverage = if (newCount > 0) newTotal / newCount else 0.0

                // Cập nhật document Story
                transaction.update(storyRef, mapOf(
                    "ratingTotal" to newTotal,
                    "ratingCount" to newCount.toLong(),
                    "rating"      to String.format("%.2f", newAverage).toDouble() // giữ 2 chữ số thập phân
                ))

                // Lưu đánh giá riêng của user (dùng set với merge để tránh xóa field khác)
                transaction.set(
                    userRatingRef,
                    mapOf(
                        "rating"     to newRating,
                        "timestamp"  to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge() // Quan trọng: chỉ cập nhật field này, không xóa field cũ
                )
            }.await()

        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Ném ra để ViewModel biết và xử lý lỗi
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