// File: model/Feedback.kt
package com.example.appdoctruyentranh.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Feedback(
    // Thông tin người gửi (Lấy từ Firebase Auth)
    val userId: String = "",
    val userEmail: String = "",

    // Nội dung báo cáo
    val subject: String = "",
    val details: String = "",

    // Trạng thái (để Admin theo dõi)
    val status: String = "Mới", // Ví dụ: "Mới", "Đang xử lý", "Đã hoàn thành"

    // Thời gian gửi
    @ServerTimestamp
    val timestamp: Date? = null
)