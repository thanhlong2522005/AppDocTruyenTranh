package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository // Giả định Repository có thể truy cập
import com.example.appdoctruyentranh.HistoryItem // Cần import HistoryItem data class
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HistoryViewModel : ViewModel() {
    private val repository = MangaRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    private val _historyList = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyList: StateFlow<List<HistoryItem>> = _historyList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadReadHistory()
    }

    fun loadReadHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _isLoading.value = false
            _historyList.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Lấy danh sách lịch sử đọc (ID truyện, ID chương, thời gian đọc)
                val historySnapshot = db.collection("read_history")
                    .document(userId)
                    .collection("reads")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(50) // Giới hạn 50 mục gần nhất
                    .get().await()

                val historyItems = historySnapshot.documents.mapNotNull { doc ->
                    val storyId = doc.getString("storyId") ?: return@mapNotNull null
                    val chapterId = doc.getString("chapterId") ?: "N/A"
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.let { time ->
                        // Giả định hàm chuyển đổi thời gian toRelativeTime() đã tồn tại
                        // Hoặc bạn có thể tự implement logic hiển thị thời gian tương đối ở UI
                        "Vừa xong" // Placeholder
                    } ?: "Không rõ"

                    // Tải thông tin chi tiết truyện
                    val storyDoc = db.collection("stories").document(storyId).get().await()
                    val story = storyDoc.toObject(com.example.appdoctruyentranh.model.Story::class.java)?.copy(id = storyId)

                    if (story != null && chapterId != "N/A") { // Kiểm tra chapterId hợp lệ
                        // Tải thông tin chương (Chương số/tên chương)
                        val chapterDoc = db.collection("stories").document(storyId)
                            .collection("chapters").document(chapterId).get().await()

                        var chapterNumber = 0
                        var chapterTitle = ""

                        // ⭐️ BƯỚC SỬA LỖI: Chỉ đọc dữ liệu nếu document chương tồn tại
                        if (chapterDoc.exists()) {
                            // Đọc 'number' (thường là Long trong Firestore) và 'title' an toàn
                            chapterNumber = chapterDoc.getLong("number")?.toInt() ?: 0
                            chapterTitle = chapterDoc.getString("title") ?: ""
                        } else {
                            // Nếu không tìm thấy chương, đặt tiêu đề mặc định
                            chapterTitle = "Chương không khả dụng (ID: $chapterId)"
                        }

                        // Format chuỗi hiển thị
                        val chapterDisplay = if (chapterTitle.isNotEmpty())
                            "Chương $chapterNumber: $chapterTitle"
                        else
                            "Chương $chapterNumber"

                        return@mapNotNull HistoryItem(
                            story = story,
                            lastChapter = chapterDisplay, // ⭐️ SỬ DỤNG CHUỖI ĐÃ FORMAT
                            readTime = timestamp
                        )
                    } else {
                        null
                    }
                }

                _historyList.value = historyItems
            } catch (e: Exception) {
                // Log lỗi
                e.printStackTrace()
                _historyList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeHistoryItem(storyId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Xóa tất cả các mục lịch sử liên quan đến StoryId này
                db.collection("read_history")
                    .document(userId)
                    .collection("reads")
                    .whereEqualTo("storyId", storyId)
                    .get().await()
                    .documents.forEach { doc ->
                        doc.reference.delete()
                    }

                // Tải lại danh sách sau khi xóa
                loadReadHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}