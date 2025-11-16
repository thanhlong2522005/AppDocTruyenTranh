// File: DownloadManagerScreen.kt
package com.example.appdoctruyentranh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appdoctruyentranh.model.DownloadItem
import com.example.appdoctruyentranh.viewmodel.DownloadStatus
import com.example.appdoctruyentranh.viewmodel.DownloadViewModel
import com.example.appdoctruyentranh.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen(
    navController: NavController,
    viewModel: DownloadViewModel = viewModel()
) {
    val downloadList by viewModel.downloadList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Tải xuống", fontWeight = FontWeight.Bold) },
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
        if (downloadList.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Download,
                title = "Chưa có truyện đã tải",
                message = "Tải truyện về để đọc ngoại tuyến (offline) nhé!",
                buttonText = null,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(downloadList, key = { it.id }) { item ->
                    DownloadItemRow(item = item, viewModel = viewModel) {
                        // TODO: Logic điều hướng đến màn hình đọc offline
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun DownloadItemRow(
    item: DownloadItem,
    viewModel: DownloadViewModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.status == DownloadStatus.COMPLETED) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.storyTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Chương: ${item.chapter.title}",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Hiển thị trạng thái
        when (item.status) {
            DownloadStatus.COMPLETED -> {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = "Hoàn tất",
                    tint = Color.Green,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.deleteDownloadedItem(item.id) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            }
            DownloadStatus.DOWNLOADING -> {
                CircularProgressIndicator(
                    progress = item.progress / 100f,
                    color = PrimaryColor,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${item.progress}%", fontSize = 14.sp, color = PrimaryColor)
            }
            DownloadStatus.PENDING -> {
                Text("Chờ", fontSize = 14.sp, color = Color.Gray)
            }
            DownloadStatus.ERROR -> {
                Text("Lỗi", fontSize = 14.sp, color = Color.Red)
            }
        }
    }
}

// Hàm EmptyState được lấy từ SmartListState.kt để đảm bảo tính nhất quán.
// Cần đảm bảo hàm này được import đúng hoặc copy sang đây nếu không thể import.
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    buttonText: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // ... (sử dụng lại code EmptyState từ SmartListState.kt)
    // Tôi sẽ giả định nó được import đúng.
    // Nếu bạn gặp lỗi, hãy đặt lại code EmptyState trong file này.
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.4f),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        buttonText?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onClick?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(buttonText)
            }
        }
    }
}