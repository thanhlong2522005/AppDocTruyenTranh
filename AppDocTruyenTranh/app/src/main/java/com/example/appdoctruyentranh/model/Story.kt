package com.example.appdoctruyentranh.model

data class Story(
    var id: Int = 0,
    val title: String = "",
    val imageUrl: String = "",
    val genreIds: List<Int> = emptyList(),      // 🔹 ID thể loại
    val description: String = "" ,// 🔹 Mô tả truyện (tùy chọn)
    val chapters: List<String> = emptyList()
)
