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
import com.example.appdoctruyentranh.model.Story

import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.model.Chapter

// Giả định PrimaryColor, AppHeader, AppBottomNavigationBar, và Story
// đã được định nghĩa và truy cập được từ AppComponents.kt hoặc package này.

// --- Data Model và Mock Data ---
data class HistoryItem(
    val story: Story,
    val lastChapter: String,
    val readTime: String
)

val mockHistoryStories = listOf(
    HistoryItem(
        story = Story(
            id ="",
            title = "Toàn Cầu Quỷ Dị Thời Đại",
            author = "Tác giả A",
            status = "Đang tiến hành",
            rating = 4.7f,
            imageUrl = "https://i.imgur.com/global_horror_cover.jpg",
            likes = "120k",
            views = "1.2M",
            totalChapters = 150,
            genreIds = listOf(1, 2),
            genres = listOf("Hành động", "Kinh dị"),
            description = "Thế giới đột nhiên bước vào thời đại quỷ dị...",
            chapters = listOf(
                Chapter(id = 25, number = 25, title = "Đại chiến quỷ vương", uploadDate = "2025-03-15")
            )
        ),
        lastChapter = "Chương 25",  // ĐÃ SỬA: lastChapter
        readTime = "Vừa xong"
    ),
    HistoryItem(
        story = Story(
            id = "9",
            title = "Thiên Đạo Đồ Thư Quán",
            author = "Hoành Tảo Thiên Nhai",
            status = "Đang tiến hành",
            rating = 4.8f,
            imageUrl = "https://i.imgur.com/library_of_heaven.jpg",
            likes = "280k",
            views = "2.8M",
            totalChapters = 800,
            genreIds = listOf(1, 4),
            genres = listOf("Hành động", "Huyền huyễn"),
            description = "Trương Huyền xuyên không thành giáo viên...",
            chapters = listOf(
                Chapter(id = 150, number = 150, title = "Thư viện mở rộng", uploadDate = "2025-06-08")
            )
        ),
        lastChapter = "Chương 150",  // ĐÃ SỬA
        readTime = "1 giờ trước"
    ),
    HistoryItem(
        story = Story(
            id = "0",
            title = "Vạn Cổ Tối Cường Tông",
            author = "Tác giả B",
            status = "Đã hoàn thành",
            rating = 4.9f,
            imageUrl = "https://i.imgur.com/strongest_sect_cover.jpg",
            likes = "300k",
            views = "3.5M",
            totalChapters = 100,
            genreIds = listOf(1, 3, 4),
            genres = listOf("Hành động", "Tiên hiệp", "Huyền huyễn"),
            description = "Một phế vật bị trục xuất...",
            chapters = listOf(
                Chapter(id = 50, number = 50, title = "Tông môn hưng thịnh", uploadDate = "2024-12-30")
            )
        ),
        lastChapter = "Chương 50",  // ĐÃ SỬA
        readTime = "Hôm qua"
    ),
    HistoryItem(
        story = Story(
            id = "1",
            title = "Phàm Nhân Tu Tiên",
            author = "Vong Ngữ",
            status = "Đang tiến hành",
            rating = 4.8f,
            imageUrl = "https://i.imgur.com/mortal_cultivation_cover.jpg",
            likes = "500k",
            views = "5.8M",
            totalChapters = 2450,
            genreIds = listOf(3, 4),
            genres = listOf("Tiên hiệp", "Huyền huyễn"),
            description = "Một phàm nhân không có thiên phú...",
            chapters = listOf(
                Chapter(id = 10, number = 10, title = "Luyện khí tầng 10", uploadDate = "2025-02-01")
            )
        ),
        lastChapter = "Chương 10",  // ĐÃ SỬA
        readTime = "1 tuần trước"
    )
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