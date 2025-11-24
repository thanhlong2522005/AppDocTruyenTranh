// File: data/local/DownloadItemEntity.kt
package com.example.appdoctruyentranh.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.appdoctruyentranh.viewmodel.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadItemEntity(
    @PrimaryKey(autoGenerate = true) // ID tự tăng, dùng để tham chiếu nội bộ
    val id: Int = 0,
    val storyId: String,
    val storyTitle: String,
    val chapterId: Int, // ID của chương
    val chapterNumber: Int,
    val chapterTitle: String,
    val progress: Int,
    val status: DownloadStatus
)