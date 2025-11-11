package com.example.appdoctruyentranh.model


data class Story(
    var id: String = "",
    val title: String = "",
    val author: String = "",
    val status: String = "",
    val rating: Float = 0f,
    val imageUrl: String = "",
    val likes: String = "",
    val genreIds: List<Int> = emptyList(),
    val views: String = "",
    val totalChapters: Int = 0,
    val genres: List<String> = emptyList(),
    val description: String = "",
    val chapters: List<Chapter> = emptyList(),
)
data class Chapter(
    val id: Int = 0,
    val number: Int = 0,
    val title: String = "",
    val uploadDate: String = ""
)
