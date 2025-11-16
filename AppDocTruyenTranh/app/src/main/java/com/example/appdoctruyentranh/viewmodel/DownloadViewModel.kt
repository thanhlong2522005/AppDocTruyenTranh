// File: viewmodel/DownloadViewModel.kt
package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.model.DownloadItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

// Trạng thái cho việc tải xuống
enum class DownloadStatus { PENDING, DOWNLOADING, COMPLETED, ERROR }

class DownloadViewModel : ViewModel() {

    private val _downloadList = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloadList: StateFlow<List<DownloadItem>> = _downloadList

    // Hàm giả lập việc tải một chương
    fun startDownload(storyId: String, storyTitle: String, chapter: Chapter) {
        val existingItem = _downloadList.value.find { it.chapter.id == chapter.id }
        if (existingItem != null && existingItem.status != DownloadStatus.ERROR) return // Đã có trong danh sách

        val newItem = DownloadItem(
            id = Random.nextLong(),
            storyId = storyId,
            storyTitle = storyTitle,
            chapter = chapter,
            progress = 0,
            status = DownloadStatus.PENDING
        )

        _downloadList.update { it + newItem }

        // Mô phỏng quá trình tải xuống
        viewModelScope.launch {
            simulateDownload(newItem.id)
        }
    }

    private suspend fun simulateDownload(itemId: Long) {
        _downloadList.update { list ->
            list.map { item ->
                if (item.id == itemId) item.copy(status = DownloadStatus.DOWNLOADING) else item
            }
        }

        for (progress in 1..100 step 10) {
            kotlinx.coroutines.delay(200) // Tải chậm rãi
            _downloadList.update { list ->
                list.map { item ->
                    if (item.id == itemId && item.status == DownloadStatus.DOWNLOADING) {
                        item.copy(progress = progress.coerceAtMost(100))
                    } else item
                }
            }
        }

        // Hoàn tất tải
        _downloadList.update { list ->
            list.map { item ->
                if (item.id == itemId) {
                    item.copy(status = DownloadStatus.COMPLETED, progress = 100)
                } else item
            }
        }
    }

    // Hàm xóa file đã tải
    fun deleteDownloadedItem(itemId: Long) {
        _downloadList.update { list ->
            list.filter { it.id != itemId }
        }
        // TODO: Thêm logic xóa file vật lý trong ứng dụng thực tế
    }

    // Hàm lấy danh sách chương đã tải của một truyện (dùng cho Màn hình Chi tiết Truyện)
    fun getDownloadedChaptersForStory(storyId: String): List<Chapter> {
        return _downloadList.value
            .filter { it.storyId == storyId && it.status == DownloadStatus.COMPLETED }
            .map { it.chapter }
    }
}