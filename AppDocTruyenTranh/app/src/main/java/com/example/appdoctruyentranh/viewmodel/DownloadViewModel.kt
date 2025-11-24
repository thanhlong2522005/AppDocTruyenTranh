// File: viewmodel/DownloadViewModel.kt (Sửa đổi)
package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.appdoctruyentranh.data.DownloadRepository
import com.example.appdoctruyentranh.data.local.AppDatabase
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.model.DownloadItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

// Trạng thái cho việc tải xuống
enum class DownloadStatus { PENDING, DOWNLOADING, COMPLETED, ERROR }

class DownloadViewModel(private val repository: DownloadRepository) : ViewModel() {

    // ⭐ Lắng nghe trực tiếp từ Repository (ĐÃ PERSISTENT)
    val downloadList: StateFlow<List<DownloadItem>> = repository.downloadItemsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun startDownload(storyId: String, storyTitle: String, chapter: Chapter) {
        val existingItem = downloadList.value.find {
            it.chapter.id == chapter.id && it.storyId == storyId && it.status != DownloadStatus.ERROR
        }
        if (existingItem != null) return // Đã có trong danh sách

        viewModelScope.launch {
            repository.startDownload(storyId, storyTitle, chapter)
        }
    }

    fun deleteDownloadedItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteDownloadedItem(itemId)
        }
    }

    // ... (các hàm khác giữ nguyên)

    // ⭐ FACTORY để tạo ViewModel với Repository
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DownloadViewModel::class.java)) {
                val db = AppDatabase.getDatabase(context)
                val repo = DownloadRepository(db.downloadDao())
                return DownloadViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}