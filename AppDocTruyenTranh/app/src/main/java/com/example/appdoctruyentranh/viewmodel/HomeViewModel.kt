package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository
import com.example.appdoctruyentranh.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = MangaRepository()

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    // Thêm dòng này
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadHomeData()
    }

    fun refresh() {
        _isRefreshing.value = true  // Bắt đầu refresh
        _uiState.value = HomeUiState(isLoading = true, errorMessage = null)
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            try {

                val banners = repository.fetchBanners()
                val newStories = repository.fetchNewStories()
                val mostViewed = repository.fetchMostViewed()
                val completedStories = repository.fetchCompletedStories()

                _uiState.value = HomeUiState(
                    banners = banners,
                    newUpdates = newStories,
                    mostViewed = mostViewed,
                    completedStories = completedStories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Không thể tải dữ liệu"
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}