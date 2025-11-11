package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository
import com.example.appdoctruyentranh.model.Genre
import com.example.appdoctruyentranh.model.Story
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val repository = MangaRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Story>>(emptyList())
    val searchResults: StateFlow<List<Story>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    init {
        loadGenres()
        loadRecentSearches()
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            search()
        }
    }

    fun selectGenre(genreId: Int?) {
        _selectedGenreId.value = genreId
        search()
    }

    fun clearResults() {
        _searchResults.value = emptyList()
        _searchQuery.value = ""
        _selectedGenreId.value = null
    }

    private fun search() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val query = _searchQuery.value.trim()
                val genreId = _selectedGenreId.value

                // Chỉ rõ kiểu trả về là List<Story>
                val results: List<Story> = if (query.isEmpty() && genreId == null) {
                    emptyList()
                } else {
                    repository.searchStories(query, genreId) // suspend call → OK trong coroutine
                }

                _searchResults.value = results

                if (query.isNotBlank() && results.isNotEmpty()) {
                    addToRecentSearches(query)
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    private fun loadGenres() {
        viewModelScope.launch {
            try {
                _genres.value = repository.getGenres()
            } catch (e: Exception) {
                _genres.value = emptyList()
            }
        }
    }

    private fun addToRecentSearches(query: String) {
        val updated = (listOf(query) + _recentSearches.value.filter { it != query }).take(6)
        _recentSearches.value = updated
        saveRecentSearches(updated)
    }

    private fun loadRecentSearches() {
        // TODO: Lưu vào SharedPreferences hoặc Room
        // Tạm thời để trống
    }

    private fun saveRecentSearches(list: List<String>) {
        // TODO: Lưu vào SharedPreferences hoặc Room
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        saveRecentSearches(emptyList())
    }
}