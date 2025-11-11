package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FavoriteViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _favoriteStories = MutableStateFlow<List<Story>>(emptyList())
    val favoriteStories: StateFlow<List<Story>> = _favoriteStories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("Lỗi FavoriteViewModel: ${throwable.localizedMessage}")
        _favoriteStories.value = emptyList()
        _isLoading.value = false
    }

    init {
        // Tải lần đầu khi ViewModel được khởi tạo
        // (Đây không đủ để cập nhật từ màn hình khác)
        loadFavoriteStories()
    }

    /**
     * Hàm công khai để buộc tải lại danh sách yêu thích.
     * Sẽ được gọi từ FavoriteScreen khi màn hình được đưa lên (ví dụ: dùng LaunchedEffect).
     */
    fun refreshFavorites() {
        loadFavoriteStories()
    }

    fun loadFavoriteStories() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _isLoading.value = false
            _favoriteStories.value = emptyList()
            return
        }

        viewModelScope.launch(exceptionHandler) {
            _isLoading.value = true
            try {
                withTimeout(8000) { // Timeout 8 giây
                    val favoriteSnapshot = db.collection("favorites")
                        .document(userId)
                        .collection("stories")
                        .get()
                        .await()

                    val storyIds = favoriteSnapshot.documents.map { it.id }

                    if (storyIds.isEmpty()) {
                        _favoriteStories.value = emptyList()
                        _isLoading.value = false
                        return@withTimeout
                    }

                    val stories = mutableListOf<Story>()
                    for (storyId in storyIds) {
                        // Sử dụng async để tải song song các truyện
                        // và withContext(Dispatchers.IO) nếu cần, nhưng Firebase SDK đã hỗ trợ suspension.
                        try {
                            val storyDoc = db.collection("stories").document(storyId).get().await()
                            storyDoc.toObject(Story::class.java)?.copy(id = storyId)?.let {
                                stories.add(it)
                            }
                        } catch (e: Exception) {
                            // Bỏ qua lỗi 1 truyện
                            continue
                        }
                    }

                    _favoriteStories.value = stories.sortedBy { it.title }

                }
            } catch (e: TimeoutCancellationException) {
                // Đã được xử lý bởi CoroutineExceptionHandler
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromFavorites(storyId: String, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("favorites")
                    .document(userId)
                    .collection("stories")
                    .document(storyId)
                    .delete()
                    .await()

                // ⭐️ Tải lại danh sách sau khi xóa thành công
                loadFavoriteStories()
                onComplete()
            } catch (e: Exception) {
                onComplete()
            }
        }
    }
}