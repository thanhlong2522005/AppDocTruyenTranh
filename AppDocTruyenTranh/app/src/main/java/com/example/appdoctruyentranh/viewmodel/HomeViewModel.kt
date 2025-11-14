package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository
import com.example.appdoctruyentranh.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = MangaRepository()

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadHomeData()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loadHomeData()
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                val banners = repository.fetchBanners()
                val newStories = repository.fetchNewStories()
                val mostViewed = repository.fetchMostViewed()
                val completedStories = repository.fetchCompletedStories()
                val favorites = repository.fetchFavorites()
                val trending = repository.fetchTrending()
                val newReleases = repository.fetchNewReleases()

                _uiState.update { current ->
                    current.copy(
                        banners = banners,
                        newUpdates = newStories,
                        mostViewed = mostViewed,
                        completedStories = completedStories,
                        favorites = favorites,
                        trending = trending,
                        newReleases = newReleases,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể tải dữ liệu"
                    )
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}