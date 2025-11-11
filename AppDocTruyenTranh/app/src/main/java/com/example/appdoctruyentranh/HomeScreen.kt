// File: HomeScreen.kt
package com.example.appdoctruyentranh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.appdoctruyentranh.model.BannerItem
import com.example.appdoctruyentranh.model.HomeUiState
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.HomeViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.res.painterResource
import com.example.appdoctruyentranh.model.Chapter

val BannerHeight = 200.dp
var chapters = mutableStateListOf<Chapter>()
val totalChapters: Int
    get() = chapters.size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = { AppHeader() },
        bottomBar = { AppBottomNavigationBar(navController) }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            indicator = {}
        ) {
            SmartListState(
                uiState = uiState.toUiState(),
                onRetry = { viewModel.refresh() },
                emptyIcon = Icons.Outlined.Refresh,
                emptyTitle = "Chưa có dữ liệu",
                emptyMessage = "Vuốt xuống để làm mới nhé!",
                buttonText = "Thử lại"
            ) { data ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // 1. Banner
                    if (data.banners.isNotEmpty()) {
                        item { SectionHeader("Đề xuất") }
                        item {
                            HomeBannerCarousel(
                                banners = data.banners,
                                onBannerClick = { navController.navigate("manga_detail/${it.id}") }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }

                    // 2. Mới cập nhật
                    data.newUpdates.takeIf { it.isNotEmpty() }?.let { list ->
                        item { StorySection("Mới Cập Nhật", list, navController) }
                    } ?: item { EmptySectionPlaceholder("Mới Cập Nhật") }

                    // 3. Xem nhiều nhất
                    data.mostViewed.takeIf { it.isNotEmpty() }?.let { list ->
                        item { StorySection("Xem Nhiều Nhất", list, navController) }
                    } ?: item { EmptySectionPlaceholder("Xem Nhiều Nhất") }

                    // 4. Truyện hoàn thành
                    data.completedStories.takeIf { it.isNotEmpty() }?.let { list ->
                        item { StorySection("Truyện Hoàn Thành", list, navController) }
                    } ?: item { EmptySectionPlaceholder("Truyện Hoàn Thành") }
                }
            }
        }
    }
}

// === GIỮ NGUYÊN HẾT CÁC HÀM DƯỚI ĐÂY (không thay đổi giao diện) ===

private fun HomeUiState.toUiState(): com.example.appdoctruyentranh.model.UiState<HomeUiState> = when {
    isLoading -> com.example.appdoctruyentranh.model.UiState.Loading
    errorMessage != null -> com.example.appdoctruyentranh.model.UiState.Error(errorMessage!!)
    banners.isEmpty() && newUpdates.isEmpty() && mostViewed.isEmpty() && completedStories.isEmpty() ->
        com.example.appdoctruyentranh.model.UiState.Empty
    else -> com.example.appdoctruyentranh.model.UiState.Success(this)
}

@Composable
fun EmptySectionPlaceholder(title: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader(title)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có truyện", color = Color.Gray)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "Xem tất cả",
            color = PrimaryColor,
            fontSize = 14.sp,
            modifier = Modifier.clickable { /* TODO */ }
        )
    }
}

@Composable
fun HomeBannerCarousel(banners: List<Story>, onBannerClick: (Story) -> Unit) {
    val pageCount = banners.size
    val pagerState = remember { mutableStateOf(0) }

    Column {
        LazyRow(
            modifier = Modifier.height(BannerHeight),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(banners) { index, story ->
                Card(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .clickable { onBannerClick(story) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = story.imageUrl,
                                placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                story.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            if (story.author.isNotEmpty()) {
                                Text(
                                    story.author,
                                    color = Color.White.copy(0.8f),
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dấu chấm chỉ trang
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.value) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (index == pagerState.value) PrimaryColor else Color.LightGray)
                        .clickable { pagerState.value = index }
                )
                if (index < pageCount - 1) Spacer(Modifier.width(6.dp))
            }
        }
    }
}

@Composable
fun StorySection(title: String, stories: List<Story>, navController: NavHostController) {
    Column {
        SectionHeader(title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stories) { story ->
                StoryCard(story = story) {
                    navController.navigate("manga_detail/${story.id}")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun StoryCard(story: Story, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(
                    model = story.imageUrl,
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    story.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
                Text(
                    "Chap ${story.totalChapters}",
                    color = PrimaryColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}