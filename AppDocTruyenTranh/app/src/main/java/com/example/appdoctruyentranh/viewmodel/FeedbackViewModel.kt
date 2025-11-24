// File: viewmodel/FeedbackViewModel.kt
package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.FeedbackRepository
import com.example.appdoctruyentranh.model.Feedback
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {
    private val repository = FeedbackRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submissionSuccess = MutableStateFlow<Boolean?>(null)
    val submissionSuccess: StateFlow<Boolean?> = _submissionSuccess.asStateFlow()

    // Để lưu lỗi (nếu có)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Xóa trạng thái thành công/lỗi
     */
    fun clearStatus() {
        _submissionSuccess.value = null
        _error.value = null
    }

    /**
     * Gửi báo cáo phản hồi lên Firestore.
     */
    fun submit(subject: String, details: String) {
        if (subject.isBlank() || details.isBlank()) {
            _error.value = "Vui lòng nhập đầy đủ Chủ đề và Chi tiết."
            return
        }

        val currentUser = auth.currentUser

        // Lấy thông tin người dùng (hoặc chế độ khách nếu chưa đăng nhập)
        val userId = currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
        val userEmail = currentUser?.email ?: "Khách"

        val feedback = Feedback(
            userId = userId,
            userEmail = userEmail,
            subject = subject.trim(),
            details = details.trim(),
            status = "Mới"
        )

        viewModelScope.launch {
            _isLoading.value = true
            _submissionSuccess.value = null
            _error.value = null
            try {
                repository.submitFeedback(feedback)
                _submissionSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Gửi thất bại. Vui lòng thử lại sau."
                _submissionSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}