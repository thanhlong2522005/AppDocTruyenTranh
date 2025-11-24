// File: DownloadManagerScreen.kt (Đã sửa)
package com.example.appdoctruyentranh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.appdoctruyentranh.viewmodel.SettingsViewModel // ⭐ Thêm import này

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen(
    navController: NavController,
    viewModel: DownloadViewModel = viewModel(
        factory = DownloadViewModel.Factory(LocalContext.current)
    ),
    // ⭐ THÊM SETTINGS VIEWMODEL ĐỂ KÍCH HOẠT CHẾ ĐỘ TỐI
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(LocalContext.current)
    )
) {
    val downloadList by viewModel.downloadList.collectAsState()

    // ⭐ Lắng nghe trạng thái Dark Mode
    val settingsState by settingsViewModel.uiState.collectAsState()
    val isDarkMode = settingsState.isDarkMode // Cần thiết để kích hoạt Theme trong MainActivity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Tải xuống", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Icon sẽ tự đổi màu nếu không chỉ định tint
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // ⭐ SỬ DỤNG MÀU CHỦ ĐỀ
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // ⭐ SỬ DỤNG MÀU NỀN CHỦ ĐỀ
        containerColor = MaterialTheme.colorScheme.background
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
                contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sắp xếp các mục đã tải (COMPLETED) lên đầu
                val sortedList = downloadList.sortedWith(
                    compareByDescending<DownloadItem> { it.status == DownloadStatus.COMPLETED }
                        .thenByDescending { it.status == DownloadStatus.DOWNLOADING }
                        .thenBy { it.storyTitle }
                )

                items(sortedList, key = { it.id }) { item ->
                    DownloadItemRow(item = item, viewModel = viewModel) {
                        // TODO: Logic điều hướng đến màn hình đọc offline
                    }
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
    // Card sẽ tự động dùng màu của MaterialTheme (surface hoặc background tùy Theme)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp) // Chiều cao cố định
            .clickable(enabled = item.status == DownloadStatus.COMPLETED) { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Sử dụng màu surface của theme
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. Icon/Cover (70dp) ---
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
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Cover",
                        tint = PrimaryColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- 2. Thông tin (Weight 1f) ---
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // ⭐ DÒNG 1: TÊN TRUYỆN
                Text(
                    text = item.storyTitle, // ⭐ Đã kiểm tra: Sử dụng đúng storyTitle
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface // Sử dụng màu chữ phù hợp
                )
                Spacer(modifier = Modifier.height(4.dp))

                // ⭐ DÒNG 2: TÊN CHƯƠNG + TRẠNG THÁI TEXT
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Chương: ${item.chapter.title}", // ⭐ Đã kiểm tra: Sử dụng đúng chapter.title
                        fontSize = 13.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Hiển thị trạng thái text bổ sung
                    when (item.status) {
                        DownloadStatus.PENDING -> {
                            Text(
                                text = "(Chờ tải)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        DownloadStatus.ERROR -> {
                            Text(
                                text = "(Lỗi)",
                                fontSize = 12.sp,
                                color = Color.Red
                            )
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp)) // Thêm khoảng cách trước cột trạng thái

            // --- 3. Trạng thái & Xóa ---
            Box(modifier = Modifier.width(70.dp), contentAlignment = Alignment.CenterEnd) {
                when (item.status) {
                    DownloadStatus.COMPLETED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icon Tải xong nhỏ
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = "Hoàn tất",
                                tint = Color.Green,
                                modifier = Modifier.size(20.dp)
                            )
                            // Nút Xóa
                            IconButton(onClick = { viewModel.deleteDownloadedItem(item.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Xóa",
                                    tint = Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    DownloadStatus.DOWNLOADING -> {
                        // ⭐ Chỉ hiển thị Progress Indicator
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = item.progress / 100f,
                                color = PrimaryColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                            // Hiển thị phần trăm ngay trên/dưới progress bar nếu cần
                            Text(
                                text = "${item.progress}%",
                                fontSize = 10.sp,
                                color = PrimaryColor
                            )
                        }
                    }
                    DownloadStatus.PENDING, DownloadStatus.ERROR -> {
                        // Nếu lỗi hoặc chờ, chỉ hiển thị nút xóa
                        IconButton(onClick = { viewModel.deleteDownloadedItem(item.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Xóa",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Dùng màu chữ của theme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    buttonText: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), // Sử dụng màu chữ/icon của theme
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), // Sử dụng màu chữ/icon của theme
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