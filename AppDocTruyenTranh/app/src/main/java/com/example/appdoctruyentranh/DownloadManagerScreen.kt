@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appdoctruyentranh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.google.firebase.auth.FirebaseAuth

// --- Mock Data ---
data class DownloadItem(
    val story: Story,
    val totalChapters: Int,
    val downloadedChapters: Int,
    val size: String
)

val mockDownloadedStories = listOf(
    DownloadItem(Story(1, "Toàn Cầu Quỷ Dị Thời Đại", latestChapter = "Chương 25"), 150, 25, "50MB"),
    DownloadItem(Story(2, "Vạn Cổ Tối Cường Tông", latestChapter = "Chương 50"), 100, 100, "150MB"),
    DownloadItem(Story(3, "Phàm Nhân Tu Tiên", latestChapter = "Chương 10"), 10, 5, "10MB"),
)

@Composable
fun DownloadManagerScreen(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        PleaseLoginScreen(navController = navController, title = "Truyện đã tải")
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Truyện đã tải", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mockDownloadedStories.isEmpty()) {
                        item {
                            EmptyState(message = "Bạn chưa tải truyện nào để đọc offline.")
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tổng số truyện: ${mockDownloadedStories.size}", color = PrimaryColor, fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = { /* TODO: Xóa tất cả */ }) {
                                    Text("Xóa tất cả", color = Color.Red)
                                }
                            }
                        }
                        items(mockDownloadedStories) { item ->
                            DownloadStoryItem(
                                item = item,
                                onStoryClick = {
                                    // Mở chi tiết truyện hoặc mở chương đã tải gần nhất
                                    navController.navigate("manga_detail/${item.story.id}")
                                },
                                onDeleteClick = {
                                    println("Deleted download: ${item.story.title}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadStoryItem(item: DownloadItem, onStoryClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable(onClick = onStoryClick)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
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
                Icon(Icons.Default.CloudDownload, contentDescription = "Cover", tint = PrimaryColor.copy(alpha = 0.5f))
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
                text = "Đã tải: ${item.downloadedChapters}/${item.totalChapters} Chương",
                fontSize = 13.sp,
                color = PrimaryColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Dung lượng: ${item.size}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // 3. Nút Xóa
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Xóa khỏi máy",
                tint = Color.Red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDownloadManagerScreen() {
    DownloadManagerScreen(navController = rememberNavController())
}