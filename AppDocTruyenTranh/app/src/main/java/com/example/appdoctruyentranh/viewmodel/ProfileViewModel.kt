package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender

    fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                firestore.collection("users").document(user.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            _gender.value = document.getString("gender") ?: ""
                        }
                    }
            }
        }
    }
}