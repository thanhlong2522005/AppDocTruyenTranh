@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appdoctruyentranh

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appdoctruyentranh.viewmodel.ChapterReaderViewModel
import com.example.appdoctruyentranh.viewmodel.ReadingMode
import com.example.appdoctruyentranh.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth

data class SettingItem(val id: String, val title: String, val icon: ImageVector, val route: String? = null)

val settingItems = listOf(
    SettingItem("group_read", "Tùy chỉnh đọc", Icons.Default.MenuBook),
    SettingItem("font", "Font chữ", Icons.Default.FontDownload),
    SettingItem("page_mode", "Chế độ lật trang", Icons.Default.Pages),
    SettingItem("theme", "Chế độ giao diện", Icons.Default.LightMode),

    SettingItem("group_download", "Quản lý File", Icons.Default.Download),
    SettingItem("download_manager", "Quản lý Tải xuống", Icons.Default.DownloadForOffline, "download_manager"),

    SettingItem("group_account", "Tài khoản", Icons.Default.Person),
    SettingItem("change_pass", "Đổi mật khẩu", Icons.Default.Key),
    SettingItem("notifications", "Quản lý thông báo", Icons.Default.Notifications),

    SettingItem("group_support", "Hỗ trợ", Icons.Default.ContactSupport),
    SettingItem("report", "Báo cáo lỗi / Phản hồi", Icons.Default.Feedback, "report_feedback"),
)


@Composable
fun SettingScreen(
    navController: NavHostController,
    chapterReaderViewModel: ChapterReaderViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(LocalContext.current)
    )
) {
    var showFontDialog by remember { mutableStateOf(false) }
    var showModeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val isDarkMode by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            settingItems.forEach { item ->
                when {
                    item.id.startsWith("group_") -> {
                        item {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                color = PrimaryColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    else -> {
                        item {
                            val detailText = when(item.id) {
                                "font" -> chapterReaderViewModel.currentFont.collectAsState().value.fontName
                                "page_mode" -> if (chapterReaderViewModel.readingMode.collectAsState().value == ReadingMode.VERTICAL_SCROLL) "Cuộn dọc" else "Lật ngang"
                                "theme" -> if (isDarkMode.isDarkMode) "Tối" else "Sáng"
                                else -> null
                            }

                            SettingRow(item = item, detailText = detailText) {
                                when (item.id) {
                                    "font" -> showFontDialog = true
                                    "page_mode" -> showModeDialog = true
                                    "theme" -> showThemeDialog = true
                                    "download_manager" -> navController.navigate("download_manager")
                                    "report" -> navController.navigate("report_feedback")
                                    "change_pass" -> {
                                        val user = auth.currentUser
                                        if (user != null && user.email != null) {
                                            val isEmailPasswordUser = user.providerData.any { it.providerId == "password" }
                                            if (isEmailPasswordUser) {
                                                auth.sendPasswordResetEmail(user.email!!)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            Toast.makeText(context, "Email đặt lại mật khẩu đã được gửi!", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(context, "Lỗi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                            } else {
                                                Toast.makeText(context, "Chức năng này không dành cho tài khoản Google/Facebook.", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Vui lòng đăng nhập để sử dụng chức năng này.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    "notifications" -> { /* Toggle Logic */ }
                                    else -> { /* Navigate or handle logic */ }
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showFontDialog) {
        FontSettingDialog(
            currentFont = chapterReaderViewModel.currentFont.collectAsState().value,
            onFontSelected = { font -> chapterReaderViewModel.setReadingFont(font) },
            onDismiss = { showFontDialog = false }
        )
    }

    if (showModeDialog) {
        ModeSettingDialog(
            currentMode = chapterReaderViewModel.readingMode.collectAsState().value,
            onModeSelected = { mode -> chapterReaderViewModel.setReadingMode(mode) },
            onDismiss = { showModeDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemeSettingDialog(
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
fun SettingRow(item: SettingItem, detailText: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (item.id == "notifications") {
            var isChecked by remember { mutableStateOf(true) }
            Switch(checked = isChecked, onCheckedChange = { isChecked = it }, enabled = detailText == null)
        } else if (detailText != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = detailText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}