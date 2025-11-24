// File: data/local/DownloadDao.kt
package com.example.appdoctruyentranh.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    // ⭐ Quan trọng: Trả về Flow để ViewModel lắng nghe thay đổi
    @Query("SELECT * FROM downloads ORDER BY storyTitle ASC, chapterNumber ASC")
    fun getAllDownloads(): Flow<List<DownloadItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DownloadItemEntity): Long // Trả về ID

    @Update
    suspend fun update(item: DownloadItemEntity)

    @Query("DELETE FROM downloads WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)
}