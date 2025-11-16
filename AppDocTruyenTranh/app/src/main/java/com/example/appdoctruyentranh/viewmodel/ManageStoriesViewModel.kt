package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository
import com.example.appdoctruyentranh.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManageStoriesViewModel : ViewModel() {

    private val repo = MangaRepository()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> get() = _stories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun loadStories() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repo.fetchAllStories()
            _stories.value = result
            _isLoading.value = false
        }
    }

    fun deleteStory(id: String) {
        viewModelScope.launch {
            repo.deleteStory(id)
            loadStories()
        }
    }
}
