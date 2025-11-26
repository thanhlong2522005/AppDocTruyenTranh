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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdoctruyentranh.model.Story

import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.model.Chapter
import androidx.lifecycle.viewmodel.compose.viewModel // ⭐️ Cần import này
import coil.compose.rememberAsyncImagePainter
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.HistoryViewModel

data class HistoryItem(
    val story: Story,
    val lastChapter: String, // Ví dụ: "Chương 25"
    val readTime: String    // Ví dụ: "Vừa xong"
)

@Composable
fun HistoryScreen(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val isAdmin by authViewModel.isAdmin.collectAsState()
    
    LaunchedEffect(Unit) {
        authViewModel.checkAdminStatus()
    }

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
        bottomBar = { AppBottomNavigationBar(navController = navController, isAdmin = isAdmin) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Lịch sử Đọc",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
                historyList.isEmpty() -> {
                    EmptyHistoryMessage(message = "Bạn chưa đọc truyện nào gần đây.")
                }
                else -> {
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
                                    navController.navigate("manga_detail/${item.story.id}")
                                },
                                onDeleteClick = {
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
        Card(
            modifier = Modifier
                .size(70.dp)
                .clickable(onClick = onStoryClick),
            shape = RoundedCornerShape(4.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = item.story.imageUrl,
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }


        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f).clickable(onClick = onStoryClick)
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
                modifier = Modifier.clickable(onClick = onResumeReadClick)
            )
            Text(
                text = "Đọc lần cuối: ${item.readTime}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

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
