package com.example.appdoctruyentranh.model

data class HomeUiState(
    val banners: List<BannerItem> = emptyList(),
    val newUpdates: List<Story> = emptyList(),
    val mostViewed: List<Story> = emptyList(),
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
