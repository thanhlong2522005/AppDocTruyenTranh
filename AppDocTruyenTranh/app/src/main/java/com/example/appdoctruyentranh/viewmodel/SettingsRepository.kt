package com.example.appdoctruyentranh.viewmodel // CHẮC CHẮN GÓI NÀY PHẢI ĐÚNG

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Khai báo DataStore cho Settings
// Context.dataStore là một thuộc tính mở rộng (extension property) cho Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    // Key để lưu trạng thái Dark Mode
    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    /**
     * Flow chứa trạng thái Dark Mode hiện tại.
     */
    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false // Mặc định là false (Light)
        }

    /**
     * Lưu trạng thái Dark Mode mới vào DataStore.
     */
    suspend fun saveDarkModePreference(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }
}