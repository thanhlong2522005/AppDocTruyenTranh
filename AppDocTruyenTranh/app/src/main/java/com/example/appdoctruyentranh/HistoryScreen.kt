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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel // ⭐️ Cần import này
import com.example.appdoctruyentranh.viewmodel.HistoryViewModel

data class HistoryItem(
    val story: Story,
    val lastChapter: String, // Ví dụ: "Chương 25"
    val readTime: String    // Ví dụ: "Vừa xong"
)

// ⭐️ Loại bỏ mockHistoryStories
// val mockHistoryStories = listOf(...)

// =========================================================================
// Màn hình Chính: HistoryScreen (Đã sửa để dùng ViewModel)
// =========================================================================

@Composable
fun HistoryScreen(navController: NavHostController) {
    // ⭐️ Khởi tạo và theo dõi ViewModel
    val viewModel: HistoryViewModel = viewModel()
    val historyList by viewModel.historyList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.loadReadHistory()
    }
    Scaffold(
        topBar = {
            AppHeader(
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(16.dp)
                            .clickable { navController.popBackStack() }
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
            // ... (Phần tiêu đề giữ nguyên)
            Text(
                text = "Lịch sử Đọc",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            when {
                isLoading -> {
                    // ⭐️ Hiển thị Loading
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
                historyList.isEmpty() -> {
                    // ⭐️ Hiển thị thông báo rỗng
                    EmptyHistoryMessage(message = "Bạn chưa đọc truyện nào gần đây.")
                }
                else -> {
                    // ⭐️ Hiển thị danh sách thực
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(historyList) { item ->
                            HistoryStoryItem(
                                item = item,
                                onStoryClick = {
                                    navController.navigate("manga_detail/${item.story.id}")
                                },
                                onResumeReadClick = {
                                    // Chuyển hướng đến chương đang đọc dở (cần lấy chapterId thực)
                                    // Tạm thời điều hướng đến chi tiết truyện
                                    navController.navigate("manga_detail/${item.story.id}")
                                },
                                onDeleteClick = {
                                    // ⭐️ Gọi hàm xóa từ ViewModel
                                    viewModel.removeHistoryItem(item.story.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// Các Composable Thành phần Lịch sử (Chỉnh sửa tham số)
// =========================================================================

@Composable
fun HistoryStoryItem(
    item: HistoryItem,
    onStoryClick: () -> Unit,
    onResumeReadClick: () -> Unit, // Thêm hành động Đọc tiếp
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ... (Phần Ảnh bìa giữ nguyên)
        Card(
            modifier = Modifier.size(70.dp).clickable(onClick = onStoryClick), // Bấm vào bìa -> chi tiết
            shape = RoundedCornerShape(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // ⭐️ Thay thế bằng Coil Image nếu có imageUrl thực tế
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Cover",
                    tint = PrimaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Thông tin
        Column(
            modifier = Modifier.weight(1f).clickable(onClick = onStoryClick) // Bấm vào text -> chi tiết
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
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onResumeReadClick) // Bấm vào đây -> đọc tiếp
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

// ... (Giữ nguyên EmptyHistoryMessage và Preview)

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