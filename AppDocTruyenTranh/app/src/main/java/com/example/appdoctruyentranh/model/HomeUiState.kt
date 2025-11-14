package com.example.appdoctruyentranh.model

data class HomeUiState(
    val banners: List<Story> = emptyList(),
    val newUpdates: List<Story> = emptyList(),

    val mostViewed: List<Story> = emptyList(),
    val favorites: List<Story> = emptyList(),
    val trending: List<Story> = emptyList(),
    val newReleases: List<Story> = emptyList(),
    val completedStories: List<Story> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = banners.isEmpty() &&
                newUpdates.isEmpty() &&
                mostViewed.isEmpty() &&
                completedStories.isEmpty()
}
