// File: com/example/appdoctruyentranh/viewmodel/GenreViewModel.kt

package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository
import com.example.appdoctruyentranh.model.Genre
import com.example.appdoctruyentranh.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenreViewModel : ViewModel() {
    private val repository = MangaRepository()

    // Trạng thái UI
    private val _uiState = MutableStateFlow<UiState<List<Genre>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Genre>>> = _uiState.asStateFlow()

    // Cho Pull-to-Refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadGenres()
    }

    // Hàm gọi khi nhấn "Thử lại"
    fun retry() {
        _uiState.value = UiState.Loading
        loadGenres()
    }

    // Hàm gọi khi kéo refresh
    fun refresh() {
        _isRefreshing.value = true
        _uiState.value = UiState.Loading
        loadGenres()
    }

    private fun loadGenres() {
        viewModelScope.launch {
            try {
                val genres = repository.fetchGenres()

                _uiState.value = if (genres.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(genres)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Không thể tải thể loại")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

}
