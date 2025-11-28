package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository
import com.example.appdoctruyentranh.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.appdoctruyentranh.model.Comment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth

class MangaDetailViewModel : ViewModel() {

    private val repository = MangaRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _mangaDetail = MutableStateFlow<Story?>(null)
    val mangaDetail: StateFlow<Story?> get() = _mangaDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> get() = _comments

    private val _isCommentLoading = MutableStateFlow(false)
    val isCommentLoading: StateFlow<Boolean> get() = _isCommentLoading

    private val _commentError = MutableStateFlow<String?>(null)
    val commentError: StateFlow<String?> get() = _commentError

    // ===========================================================
// 🔹 TẢI BÌNH LUẬN
// ===========================================================
    fun loadComments(mangaId: String) {
        viewModelScope.launch {
            _isCommentLoading.value = true
            _commentError.value = null
            try {
                val result = repository.fetchComments(mangaId) // Gọi Repository
                _comments.value = result
            } catch (e: Exception) {
                _commentError.value = "Lỗi tải bình luận: ${e.message}"
                _comments.value = emptyList()
            } finally {
                _isCommentLoading.value = false
            }
        }
    }

    // ===========================================================
// 💬 GỬI BÌNH LUẬN MỚI
// ===========================================================
    fun postComment(storyId: String, content: String, user: FirebaseUser?) {
        if (user == null) {
            _commentError.value = "Vui lòng đăng nhập để bình luận."
            return
        }
        if (content.isBlank()) return // Bỏ qua nếu nội dung rỗng

        viewModelScope.launch {
            // Thay đổi ở đây: Chỉ cần tạo Comment với userId và content.
            // Các thông tin khác như tên và ảnh sẽ được Repository xử lý khi TẢI bình luận.
            val newComment = Comment(
                userId = user.uid,
                content = content.trim()
            )
            try {
                // 1. Gửi lên Firestore qua Repository
                repository.postComment(storyId, newComment)

                // 2. Tải lại danh sách để UI hiển thị bình luận mới nhất
                // Hàm loadComments giờ đây sẽ tự động lấy cả thông tin người dùng.
                loadComments(storyId)

            } catch (e: Exception) {
                _commentError.value = "Gửi bình luận thất bại: ${e.message}"
            }
        }
    }

    // ===========================================================
// 🗑️ XÓA BÌNH LUẬN (CHO ADMIN)
// ===========================================================
    fun deleteComment(storyId: String, commentId: String) {
        viewModelScope.launch {
            try {
                // 1. Gọi repository để xóa trên Firestore
                repository.deleteComment(storyId, commentId)

                // 2. Cập nhật lại UI bằng cách xóa bình luận khỏi StateFlow
                val updatedComments = _comments.value.filterNot { it.id == commentId }
                _comments.value = updatedComments

            } catch (e: Exception) {
                // Xử lý nếu có lỗi
                _commentError.value = "Lỗi khi xóa bình luận: ${e.message}"
            }
        }
    }


    // ===========================================================
    // 🔹 TẢI DỮ LIỆU CHI TIẾT TRUYỆN
    // ===========================================================
    fun loadMangaDetail(mangaId: String) {
        val userId = auth.currentUser?.uid
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var story = repository.fetchMangaDetail(mangaId)

                if (story != null && userId != null) {
                    // ⭐️ BỔ SUNG: KIỂM TRA TRẠNG THÁI THÍCH TỪ FIRESTORE
                    val isFavorite = repository.checkFavoriteStatus(userId, mangaId)

                    // Cập nhật trạng thái isLiked vào Story model
                    story = story.copy(isLiked = isFavorite)
                }

                // Cập nhật StateFlow
                _mangaDetail.value = story

            } catch (e: Exception) {
                // ...
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ===========================================================
    // ❤️ XỬ LÝ LIKE (CÓ THỂ BẤM NHIỀU LẦN)
    // ===========================================================
    fun toggleLike() {
        val current = _mangaDetail.value ?: return
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Xử lý trường hợp người dùng chưa đăng nhập
            _error.value = "Vui lòng đăng nhập để thực hiện chức năng này."
            return
        }

        val newLikedState = !current.isLiked
        val newLikesCount = if (newLikedState) current.likes + 1 else current.likes - 1

        val updated = current.copy(
            isLiked = newLikedState,
            likes = newLikesCount.coerceAtLeast(0)
        )
        _mangaDetail.value = updated

        // GỌI HÀM REPOSITORY MỚI VỚI userId
        viewModelScope.launch {
            try {
                // ⭐️ CHÚ Ý: Cần hàm toggleFavoriteStatus trong Repository
                repository.toggleFavoriteStatus(
                    userId = userId,
                    storyId = updated.id,
                    isFavorite = newLikedState
                )
            } catch (e: Exception) {
                // Hoàn tác UI nếu lưu lên Firestore thất bại
                _mangaDetail.value = current
                _error.value = "Lỗi lưu trạng thái yêu thích."
            }
        }
    }

    // ===========================================================
    // ⭐ XỬ LÝ CẬP NHẬT RATING
    // ===========================================================
    fun updateRating(newRating: Float) {
        val current = _mangaDetail.value ?: return

        // UI cập nhật ngay
        val updated = current.copy(rating = newRating)
        _mangaDetail.value = updated

        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                repository.updateRating(current.id, userId, newRating)
            } catch (_: Exception) {}
        }
    }
    fun incrementViewCount() {
        val current = _mangaDetail.value
        if (current != null) {
            // 1. Cập nhật UI ngay lập tức
            val updated = current.copy(views = current.views + 1)
            _mangaDetail.value = updated

            // 2. Cập nhật lên Firestore thông qua Repository
            viewModelScope.launch {
                try {
                    repository.incrementViewCount(updated.id, current.views)
                } catch (e: Exception) {
                    // Xử lý lỗi nếu việc cập nhật View Count thất bại
                    // (Thường bỏ qua lỗi này vì view count không quan trọng như like/rating)
                    println("Lỗi cập nhật View Count: ${e.localizedMessage}")
                }
            }
        }
    }
    fun saveReadHistory(storyId: String, chapterId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // ⭐️ Cần có hàm này trong MangaRepository
                repository.saveReadHistory(userId, storyId, chapterId)
            } catch (e: Exception) {
                println("Lỗi lưu lịch sử đọc: ${e.message}")
            }
        }
    }

}
