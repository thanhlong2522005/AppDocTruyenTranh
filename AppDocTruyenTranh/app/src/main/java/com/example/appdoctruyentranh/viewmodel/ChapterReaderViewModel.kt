package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Định nghĩa các hằng số cho Font và Mode
enum class ReadingMode { VERTICAL_SCROLL, HORIZONTAL_PAGINATION }
enum class ReadingFont(val fontName: String) { DEFAULT("Default"), SERIF("Serif"), SANS_SERIF("Sans Serif") }

class ChapterReaderViewModel : ViewModel() {

    // --- Trạng thái UI cơ bản ---
    private val _isMenuVisible = MutableStateFlow(false)
    val isMenuVisible: StateFlow<Boolean> = _isMenuVisible.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // --- Trạng thái tùy chỉnh đọc (Nhiệm vụ Thành viên 3) ---
    private val _currentFont = MutableStateFlow(ReadingFont.DEFAULT)
    val currentFont: StateFlow<ReadingFont> = _currentFont.asStateFlow()

    private val _readingMode = MutableStateFlow(ReadingMode.VERTICAL_SCROLL)
    val readingMode: StateFlow<ReadingMode> = _readingMode.asStateFlow()


    fun toggleMenuVisibility() {
        _isMenuVisible.value = !_isMenuVisible.value
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setReadingFont(font: ReadingFont) {
        _currentFont.value = font
    }

    fun setReadingMode(mode: ReadingMode) {
        _readingMode.value = mode
    }

    // --- Logic Chuyển chương ---
    fun goToNextChapter(navController: NavHostController) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route ?: return
        val regex = Regex("read/(\\d+)/(\\d+)")
        val match = regex.find(currentRoute) ?: return

        val mangaId = match.groupValues[1].toIntOrNull() ?: return
        val currentChapterId = match.groupValues[2].toIntOrNull() ?: return

        val nextChapterId = currentChapterId + 1

        if (nextChapterId <= 100) { // Giả lập tối đa 100 chương
            navController.navigate("read/$mangaId/$nextChapterId") {
                launchSingleTop = true
            }
        } else {
            // Hiển thị thông báo: Hết chương
            // Trong thực tế, cần dùng Toast hoặc State để hiển thị trong UI
        }
    }

    fun goToPreviousChapter(navController: NavHostController) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route ?: return
        val regex = Regex("read/(\\d+)/(\\d+)")
        val match = regex.find(currentRoute) ?: return

        val mangaId = match.groupValues[1].toIntOrNull() ?: return
        val currentChapterId = match.groupValues[2].toIntOrNull() ?: return

        val prevChapterId = currentChapterId - 1

        if (prevChapterId >= 1) { // Giả lập chương tối thiểu là 1
            navController.navigate("read/$mangaId/$prevChapterId") {
                launchSingleTop = true
            }
        } else {
            // Hiển thị thông báo: Chương đầu tiên
        }
    }
}