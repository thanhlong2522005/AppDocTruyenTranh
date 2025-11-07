@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class // Đảm bảo có ExperimentalAnimationApi
)

package com.example.appdoctruyentranh

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appdoctruyentranh.viewmodel.ChapterReaderViewModel
import com.example.appdoctruyentranh.viewmodel.ReadingFont // Cần import từ ViewModel
// BỔ SUNG CÁC IMPORT CẦN THIẾT
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
// SỬ DỤNG PrimaryColor TỪ CommonComposables
import com.example.appdoctruyentranh.PrimaryColor

// --- Mock Data ---
val mockPages = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
const val MAX_CHAPTER_ID = 100 // Giả lập tổng số chương

// Hàm helper để chuyển enum Font thành FontFamily (Giả định)
@Composable
fun getCustomFontFamily(readingFont: ReadingFont): FontFamily {
    // Trong thực tế, bạn sẽ dùng font cụ thể trong R.font
    return when (readingFont) {
        ReadingFont.SERIF -> FontFamily.Serif
        ReadingFont.SANS_SERIF -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
}

// =========================================================================
// MÀN HÌNH CHÍNH: ReadScreen
// =========================================================================

@Composable
fun ReadScreen(
    navController: NavHostController,
    mangaId: Int,
    chapterId: Int,
    viewModel: ChapterReaderViewModel = viewModel()
) {
    // State UI
    val isMenuVisible by viewModel.isMenuVisible.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentFont by viewModel.currentFont.collectAsState()

    // Áp dụng font
    val displayFontFamily = getCustomFontFamily(currentFont)

    // Sử dụng màu nền tối hoặc trắng
    val bgColor = if (isDarkMode) Color(0xFF121212) else Color.White

    // State cho Dialog
    var showReportDialog by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf(false) }

    // List state để quản lý cuộn và hiển thị thanh trượt tiến trình
    val listState = rememberLazyListState()
    val progress: Float = remember { derivedStateOf {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.totalItemsCount == 0) 0f else {
            val lastItemIndex = listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size
            (lastItemIndex).toFloat() / layoutInfo.totalItemsCount.toFloat()
        }
    } }.value.coerceIn(0f, 1f)


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) { paddingValues ->
        // Nội dung chính của truyện (Scrollable)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(paddingValues)
        ) {
            // LazyColumn cho chế độ cuộn dọc
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.toggleMenuVisibility() }, // Click vào để ẩn/hiện menu
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(mockPages) { page ->
                    MangaPagePlaceholder(
                        page = page,
                        isDarkMode = isDarkMode,
                        fontFamily = displayFontFamily // Áp dụng font
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                item {
                    // Thông báo kết thúc chương
                    Text(
                        text = "--- Hết chương ${chapterId} ---",
                        color = Color.Gray,
                        modifier = Modifier.padding(24.dp),
                        fontFamily = displayFontFamily // Áp dụng font
                    )
                }
            }

            // Thanh Menu TOP (Header)
            MenuTopBar(
                isVisible = isMenuVisible,
                title = "Chương $chapterId - Tên Truyện (ID: $mangaId)",
                onBack = { navController.popBackStack() },
                onSettingClick = { navController.navigate("settings") }
            )

            // Thanh Menu BOTTOM (Footer) - ĐÃ SỬA LỖI ALIGN BẰNG CÁCH SỬ DỤNG BỌC BOX
            Box(
                modifier = Modifier
                    .fillMaxSize() // Chiếm toàn bộ không gian Box cha
            ) {
                MenuBottomBar(
                    isVisible = isMenuVisible,
                    chapterId = chapterId,
                    progress = progress, // Truyền tiến trình thực tế
                    onPrevClick = { viewModel.goToPreviousChapter(navController) },
                    onNextClick = { viewModel.goToNextChapter(navController) },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onReportClick = { showReportDialog = true }, // Mở Dialog
                    onCommentClick = { showCommentDialog = true }, // Mở Dialog
                    // ĐẶT CĂN CHỈNH VÀO MODIFIER CỦA CHÍNH COMPOSABLE NÀY
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // DIALOG: Báo cáo lỗi / Phản hồi
    if (showReportDialog) {
        SimpleAlertDialog(
            title = "Báo cáo lỗi",
            message = "Bạn muốn báo cáo lỗi về chương này (ảnh bị hỏng, lỗi dịch thuật)?\nTính năng này sẽ gửi báo cáo đến quản trị viên.",
            onDismiss = { showReportDialog = false }
        )
    }

    // DIALOG: Bình luận
    if (showCommentDialog) {
        SimpleAlertDialog(
            title = "Bình luận",
            message = "Tính năng Bình luận sẽ được mở tại đây.",
            onDismiss = { showCommentDialog = false }
        )
    }
}

// =========================================================================
// COMPONENTS
// =========================================================================

// Placeholder cho một trang truyện (Thêm tham số fontFamily)
@Composable
fun MangaPagePlaceholder(page: Int, isDarkMode: Boolean, fontFamily: FontFamily) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(500.dp), // Chiều cao cố định giả lập trang
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF212121) else Color(0xFFF0F0F0)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Trang truyện $page",
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Demo cho font size (có thể thay đổi dựa trên setting)
                Text(
                    text = "Trang $page",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = fontFamily
                )

                // Demo cho font size
                Text(
                    text = "A A A",
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontSize = 32.sp,
                    fontFamily = fontFamily
                )
            }
        }
    }
}

