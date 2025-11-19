@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appdoctruyentranh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appdoctruyentranh.viewmodel.ReadingFont
import com.example.appdoctruyentranh.viewmodel.ReadingMode
import com.example.appdoctruyentranh.PrimaryColor
// Import các lớp cần thiết cho SettingsViewModel
import com.example.appdoctruyentranh.viewmodel.SettingsViewModel
// Import hàm helper từ ReadScreen.kt (Cần đảm bảo hàm này có sẵn)
import com.example.appdoctruyentranh.getCustomFontFamily
import androidx.compose.ui.platform.LocalContext // Cần cho ViewModel Factory

// =========================================================================
// 1. Dialog Chọn Font Chữ (Giữ nguyên)
// =========================================================================

@Composable
fun FontSettingDialog(
    currentFont: ReadingFont,
    onFontSelected: (ReadingFont) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Chọn Font Chữ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                Spacer(modifier = Modifier.height(16.dp))

                ReadingFont.entries.forEach { font ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFontSelected(font)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = font.fontName,
                            fontSize = 16.sp,
                            fontWeight = if (font == currentFont) FontWeight.Bold else FontWeight.Normal,
                            color = if (font == currentFont) PrimaryColor else Color.Black,
                            fontFamily = getCustomFontFamily(font) // Sử dụng font helper
                        )
                        if (font == currentFont) {
                            Icon(Icons.Default.Check, contentDescription = "Đã chọn", tint = PrimaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Hủy", color = PrimaryColor)
                }
            }
        }
    }
}

// =========================================================================
// 2. Dialog Chọn Chế độ Lật trang (Giữ nguyên)
// =========================================================================

@Composable
fun ModeSettingDialog(
    currentMode: ReadingMode,
    onModeSelected: (ReadingMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Chọn Chế độ Đọc", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                Spacer(modifier = Modifier.height(16.dp))

                val modes = listOf(
                    ReadingMode.VERTICAL_SCROLL to "Cuộn dọc (Giống Web)",
                    ReadingMode.HORIZONTAL_PAGINATION to "Lật ngang (Giống Sách)"
                )

                modes.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onModeSelected(mode)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            fontWeight = if (mode == currentMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (mode == currentMode) PrimaryColor else Color.Black
                        )
                        if (mode == currentMode) {
                            Icon(Icons.Default.Check, contentDescription = "Đã chọn", tint = PrimaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Hủy", color = PrimaryColor)
                }
            }
        }
    }
}

// =========================================================================
// 3. Dialog Chọn Theme (Sáng/Tối) - FIX LỖI CRASH VÀ LOGIC
// =========================================================================

@Composable
fun ThemeSettingDialog(
    onDismiss: () -> Unit,
    // FIX CRASH: Sử dụng Factory
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(LocalContext.current)
    )
) {
    // 1. Lắng nghe trạng thái Dark Mode hiện tại từ ViewModel
    val uiState by settingsViewModel.uiState.collectAsState()
    val currentIsDarkMode = uiState.isDarkMode

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Chọn Chế độ Giao diện", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                Spacer(modifier = Modifier.height(16.dp))

                val themes = listOf(
                    false to "Chế độ Sáng",
                    true to "Chế độ Tối"
                )

                themes.forEach { (isDark, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // 2. Gọi hàm setDarkMode() của ViewModel để lưu trạng thái
                                settingsViewModel.setDarkMode(isDark)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            fontWeight = if (isDark == currentIsDarkMode) FontWeight.Bold else FontWeight.Normal,
                            // Dùng màu Theme
                            color = if (isDark == currentIsDarkMode) PrimaryColor else MaterialTheme.colorScheme.onSurface
                        )
                        if (isDark == currentIsDarkMode) {
                            Icon(Icons.Default.Check, contentDescription = "Đã chọn", tint = PrimaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Hủy", color = PrimaryColor)
                }
            }
        }
    }
}