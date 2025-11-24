package com.example.appdoctruyentranh.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val content: String = "",
    @ServerTimestamp // Annotation quan trọng để Firestore tự động thêm thời gian
    val timestamp: Date? = null
)