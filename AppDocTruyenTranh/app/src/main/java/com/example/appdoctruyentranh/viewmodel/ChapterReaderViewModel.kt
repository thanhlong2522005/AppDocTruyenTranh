package com.example.appdoctruyentranh.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.model.Story
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- Enums ---
enum class ReadingMode { VERTICAL_SCROLL, HORIZONTAL_PAGINATION }
enum class ReadingFont(val fontName: String) {
    DEFAULT("Default"), SERIF("Serif"), SANS_SERIF("Sans Serif")
}

data class ChapterData(
    val pages: List<String> = emptyList(),
    val totalPages: Int = 0
)

class ChapterReaderViewModel : ViewModel() {

    // --- UI State ---
    private val _isMenuVisible = MutableStateFlow(false)
    val isMenuVisible: StateFlow<Boolean> = _isMenuVisible.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentFont = MutableStateFlow(ReadingFont.DEFAULT)
    val currentFont: StateFlow<ReadingFont> = _currentFont.asStateFlow()

    private val _readingMode = MutableStateFlow(ReadingMode.VERTICAL_SCROLL)
    val readingMode: StateFlow<ReadingMode> = _readingMode.asStateFlow()

    // --- Dữ liệu hiện tại ---
    private val _currentStory = MutableStateFlow<Story?>(null)
    val currentStory: StateFlow<Story?> = _currentStory.asStateFlow()

    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()

    private val _chapterData = MutableStateFlow(ChapterData())
    val chapterData: StateFlow<ChapterData> = _chapterData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- Cache ---
    private val chapterCache = mutableMapOf<String, ChapterData>()

    // --- Tải chương (mangaId: String, chapterNumber: Int) ---

    fun loadChapter(mangaId: String, chapterNumber: Int) {
        val cacheKey = "${mangaId}_$chapterNumber"
        _errorMessage.value = null
        if (chapterCache.containsKey(cacheKey)) {
            Log.d("ChapterReader", "Dùng cache cho $cacheKey")
            _chapterData.value = chapterCache[cacheKey]!!
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val db = FirebaseFirestore.getInstance()

                // 1. Lấy Story
                val storyDoc = db.collection("stories")
                    .document(mangaId)
                    .get()
                    .await()

                if (!storyDoc.exists()) {
                    Log.e("ChapterReader", "Không tìm thấy truyện với ID: $mangaId")
                    _errorMessage.value = "Truyện không tồn tại"
                    _isLoading.value = false
                    return@launch
                }

                Log.d("ChapterReader", "Story raw data: ${storyDoc.data}")
                val story = storyDoc.toObject(Story::class.java)?.apply { id = mangaId }
                _currentStory.value = story

                // 2. Lấy Chapter
                val chapterDoc = db.collection("stories")
                    .document(mangaId)
                    .collection("chapters")
                    .document(chapterNumber.toString())
                    .get()
                    .await()

                if (!chapterDoc.exists()) {
                    Log.e("ChapterReader", "Không tìm thấy chương $chapterNumber")
                    _errorMessage.value = "Chương $chapterNumber không tồn tại"
                    _isLoading.value = false
                    return@launch
                }

                Log.d("ChapterReader", "Chapter raw data: ${chapterDoc.data}")
                val chapter = chapterDoc.toObject(Chapter::class.java)?.apply { number = chapterNumber }
                _currentChapter.value = chapter

                Log.d("ChapterReader", "Chapter pages: ${chapter?.pages}")
                val pages = chapter?.pages ?: emptyList()
                val chapterData = ChapterData(pages = pages, totalPages = pages.size)
                _chapterData.value = chapterData
                chapterCache[cacheKey] = chapterData

            } catch (e: Exception) {
                Log.e("ChapterReader", "Lỗi khi tải chương: ${e.message}", e)
                _errorMessage.value = e.message ?: "Lỗi tải dữ liệu"
                _chapterData.value = ChapterData()
            } finally {
                _isLoading.value = false
            }
        }
    }


    // --- Xóa lỗi ---
    fun clearError() {
        _errorMessage.value = null
    }

    // --- UI Actions ---
    fun toggleMenuVisibility() {
        _isMenuVisible.update { !it }
    }

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
    }

    fun setReadingFont(font: ReadingFont) {
        _currentFont.value = font
    }

    fun setReadingMode(mode: ReadingMode) {
        _readingMode.value = mode
    }

    // --- Chuyển chương ---
    fun goToNextChapter(navController: NavHostController, mangaId: String, currentChapterNumber: Int) {
        val story = _currentStory.value ?: return
        val maxChapter = story.chapters.maxOfOrNull { it.number } ?: currentChapterNumber
        val nextChapter = currentChapterNumber + 1

        if (nextChapter <= maxChapter) {
            navigateToChapter(navController, mangaId, nextChapter)
        }
    }

    fun goToPreviousChapter(navController: NavHostController, mangaId: String, currentChapterNumber: Int) {
        val prevChapter = currentChapterNumber - 1
        if (prevChapter >= 1) {
            navigateToChapter(navController, mangaId, prevChapter)
        }
    }

    private fun navigateToChapter(navController: NavHostController, mangaId: String, chapterNumber: Int) {
        navController.navigate("read/$mangaId/$chapterNumber") {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // --- Dọn dẹp ---
    override fun onCleared() {
        super.onCleared()
        chapterCache.clear()
    }
}