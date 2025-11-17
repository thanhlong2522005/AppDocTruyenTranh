@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalPagerApi::class
)

package com.example.appdoctruyentranh

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.appdoctruyentranh.viewmodel.ChapterReaderViewModel
import com.example.appdoctruyentranh.viewmodel.ReadingFont
import com.example.appdoctruyentranh.viewmodel.ReadingMode
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
@Composable
fun getCustomFontFamily(readingFont: ReadingFont): FontFamily {
    return when (readingFont) {
        ReadingFont.SERIF -> FontFamily.Serif
        ReadingFont.SANS_SERIF -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
}

@Composable
fun ReadScreen(
    navController: NavHostController,
    mangaId: String,        // String từ Story.id
    chapterId: String,      // String từ route → parse thành Int
    viewModel: ChapterReaderViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        PleaseLoginScreen(navController = navController, title = "Đọc truyện")
    } else {
        val chapterIdInt = remember(chapterId) { chapterId.toIntOrNull() ?: 1 }

        val isMenuVisible by viewModel.isMenuVisible.collectAsState()
        val isDarkMode by viewModel.isDarkMode.collectAsState()
        val currentFont by viewModel.currentFont.collectAsState()
        val readingMode by viewModel.readingMode.collectAsState()
        val displayFontFamily = getCustomFontFamily(currentFont)

        val chapterData by viewModel.chapterData.collectAsState()
        val story by viewModel.currentStory.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.errorMessage.collectAsState()

        val bgColor = if (isDarkMode) Color(0xFF121212) else Color.White

        var showReportDialog by remember { mutableStateOf(false) }
        var showCommentDialog by remember { mutableStateOf(false) }

        val listState = rememberLazyListState()
        val pagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(mangaId, chapterIdInt) {
            viewModel.loadChapter(mangaId, chapterIdInt)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
            return
        }

        error?.let {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(it, color = Color.Red, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadChapter(mangaId, chapterIdInt) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Thử lại")
                    }
                }
            }
            return
        }

        if (chapterData.pages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có trang nào", color = Color.Gray)
            }
            return
        }

        val currentPage = when (readingMode) {
            ReadingMode.VERTICAL_SCROLL -> listState.firstVisibleItemIndex + 1
            ReadingMode.HORIZONTAL_PAGINATION -> pagerState.currentPage + 1
        }

        val currentChapter = story?.chapters?.find { it.number == chapterIdInt }
        val chapterTitle = currentChapter?.title?.takeIf { it.isNotBlank() } ?: "Chương $chapterIdInt"
        val totalChapters = story?.chapters?.size ?: 1
        val hasPrev = chapterIdInt > 1
        val hasNext = chapterIdInt < totalChapters

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
                when (readingMode) {
                    ReadingMode.VERTICAL_SCROLL -> {
                        VerticalReader(
                            pages = chapterData.pages,
                            isDarkMode = isDarkMode,
                            listState = listState,
                            onToggleMenu = { viewModel.toggleMenuVisibility() }
                        )
                    }
                    ReadingMode.HORIZONTAL_PAGINATION -> {
                        HorizontalPagerReader(
                            pages = chapterData.pages,
                            isDarkMode = isDarkMode,
                            pagerState = pagerState,
                            onToggleMenu = { viewModel.toggleMenuVisibility() }
                        )
                    }
                }

                MenuTopBar(
                    isVisible = isMenuVisible,
                    title = "${story?.title ?: "Truyện"} - $chapterTitle",
                    onBack = { navController.popBackStack() },
                    onSettingClick = { navController.navigate("settings") }
                )

                MenuBottomBar(
                    isVisible = isMenuVisible,
                    chapterId = chapterIdInt,
                    totalChapters = totalChapters,
                    totalPages = chapterData.pages.size,
                    currentPage = currentPage.coerceAtMost(chapterData.pages.size),
                    hasPrev = hasPrev,
                    hasNext = hasNext,
                    onPrevClick = {
                        if (hasPrev) {
                            coroutineScope.launch {
                                navController.navigate("read/$mangaId/${chapterIdInt - 1}")
                            }
                        }
                    },
                    onNextClick = {
                        if (hasNext) {
                            coroutineScope.launch {
                                navController.navigate("read/$mangaId/${chapterIdInt + 1}")
                            }
                        }
                    },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onReportClick = { showReportDialog = true },
                    onCommentClick = { showCommentDialog = true },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        if (showReportDialog) {
            SimpleAlertDialog(
                title = "Báo cáo lỗi",
                message = "Gửi báo cáo về chương này?",
                onDismiss = { showReportDialog = false }
            )
        }

        if (showCommentDialog) {
            SimpleAlertDialog(
                title = "Bình luận",
                message = "Tính năng bình luận đang phát triển.",
                onDismiss = { showCommentDialog = false }
            )
        }
    }
}

@Composable
fun VerticalReader(
    pages: List<String>,
    isDarkMode: Boolean,
    listState: LazyListState,
    onToggleMenu: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onToggleMenu,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentPadding = PaddingValues(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(pages) { imageUrl ->
            MangaPageImage(imageUrl = imageUrl, isDarkMode = isDarkMode)
            Spacer(Modifier.height(2.dp))
        }
        item {
            Text(
                text = "--- Hết chương ---",
                color = Color.Gray,
                modifier = Modifier.padding(24.dp),
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerReader(
    pages: List<String>,
    isDarkMode: Boolean,
    pagerState: PagerState,
    onToggleMenu: () -> Unit
) {
    HorizontalPager(
        count = pages.size,
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onToggleMenu,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) { pageIndex ->
        MangaPageImage(imageUrl = pages[pageIndex], isDarkMode = isDarkMode)
    }
}

@Composable
fun MangaPageImage(imageUrl: String, isDarkMode: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .heightIn(min = 400.dp, max = 900.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
        )
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Trang truyện",
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkMode) Color.Black else Color.White),
            contentScale = ContentScale.Fit
        )
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
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = onSettingClick) {
                    Icon(Icons.Default.Settings, "Cài đặt", tint = Color.White)
                }
                IconButton(onClick = { /* Download */ }) {
                    Icon(Icons.Default.Download, "Tải xuống", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor.copy(alpha = 0.95f))
        )
    }
}

@Composable
fun MenuBottomBar(
    isVisible: Boolean,
    chapterId: Int,
    totalChapters: Int,
    totalPages: Int,
    currentPage: Int,
    hasPrev: Boolean,
    hasNext: Boolean,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trang: $currentPage/$totalPages", color = Color.White, fontSize = 12.sp)
                Text("Chương $chapterId/$totalChapters", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = onPrevClick, enabled = hasPrev) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Chương trước", tint = if (hasPrev) Color.White else Color.White.copy(0.5f))
                }
                IconButton(onClick = onToggleDarkMode) {
                    Icon(Icons.Default.DarkMode, "Dark mode", tint = Color.White)
                }
                IconButton(onClick = onReportClick) {
                    Icon(Icons.Default.Report, "Báo cáo", tint = Color.White)
                }
                IconButton(onClick = onCommentClick) {
                    Icon(Icons.Default.Comment, "Bình luận", tint = Color.White)
                }
                IconButton(onClick = onNextClick, enabled = hasNext) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Chương sau", tint = if (hasNext) Color.White else Color.White.copy(0.5f))
                }
            }
        }
    }
}

@Composable
fun SimpleAlertDialog(title: String, message: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                Spacer(Modifier.height(16.dp))
                Text(message, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(PrimaryColor)
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}