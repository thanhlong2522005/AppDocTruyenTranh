package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.model.Genre
import com.example.appdoctruyentranh.model.Story
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class UploadViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storiesRef = db.collection("stories")
    private val genresRef = db.collection("genres")

    // Các collection hiển thị trên trang chủ
    private val displayCollections = mapOf(
        "banners" to db.collection("banners"),
        "completed_stories" to db.collection("completed_stories"),
        "favorites" to db.collection("favorites"),
        "most_viewed" to db.collection("most_viewed"),
        "new_releases" to db.collection("new_releases"),
        "new_updates" to db.collection("new_updates"),
        "trending_list" to db.collection("trending_list")
    )

    // =================================================================
    // 1. TẠO TRUYỆN MỚI
    // =================================================================
    fun createStory(
        story: Story,
        imageUrl: String,
        selectedGenreNames: List<String>,
        displayLists: Set<String>, // các collection muốn thêm vào
        onSuccess: (storyId: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val docRef = storiesRef.document()
                val newStory = story.copy(
                    id = docRef.id,
                    imageUrl = imageUrl,
                    genres = selectedGenreNames
                )

                // Bước 1: Tạo truyện
                docRef.set(newStory).await()

                // Bước 2: Thêm vào các danh sách hiển thị
                displayLists.forEach { collectionName ->
                    addToDisplayList(collectionName, docRef.id, {}, {})
                }

                onSuccess(docRef.id)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // =================================================================
    // 2. CẬP NHẬT TRUYỆN
    // =================================================================
    fun updateStory(
        story: Story,
        imageUrl: String,
        selectedGenreNames: List<String>,
        displayLists: Set<String>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updatedStory = story.copy(
                    imageUrl = imageUrl,
                    genres = selectedGenreNames
                )

                // Cập nhật truyện
                storiesRef.document(story.id).set(updatedStory, SetOptions.merge()).await()

                // Đồng bộ danh sách hiển thị
                val validCollections = displayCollections.keys
                val currentLists = validCollections.associateWith { collectionName ->
                    isInDisplayListSync(story.id, collectionName)
                }

                // Xóa những cái không còn
                currentLists.filter { (name, isIn) -> isIn && !displayLists.contains(name) }
                    .forEach { (name, _) -> removeFromDisplayList(name, story.id, {}, {}) }

                // Thêm những cái mới
                displayLists.filter { name -> !currentLists[name]!! }
                    .forEach { name -> addToDisplayList(name, story.id, {}, {}) }

                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // =================================================================
    // 3. THÊM CHƯƠNG (tự động đánh số)
    // =================================================================
    fun addChapter(
        mangaId: String,
        chapter: Chapter,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val storyRef = storiesRef.document(mangaId)
                val chaptersRef = storyRef.collection("chapters")

                db.runTransaction { transaction ->
                    val storySnapshot = transaction.get(storyRef)
                    val currentMax = storySnapshot.getLong("lastChapterNumber") ?: 0L
                    val newChapterNumber = currentMax + 1

                    val newChapterRef = chaptersRef.document(newChapterNumber.toString())
                    val newChapter = chapter.copy(
                        id = newChapterNumber.toInt(),
                        number = newChapterNumber.toInt(),
                        uploadDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    )

                    transaction.set(newChapterRef, newChapter)
                    transaction.set(storyRef, mapOf("lastChapterNumber" to newChapterNumber), SetOptions.merge())
                }.await()

                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // =================================================================
    // 4. LẤY DANH SÁCH TRUYỆN
    // =================================================================
    fun getAllStories(onResult: (List<Story>) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot = storiesRef.get().await()
                val stories = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Story::class.java)?.copy(id = doc.id)
                }
                onResult(stories)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }

    // =================================================================
    // 5. LẤY TRUYỆN THEO ID
    // =================================================================
    fun getStoryById(mangaId: String, onResult: (Story?) -> Unit) {
        viewModelScope.launch {
            try {
                val doc = storiesRef.document(mangaId).get().await()
                val story = doc.toObject(Story::class.java)?.copy(id = doc.id)
                onResult(story)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    // =================================================================
    // 6. LẤY DANH SÁCH THỂ LOẠI
    // =================================================================
    fun getGenres(onResult: (List<Genre>) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot = genresRef.get().await()
                val genres = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Genre::class.java)?.copy(id = doc.id.toInt())
                }
                onResult(genres)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }

    // =================================================================
    // 7. THÊM VÀO DANH SÁCH HIỂN THỊ
    // =================================================================
    fun addToDisplayList(
        collectionName: String,
        storyId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val collection = displayCollections[collectionName]
                    ?: throw IllegalArgumentException("Collection $collectionName không tồn tại")

                val exists = collection.document(storyId).get().await().exists()
                if (exists) {
                    onSuccess()
                    return@launch
                }

                collection.document(storyId).set(mapOf("storyId" to storyId)).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
    // Thêm vào UploadViewModel.kt
    suspend fun getStoryByIdSuspend(mangaId: String): Story? = withContext(Dispatchers.IO) {
        try {
            val doc = storiesRef.document(mangaId).get().await()
            doc.toObject(Story::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGenresSuspend(): List<Genre> = withContext(Dispatchers.IO) {
        try {
            val snapshot = genresRef.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Genre::class.java)?.copy(id = doc.id.toInt())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    // =================================================================
    // 8. XÓA KHỎI DANH SÁCH HIỂN THỊ
    // =================================================================
    fun removeFromDisplayList(
        collectionName: String,
        storyId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val collection = displayCollections[collectionName]
                    ?: throw IllegalArgumentException("Collection $collectionName không tồn tại")
                collection.document(storyId).delete().await()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // =================================================================
    // 9. KIỂM TRA TRONG DANH SÁCH
    // =================================================================
    fun isInDisplayList(
        collectionName: String,
        storyId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val collection = displayCollections[collectionName]
                    ?: throw IllegalArgumentException("Collection $collectionName không tồn tại")
                val exists = collection.document(storyId).get().await().exists()
                onResult(exists)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // Helper: sync version (dùng trong update)
    internal suspend fun isInDisplayListSync(storyId: String, collectionName: String): Boolean {
        return try {
            val collection = displayCollections[collectionName]!!
            collection.document(storyId).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    // =================================================================
    // 10. TĂNG VIEW / LIKE
    // =================================================================
    fun incrementViews(storyId: String) {
        storiesRef.document(storyId).update("views", FieldValue.increment(1))
    }

    fun toggleLike(storyId: String, currentLikes: Int, isLiked: Boolean) {
        val newLikes = if (isLiked) currentLikes - 1 else currentLikes + 1
        storiesRef.document(storyId).update("likes", newLikes)
    }
}