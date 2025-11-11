package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.GenreRepository
import com.example.appdoctruyentranh.model.Genre
import com.example.appdoctruyentranh.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenreViewModel : ViewModel() {
    private val repository = GenreRepository()

    // Trạng thái UI (Loading, Success, Empty, Error)
    private val _uiState = MutableStateFlow<UiState<List<Genre>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Genre>>> = _uiState.asStateFlow()

    // Trạng thái khi kéo "Refresh"
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadGenres()
    }

    /** Gọi lại khi nhấn "Thử lại" */
    fun retry() {
        _uiState.value = UiState.Loading
        loadGenres()
    }

    /** Gọi khi người dùng kéo xuống để làm mới */
    fun refresh() {
        _isRefreshing.value = true
        _uiState.value = UiState.Loading
        loadGenres()
    }

    /** Tải danh sách thể loại từ Firestore */
    private fun loadGenres() {
        viewModelScope.launch {
            try {
                val genres = repository.getGenres()
                _uiState.value = if (genres.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(genres)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Không thể tải danh sách thể loại")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
