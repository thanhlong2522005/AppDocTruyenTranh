package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.appdoctruyentranh.data.MangaRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await


sealed class RegistrationState {
    object Idle : RegistrationState() // Trạng thái chờ
    object Loading : RegistrationState() // Đang xử lý
    object Success : RegistrationState() // Thành công
    data class Error(val message: String) : RegistrationState() // Lỗi
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val repository = MangaRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState = _registrationState.asStateFlow()

    fun registerUser(email: String, password: String, name: String) {
        _registrationState.value = RegistrationState.Loading
        viewModelScope.launch {
            try {
                // 1. Tạo tài khoản với Email và Password
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // 2. Nếu tạo tài khoản thành công, lưu thông tin vào Firestore thông qua Repository
                    repository.createUserInFirestore(firebaseUser, name)
                    _registrationState.value = RegistrationState.Success
                } else {
                    _registrationState.value = RegistrationState.Error("Không thể lấy thông tin người dùng.")
                }
            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error(e.message ?: "Đã xảy ra lỗi không xác định")
            }
        }
    }

    init {
        checkAdminStatus()
    }

    fun checkAdminStatus() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _isAdmin.value = false
                return@launch // Kết thúc sớm nếu chưa đăng nhập
            }

            try {
                // Sử dụng await() để coroutine chờ kết quả trả về từ Firestore
                val document = firestore.collection("admins").document(currentUser.uid).get().await()

                if (document != null && document.exists()) {
                    // Cập nhật giá trị khi đã có kết quả
                    _isAdmin.value = document.getBoolean("isAdmin") ?: false
                } else {
                    _isAdmin.value = false
                }
            } catch (e: Exception) {
                // Nếu có lỗi (mất mạng, v.v.), coi như không phải admin
                _isAdmin.value = false
            }
        }
    }
}