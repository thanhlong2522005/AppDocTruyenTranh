// File: data/DownloadRepository.kt (Thay thế logic cũ)
package com.example.appdoctruyentranh.data

import com.example.appdoctruyentranh.data.local.DownloadDao
import com.example.appdoctruyentranh.data.local.DownloadItemEntity
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.viewmodel.DownloadStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DownloadRepository(private val downloadDao: DownloadDao) {

    // ⭐ Ánh xạ Flow từ Entity sang Model cho ViewModel
    val downloadItemsFlow: Flow<List<com.example.appdoctruyentranh.model.DownloadItem>> =
        downloadDao.getAllDownloads().map { entities ->
            entities.map { entity ->
                // Ánh xạ ngược từ Entity sang DownloadItem Model
                com.example.appdoctruyentranh.model.DownloadItem(
                    id = entity.id.toLong(),
                    storyId = entity.storyId,
                    storyTitle = entity.storyTitle,
                    chapter = Chapter(
                        id = entity.chapterId,
                        number = entity.chapterNumber,
                        title = entity.chapterTitle
                    ),
                    progress = entity.progress,
                    status = entity.status
                )
            }
        }

    // Bắt đầu tải xuống
    suspend fun startDownload(storyId: String, storyTitle: String, chapter: Chapter) {
        // 1. Tạo entity ban đầu (PENDING) và lưu vào DB.
        val initialEntity = DownloadItemEntity(
            storyId = storyId,
            storyTitle = storyTitle,
            chapterId = chapter.id,
            chapterNumber = chapter.number,
            chapterTitle = chapter.title,
            progress = 0,
            status = DownloadStatus.PENDING
        )
        // Lấy ID mới được tạo
        val itemId = downloadDao.insert(initialEntity).toInt()

        // 2. Mô phỏng và cập nhật trạng thái
        simulateDownload(itemId)
    }

    private suspend fun simulateDownload(itemId: Int) {
        var currentProgress = 0
        var currentStatus = DownloadStatus.DOWNLOADING

        // Cập nhật trạng thái ban đầu thành DOWNLOADING
        downloadDao.update(
            DownloadItemEntity(id = itemId, progress = 0, status = DownloadStatus.DOWNLOADING,
                storyId = "", storyTitle = "", chapterId = 0, chapterNumber = 0, chapterTitle = "") // Sẽ được merge
        )

        for (progress in 1..100 step 10) {
            delay(200)
            currentProgress = progress.coerceAtMost(100)

            // Cập nhật tiến trình vào DB
            downloadDao.update(
                DownloadItemEntity(id = itemId, progress = currentProgress, status = currentStatus,
                    storyId = "", storyTitle = "", chapterId = 0, chapterNumber = 0, chapterTitle = "") // Cập nhật các trường chính
            )
        }

        // 3. Hoàn tất tải
        downloadDao.update(
            DownloadItemEntity(id = itemId, progress = 100, status = DownloadStatus.COMPLETED,
                storyId = "", storyTitle = "", chapterId = 0, chapterNumber = 0, chapterTitle = "")
        )
    }

    suspend fun deleteDownloadedItem(itemId: Long) {
        downloadDao.deleteById(itemId.toInt())
        // TODO: Thêm logic xóa file vật lý (ảnh) trên thiết bị tại đây
    }
}