// Menu Top (Header)
@Composable
fun MenuTopBar(
    isVisible: Boolean,
    title: String,
    onBack: () -> Unit,
    onSettingClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        TopAppBar(
            title = {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                // Tùy chọn (Menu cài đặt)
                IconButton(onClick = onSettingClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Cài đặt đọc", tint = Color.White)
                }
                // Tải xuống
                IconButton(onClick = { /* Xử lý tải xuống */ }) {
                    Icon(Icons.Default.Download, contentDescription = "Tải xuống", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PrimaryColor.copy(alpha = 0.95f), // Màu đậm hơn một chút
                titleContentColor = Color.White
            )
        )
    }
}

// Menu Bottom (Footer)
@Composable
fun MenuBottomBar(
    isVisible: Boolean,
    chapterId: Int,
    progress: Float, // Tiến trình thực tế
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onReportClick: () -> Unit,
    onCommentClick: () -> Unit,
    // THÊM MODIFIER ĐỂ NHẬN CĂN CHỈNH TỪ BOX CHỦ
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier.fillMaxWidth() // Nhận modifier.align(Alignment.BottomCenter) từ Box cha
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor.copy(alpha = 0.95f))
                .padding(vertical = 8.dp)
        ) {
            // Hàng 1: Thanh tiến trình
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    // Hiển thị Trang hiện tại/Tổng số trang
                    text = "Trang: ${((progress * mockPages.size).toInt() + 1).coerceAtMost(mockPages.size)}/${mockPages.size}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "Chương $chapterId",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Slider(
                value = progress,
                onValueChange = { /* TODO: Xử lý thay đổi trang bằng thanh trượt (chuyển listState) */ },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.8f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Hàng 2: Điều hướng và Tùy chỉnh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Chương trước
                IconButton(
                    onClick = onPrevClick,
                    enabled = chapterId > 1
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Chương trước", tint = if (chapterId > 1) Color.White else Color.White.copy(alpha = 0.5f))
                }
                // Nút Dark Mode
                IconButton(onClick = onToggleDarkMode) {
                    Icon(Icons.Default.DarkMode, contentDescription = "Chế độ tối", tint = Color.White)
                }
                // Nút Báo cáo (Tính năng nâng cao)
                IconButton(onClick = onReportClick) {
                    Icon(Icons.Default.Report, contentDescription = "Báo cáo lỗi", tint = Color.White)
                }
                // Nút Bình luận (Tính năng nâng cao)
                IconButton(onClick = onCommentClick) {
                    Icon(Icons.Default.Comment, contentDescription = "Bình luận", tint = Color.White)
                }
                // Nút Chương sau
                IconButton(
                    onClick = onNextClick,
                    enabled = chapterId < MAX_CHAPTER_ID
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Chương sau", tint = if (chapterId < MAX_CHAPTER_ID) Color.White else Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// Dialog đơn giản (thay thế cho alert())
@Composable
fun SimpleAlertDialog(title: String, message: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(message, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}