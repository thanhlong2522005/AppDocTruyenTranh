@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appdoctruyentranh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
// Import PrimaryColor

@Composable
fun ProfileScreen(navController: NavHostController) {
    Scaffold(
        topBar = { AppHeader(navigationIcon = null) }, // Icon menu mặc định
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Phần Header Profile
            ProfileHeader(navController = navController)

            // Danh sách các tùy chọn và tính năng nâng cao
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Divider(color = Color.LightGray)
                ProfileMenuItem(
                    icon = Icons.Default.CloudDownload,
                    title = "Truyện đã tải",
                    onClick = { navController.navigate("download_manager") } // -> DOWNLOAD MANAGER
                )
                Divider(color = Color.LightGray)
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = "Lịch sử đọc",
                    onClick = { navController.navigate("history") } // -> HISTORY SCREEN
                )
                Divider(color = Color.LightGray)
                ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    title = "Truyện yêu thích",
                    onClick = { navController.navigate("favorite") } // -> FAVORITE SCREEN
                )
                Divider(color = Color.LightGray, thickness = 8.dp) // Dùng divider dày để chia nhóm
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Cài đặt ứng dụng",
                    onClick = { navController.navigate("settings") } // -> SETTING SCREEN
                )
                Divider(color = Color.LightGray)
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "Giới thiệu & Hỗ trợ",
                    onClick = { /* TODO: Navigate to About/Support */ }
                )
                Divider(color = Color.LightGray)
                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    title = "Đăng xuất",
                    isLogout = true,
                    onClick = { navController.navigate("login") { popUpTo("home") { inclusive = true } } }
                )
                Divider(color = Color.LightGray)
            }
        }
    }
}

@Composable
fun ProfileHeader(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ảnh đại diện (Placeholder)
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(PrimaryColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = PrimaryColor,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tên Người Dùng", // Tạm thời
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "user@example.com", // Tạm thời
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        // Nút Chỉnh sửa Profile
        IconButton(
            onClick = { /* TODO: Navigate to Edit Profile */ } // Theo Figma: Chỉnh sửa
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Chỉnh sửa hồ sơ",
                tint = PrimaryColor
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isLogout: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isLogout) Color.Red else PrimaryColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLogout) Color.Red else Color.Black
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(16.dp)
        )
    }
}