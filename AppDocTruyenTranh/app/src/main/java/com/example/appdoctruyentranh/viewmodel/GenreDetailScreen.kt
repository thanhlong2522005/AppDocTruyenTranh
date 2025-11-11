package com.example.appdoctruyentranh.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.GenreRepository
import com.example.appdoctruyentranh.model.Story
import kotlinx.coroutines.launch

class GenreDetailViewModel : ViewModel() {
    private val repo = GenreRepository()

    private val _stories = mutableStateOf<List<Story>>(emptyList())
    val stories: State<List<Story>> = _stories

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun loadStories(genreId: Int) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = repo.getStoriesByGenre(genreId)
                _stories.value = result
            } catch (e: Exception) {
                _error.value = "Lỗi tải truyện: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}