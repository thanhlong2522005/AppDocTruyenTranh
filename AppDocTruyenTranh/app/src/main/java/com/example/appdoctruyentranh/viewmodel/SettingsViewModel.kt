package com.example.appdoctruyentranh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Data class đại diện cho trạng thái UI của Settings.
 */
data class SettingsUiState(
    val isDarkMode: Boolean = false
)

/**
 * ViewModel quản lý trạng thái Dark Mode của ứng dụng.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.isDarkModeFlow
        .map { isDarkMode ->
            SettingsUiState(isDarkMode)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveDarkModePreference(isDark)
        }
    }

    /**
     * Lớp Factory để khởi tạo SettingsViewModel.
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                // Đảm bảo Repository luôn nhận ApplicationContext
                val applicationContext = context.applicationContext
                val repository = SettingsRepository(applicationContext)
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}