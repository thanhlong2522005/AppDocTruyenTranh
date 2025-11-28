package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdoctruyentranh.data.MangaRepository // SỬA: Thêm import này
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = MangaRepository() // SỬA: Khởi tạo repository
    private val auth = FirebaseAuth.getInstance()

    // Các StateFlow để giữ thông tin người dùng và cho UI lắng nghe
    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _userAvatarUrl = MutableStateFlow("")
    val userAvatarUrl = _userAvatarUrl.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender = _gender.asStateFlow()

    /**
     * Tải thông tin chi tiết của người dùng đang đăng nhập từ collection "users" trên Firestore.
     */
    fun loadUserProfile() {
        val currentUser = auth.currentUser ?: return // Nếu không có user, không làm gì cả
        viewModelScope.launch {
            try {
                // SỬA: Gọi đến Repository để lấy dữ liệu, thay vì gọi trực tiếp Firestore
                val userProfile = repository.getUserProfile(currentUser.uid)

                if (userProfile != null) {
                    _userName.value = userProfile.name
                    _userEmail.value = currentUser.email ?: "" // Email vẫn có thể lấy từ Auth
                    _userAvatarUrl.value = userProfile.imageUrl
                    _gender.value = userProfile.gender
                }
            } catch (e: Exception) {
                // Xử lý lỗi nếu cần
                _userName.value = "Lỗi tải dữ liệu"
            }
        }
    }
}
