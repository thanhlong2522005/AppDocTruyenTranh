package com.example.appdoctruyentranh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
// Giả định PrimaryColor, AppHeader, AppBottomNavigationBar, và Story
// đã được định nghĩa và truy cập được từ AppComponents.kt hoặc package này.

// --- Data Model và Mock Data ---
data class HistoryItem(
    val story: Story,
    val lastChapter: String,
    val readTime: String
)

val mockHistoryStories = listOf(
    HistoryItem(Story(1, "Toàn Cầu Quỷ Dị Thời Đại"), "Chương 25", "Vừa xong"),
    HistoryItem(Story(9, "Thiên Đạo Đồ Thư Quán"), "Chương 150", "1 giờ trước"),
    HistoryItem(Story(10, "Vạn Cổ Tối Cường Tông"), "Chương 50", "Hôm qua"),
    HistoryItem(Story(11, "Phàm Nhân Tu Tiên"), "Chương 10", "1 tuần trước"),
)

// =========================================================================
// Màn hình Chính: HistoryScreen
// =========================================================================

@Composable
fun HistoryScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            AppHeader(
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Icon quay lại
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(16.dp)
                            .clickable {
                                navController.popBackStack() // Hành động quay lại
                            }
                    )
                }
            )
        },

        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tiêu đề
            Text(
                text = "Lịch sử Đọc",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // Danh sách lịch sử
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (mockHistoryStories.isEmpty()) {
                    item {
                        EmptyHistoryMessage(message = "Bạn chưa đọc truyện nào gần đây.")
                    }
                } else {
                    items(mockHistoryStories) { item ->
                        HistoryStoryItem(
                            item = item,
                            onStoryClick = {
                                // Điều hướng đến trang chi tiết truyện
                                navController.navigate("manga_detail/${item.story.id}")
                            },
                            onDeleteClick = {
                                // Xử lý logic xóa khỏi lịch sử
                                println("Deleted from history: ${item.story.title}")
                            }
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Các Composable Thành phần Lịch sử
// =========================================================================

@Composable
fun HistoryStoryItem(item: HistoryItem, onStoryClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable(onClick = onStoryClick)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Ảnh bìa (Placeholder)
        Card(
            modifier = Modifier.size(70.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook, // Icon sách mở
                    contentDescription = "Cover",
                    tint = PrimaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Thông tin
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.story.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Đã đọc đến: ${item.lastChapter}",
                fontSize = 13.sp,
                color = PrimaryColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Đọc lần cuối: ${item.readTime}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // 3. Nút Xóa
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Xóa khỏi lịch sử",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun EmptyHistoryMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Empty",
            tint = Color.LightGray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHistoryScreen() {
    HistoryScreen(navController = rememberNavController())
}