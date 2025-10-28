package com.example.appdoctruyentranh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.AppHeader
import com.example.appdoctruyentranh.AppBottomNavigationBar
import com.example.appdoctruyentranh.PrimaryColor
// Import Story data class từ file khác nếu nó không ở đây
// import com.example.appdoctruyentranh.Story

// --- Data Model và Mock Data (Sử dụng lại Story) ---
// Giả sử Story đã được định nghĩa trong AppComponents.kt hoặc HomeScreen.kt
val mockFavoriteStories = listOf(
    Story(1, "Toàn Cầu Quỷ Dị Thời Đại"),
    Story(9, "Thiên Đạo Đồ Thư Quán"),
    Story(10, "Vạn Cổ Tối Cường Tông"),
    Story(11, "Phàm Nhân Tu Tiên"),
    Story(12, "Ta Chỉ Là Một Tiên Nhân"),
)

// =========================================================================
// Màn hình Chính: FavoriteScreen
// =========================================================================

@Composable
fun FavoriteScreen(navController: NavHostController) {
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
        bottomBar = { AppBottomNavigationBar(navController = navController) } // Sử dụng Bottom Nav chung
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tiêu đề
            Text(
                text = "Truyện Yêu Thích",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // Danh sách truyện yêu thích
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (mockFavoriteStories.isEmpty()) {
                    item {
                        EmptyListMessage(message = "Bạn chưa thêm truyện nào vào danh sách yêu thích.")
                    }
                } else {
                    items(mockFavoriteStories) { story ->
                        FavoriteStoryItem(
                            story = story,
                            onStoryClick = {
                                // navController.navigate("manga_detail/${story.id}")
                            },
                            onRemoveClick = {
                                println("Removed: ${story.title}")
                            }
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Các Composable Thành phần
// =========================================================================

@Composable
fun FavoriteStoryItem(story: Story, onStoryClick: () -> Unit, onRemoveClick: () -> Unit) {
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
                    imageVector = Icons.Default.Favorite,
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
                text = story.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Chương: 150", // Thông tin thêm
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // 3. Nút Xóa
        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Xóa khỏi yêu thích",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun EmptyListMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Empty",
            tint = Color.LightGray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFavoriteScreen() {
    // Tạo mock NavController cho Preview
    FavoriteScreen(navController = rememberNavController())
}