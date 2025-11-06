package com.example.appdoctruyentranh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appdoctruyentranh.model.BannerItem
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.HomeViewModel

val BannerHeight = 200.dp


// =========================================================================
// Màn hình Chính: HomeScreen (dùng ViewModel)
// =========================================================================
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    HomeScreenContent(
        uiState = uiState,
        navController = navController,
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
fun HomeScreenContent(
    uiState: com.example.appdoctruyentranh.model.HomeUiState,
    navController: NavHostController,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = { AppHeader() },
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Lỗi: ${uiState.errorMessage}",
                            color = Color.Red,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Text("Thử lại")
                        }
                    }
                }
            }

            uiState.isEmpty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có dữ liệu để hiển thị",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 1. Banner
                    if (uiState.banners.isNotEmpty()) {
                        item { SectionHeader(title = "Đề xuất") }
                        item {
                            HomeBannerCarousel(
                                banners = uiState.banners,
                                onBannerClick = { banner ->
                                    navController.navigate("manga_detail/${banner.id}")
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // 2. Mới cập nhật
                    if (uiState.newUpdates.isNotEmpty()) {
                        item {
                            StorySection(
                                title = "Mới Cập Nhật",
                                stories = uiState.newUpdates,
                                onStoryClick = { story ->
                                    navController.navigate("manga_detail/${story.id}")
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    } else {
                        item { EmptySectionPlaceholder("Mới Cập Nhật") }
                    }

                    // 3. Xem nhiều nhất
                    if (uiState.mostViewed.isNotEmpty()) {
                        item {
                            StorySection(
                                title = "Xem Nhiều Nhất",
                                stories = uiState.mostViewed,
                                onStoryClick = { story ->
                                    navController.navigate("manga_detail/${story.id}")
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    } else {
                        item { EmptySectionPlaceholder("Xem Nhiều Nhất") }
                    }

                    // 4. Truyện hoàn thành
                    if (uiState.completedStories.isNotEmpty()) {
                        item {
                            StorySection(
                                title = "Truyện Hoàn Thành",
                                stories = uiState.completedStories,
                                onStoryClick = { story ->
                                    navController.navigate("manga_detail/${story.id}")
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    } else {
                        item { EmptySectionPlaceholder("Truyện Hoàn Thành") }
                    }
                }
            }
        }
    }
}

// =========================================================================
// Placeholder khi không có dữ liệu
// =========================================================================
@Composable
fun EmptySectionPlaceholder(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Xem tất cả", color = PrimaryColor, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Chưa có dữ liệu", color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// =========================================================================
// Các Composable giữ nguyên (chỉ thay mock → real data)
// =========================================================================
@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Xem tất cả",
            color = PrimaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { /* TODO: Navigate to All */ }
        )
    }
}

@Composable
fun HomeBannerCarousel(banners: List<BannerItem>, onBannerClick: (BannerItem) -> Unit) {
    val pagerState = remember { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(BannerHeight),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(banners) { index, item ->
                BannerItemCard(item = item) {
                    pagerState.value = index
                    onBannerClick(item)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            banners.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (index == pagerState.value) PrimaryColor else Color.LightGray,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .clickable { pagerState.value = index }
                )
                if (index < banners.lastIndex) Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

@Composable
fun BannerItemCard(item: BannerItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StorySection(
    title: String,
    stories: List<Story>,
    onStoryClick: (Story) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stories) { story ->
                StoryItem(story = story, onClick = { onStoryClick(story) })
            }
        }
    }
}