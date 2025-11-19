package com.example.appdoctruyentranh.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appdoctruyentranh.viewmodel.SettingsViewModel // Bắt buộc phải import
import androidx.compose.ui.graphics.Color
// Imports bắt buộc cho Typography
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.isSystemInDarkTheme // Import này bị thiếu trong file cũ

// LƯU Ý: Đã XÓA định nghĩa các màu Purple80, Pink80, v.v. ở đây.
// Chúng được đọc từ Color.kt trong cùng package.

private val DarkColorScheme = darkColorScheme(
    // Sử dụng màu từ Color.kt
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFFFFBFE),
    onSurface = Color(0xFFFFFBFE),
)

private val LightColorScheme = lightColorScheme(
    // Sử dụng màu từ Color.kt
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

// --- KHẮC PHỤC LỖI: ĐỊNH NGHĨA TYPOGRAPHY ---
val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
)


@Composable
fun AppDocTruyenTranhTheme(
    dynamicColor: Boolean = true,
    // FIX LỖI CRASH: Bắt buộc phải sử dụng Factory
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(LocalContext.current)
    ),
    content: @Composable () -> Unit
) {
    // Lắng nghe trạng thái Dark Mode đã được lưu
    val uiState by settingsViewModel.uiState.collectAsState()

    // Nếu DataStore chưa có giá trị, dùng cài đặt hệ thống (isSystemInDarkTheme)
    // Nếu DataStore có giá trị, dùng giá trị đó (uiState.isDarkMode)
    val darkTheme = if (uiState.isDarkMode) true else isSystemInDarkTheme()

    val colorScheme = when {
        // Dynamic Color (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Theme đã chọn (Dark hoặc Light)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Sử dụng Typography đã định nghĩa
        content = content
    )
}