// File: model/DownloadItem.kt
package com.example.appdoctruyentranh.model

import com.example.appdoctruyentranh.viewmodel.DownloadStatus
import com.example.appdoctruyentranh.model.Chapter

data class DownloadItem(
    val id: Long, // ID duy nhat cho muc tai xuong
    val storyId: String, // ID cua Truyen
    val storyTitle: String, // Ten Truyen
    val chapter: Chapter, // Thong tin chuong dang tai (import tu Chapter.kt)
    val progress: Int, // Tien do tai (0-100)
    val status: DownloadStatus // Trang thai tai (PENDING, DOWNLOADING, COMPLETED, ERROR)
)