package com.example.appdoctruyentranh.model


data class Story(
    var id: String = "",
    val title: String = "",
    val author: String = "",
    val status: String = "",
    var rating: Float = 0f,
    val imageUrl: String = "",
    var likes: Int = 0,
    var views: Int = 0,
    val genres: List<String> = emptyList(),
    val description: String = "",
    val chapters: List<Chapter> = emptyList(),
    var isLiked: Boolean = false
) {
    val totalChapters: Int
        get() = chapters.size
}



data class Chapter(
    val id: Int = 0,
    val number: Int = 0,
    val title: String = "",
    val uploadDate: String = ""
)
