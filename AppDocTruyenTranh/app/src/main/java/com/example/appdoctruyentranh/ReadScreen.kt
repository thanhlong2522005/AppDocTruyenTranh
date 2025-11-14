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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appdoctruyentranh.viewmodel.ChapterReaderViewModel
import com.example.appdoctruyentranh.viewmodel.ReadingFont // Cần import từ ViewModel
// BỔ SUNG CÁC IMPORT CẦN THIẾT
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseAuth
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

// --- COMPOSABLE YÊU CẦU ĐĂNG NHẬP ---
@Composable
fun PleaseLoginScreen(navController: NavHostController, title: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Vui lòng đăng nhập để sử dụng tính năng này", textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("login") }) {
                Text("Đăng nhập / Đăng ký")
            }
        }
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
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        PleaseLoginScreen(navController = navController, title = "Đọc truyện")
    } else {
        // Giao diện cho người dùng đã đăng nhập
        val isMenuVisible by viewModel.isMenuVisible.collectAsState()
        val isDarkMode by viewModel.isDarkMode.collectAsState()
        val currentFont by viewModel.currentFont.collectAsState()
        val displayFontFamily = getCustomFontFamily(currentFont)
        val bgColor = if (isDarkMode) Color(0xFF121212) else Color.White
        var showReportDialog by remember { mutableStateOf(false) }
        var showCommentDialog by remember { mutableStateOf(false) }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.toggleMenuVisibility() },
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(mockPages) { page ->
                        MangaPagePlaceholder(
                            page = page,
                            isDarkMode = isDarkMode,
                            fontFamily = displayFontFamily
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    item {
                        Text(
                            text = "--- Hết chương ${chapterId} ---",
                            color = Color.Gray,
                            modifier = Modifier.padding(24.dp),
                            fontFamily = displayFontFamily
                        )
                    }
                }
                MenuTopBar(
                    isVisible = isMenuVisible,
                    title = "Chương $chapterId - Tên Truyện (ID: $mangaId)",
                    onBack = { navController.popBackStack() },
                    onSettingClick = { navController.navigate("settings") }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    MenuBottomBar(
                        isVisible = isMenuVisible,
                        chapterId = chapterId,
                        progress = progress,
                        onPrevClick = { viewModel.goToPreviousChapter(navController) },
                        onNextClick = { viewModel.goToNextChapter(navController) },
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onReportClick = { showReportDialog = true },
                        onCommentClick = { showCommentDialog = true },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        if (showReportDialog) {
            SimpleAlertDialog(
                title = "Báo cáo lỗi",
                message = "Bạn muốn báo cáo lỗi về chương này (ảnh bị hỏng, lỗi dịch thuật)?\nTính năng này sẽ gửi báo cáo đến quản trị viên.",
                onDismiss = { showReportDialog = false }
            )
        }

        if (showCommentDialog) {
            SimpleAlertDialog(
                title = "Bình luận",
                message = "Tính năng Bình luận sẽ được mở tại đây.",
                onDismiss = { showCommentDialog = false }
            )
        }
    }
}

// =========================================================================
// COMPONENTS
// =========================================================================

@Composable
fun MangaPagePlaceholder(page: Int, isDarkMode: Boolean, fontFamily: FontFamily) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(500.dp),
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
                Text(
                    text = "Trang $page",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = fontFamily
                )

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
                IconButton(onClick = onSettingClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Cài đặt đọc", tint = Color.White)
                }
                IconButton(onClick = { /* Xử lý tải xuống */ }) {
                    Icon(Icons.Default.Download, contentDescription = "Tải xuống", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PrimaryColor.copy(alpha = 0.95f),
                titleContentColor = Color.White
            )
        )
    }
}

@Composable
fun MenuBottomBar(
    isVisible: Boolean,
    chapterId: Int,
    progress: Float,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onReportClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor.copy(alpha = 0.95f))
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevClick,
                    enabled = chapterId > 1
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Chương trước", tint = if (chapterId > 1) Color.White else Color.White.copy(alpha = 0.5f))
                }
                IconButton(onClick = onToggleDarkMode) {
                    Icon(Icons.Default.DarkMode, contentDescription = "Chế độ tối", tint = Color.White)
                }
                IconButton(onClick = onReportClick) {
                    Icon(Icons.Default.Report, contentDescription = "Báo cáo lỗi", tint = Color.White)
                }
                IconButton(onClick = onCommentClick) {
                    Icon(Icons.Default.Comment, contentDescription = "Bình luận", tint = Color.White)
                }
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