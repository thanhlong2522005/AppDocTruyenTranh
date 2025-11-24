// File: data/local/DownloadStatusConverter.kt
package com.example.appdoctruyentranh.data.local

import androidx.room.TypeConverter
import com.example.appdoctruyentranh.viewmodel.DownloadStatus

class DownloadStatusConverter {
    @TypeConverter
    fun fromStatus(status: DownloadStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(statusName: String): DownloadStatus {
        return DownloadStatus.valueOf(statusName)
    }
}