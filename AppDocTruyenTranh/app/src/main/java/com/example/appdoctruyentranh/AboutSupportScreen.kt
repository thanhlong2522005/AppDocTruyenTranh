// File: AboutSupportScreen.kt
package com.example.appdoctruyentranh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSupportScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giới thiệu & Hỗ trợ", fontWeight = FontWeight.Bold) },
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
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- 1. TIÊU ĐỀ CHÍNH ---
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Về Ứng dụng MangaGo", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // --- 2. GIỚI THIỆU CHUNG ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MangaGo là một ứng dụng đọc truyện tranh đa nền tảng được xây dựng hoàn toàn bằng Jetpack Compose, mang đến trải nghiệm đọc mượt mà và hiện đại cho người dùng.",
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }

            // --- 3. TÍNH NĂNG CHÍNH ---
            item {
                Text("✨ Tính năng chính", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                val features = listOf(
                    "📖 Đọc truyện" to "Giao diện đọc truyện tối ưu, hỗ trợ chế độ cuộn dọc và lật trang ngang.",
                    "👤 Hệ thống tài khoản" to "Đăng ký / Đăng nhập bằng Email, Google, Facebook. Chế độ khách và chỉnh sửa thông tin cá nhân.",
                    "⚙️ Phân quyền Admin" to "Hệ thống phân quyền dựa trên vai trò sử dụng Firestore với giao diện quản lý riêng.",
                    "❤️ Cá nhân hóa" to "Thêm truyện vào danh sách Yêu thích và Lưu lại Lịch sử đọc.",
                    "🔍 Tìm kiếm & Khám phá" to "Tìm kiếm truyện theo tên, tác giả, và khám phá theo Thể loại.",
                    "🎨 Giao diện hiện đại" to "Xây dựng 100% bằng Jetpack Compose theo ngôn ngữ thiết kế Material 3, hỗ trợ chế độ Sáng/Tối."
                )

                features.forEach { (title, description) ->
                    FeatureItem(title = title, description = description)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- 4. CÔNG NGHỆ SỬ DỤNG ---
            item {
                Text("🛠️ Công nghệ sử dụng", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                val technologies = mapOf(
                    "Ngôn ngữ" to "Kotlin",
                    "Giao diện" to "Jetpack Compose",
                    "Kiến trúc" to "MVVM (Model-View-ViewModel)",
                    "Backend & CSDL" to "Firebase Authentication & Cloud Firestore",
                    "Tải ảnh" to "Coil",
                    "Điều hướng" to "Navigation Compose"
                )

                technologies.forEach { (title, detail) ->
                    TechnologyItem(title = title, detail = detail)
                }
            }

            // --- 5. HỖ TRỢ (Nút) ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate("report_feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Báo cáo lỗi / Gửi phản hồi", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FeatureItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TechnologyItem(title: String, detail: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "$title:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = detail,
            fontSize = 14.sp,
            color = PrimaryColor,
            fontWeight = FontWeight.Medium
        )
    }
}