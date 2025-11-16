package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    init {
        checkAdminStatus()
    }

    fun checkAdminStatus() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                firestore.collection("admins").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            _isAdmin.value = document.getBoolean("isAdmin") ?: false
                        } else {
                            _isAdmin.value = false
                        }
                    }
                    .addOnFailureListener {
                        _isAdmin.value = false // Lỗi -> không phải admin
                    }
            } else {
                _isAdmin.value = false // Chưa đăng nhập -> không phải admin
            }
        }
    }
}