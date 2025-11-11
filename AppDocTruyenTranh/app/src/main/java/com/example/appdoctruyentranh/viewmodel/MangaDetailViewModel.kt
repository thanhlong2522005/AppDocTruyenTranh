package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository
import com.example.appdoctruyentranh.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MangaDetailViewModel : ViewModel() {
    private val repository = MangaRepository()
    private val _mangaDetail = MutableStateFlow<Story?>(null)
    val mangaDetail: StateFlow<Story?> get() = _mangaDetail
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun loadMangaDetail(mangaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // ⚠️ Thay đổi: Repository phải trả về kiểu Story
                val result = repository.fetchMangaDetail(mangaId)
                if (result != null) {
                    _mangaDetail.value = result
                } else {
                    _error.value = "Không tìm thấy truyện có ID $mangaId"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tải dữ liệu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}