package com.example.appdoctruyentranh.model

import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.Exclude
import java.util.Date


data class UserInfo(
    val name: String = "Ẩn danh",
    val avatarUrl: String = ""
)


data class Comment(
    var id: String = "",
    val userId: String = "",
    val content: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,


    @get:Exclude
    var userInfo: UserInfo? = null
)
