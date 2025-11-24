// File: data/FeedbackRepository.kt
package com.example.appdoctruyentranh.data

import com.example.appdoctruyentranh.model.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FeedbackRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("app_feedback") // Collection riêng cho báo cáo

    /**
     * Gửi đối tượng Feedback mới lên Firestore.
     */
    suspend fun submitFeedback(feedback: Feedback) {
        try {
            // Firestore sẽ tự động điền timestamp và tạo document ID
            collection.add(feedback).await()
        } catch (e: Exception) {
            // Log lỗi để debug
            e.printStackTrace()
            throw e
        }
    }
}