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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
// Các components và data classes dùng chung được truy cập tự động
// (Giả định AppHeader, AppBottomNavigationBar, PrimaryColor, Story, StoryItem nằm trong AppComponents.kt)

// --- Constants và Data Model Riêng ---
val BannerHeight = 200.dp

// KHÔNG CẦN định nghĩa lại data class Story ở đây (đã có trong AppComponents.kt)

data class BannerItem(
    val id: Int,
    val title: String,
    val imageUrl: Int // Vẫn giữ Int, dùng Color/Icon placeholder
)

val mockStories = listOf(
    Story(1, "Toàn Cầu Quỷ Dị Thời Đại"),
    Story(2, "Nguyên Lai Ta Là Tà Tu Tiên Đại Lão"),
    Story(3, "Đệ tử tu luyện"),
    Story(4, "Ngã Dục Phong Thiên"),
    Story(5, "Ta Làm Đạo Sĩ Những Năm Kia"),
    Story(6, "Người Khác Tu Tiên"),
    Story(7, "Sư Huynh Luôn Luôn Bảo Vệ Ta"),
    Story(8, "Chân Linh Cửu Biến"),
)

val mockBanners = listOf(
    BannerItem(1, "Trở về thời thiếu niên để xưng vương!", 0),
    BannerItem(2, "Banner truyện mới 2", 0),
    BannerItem(3, "Banner truyện mới 3", 0),
)

// =========================================================================
// Màn hình Chính: HomeScreen
// =========================================================================

@Composable
fun HomeScreen(navController: NavHostController) {
    HomeScreenContent(navController = navController)
}

@Composable
fun HomeScreenContent(navController: NavHostController) {
    Scaffold(
        topBar = { AppHeader() }, // Dùng component chung
        bottomBar = { AppBottomNavigationBar(navController = navController) } // Dùng component chung
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Banner Carousel
            item { SectionHeader(title = "Đề xuất") }
            item {
                HomeBannerCarousel(
                    banners = mockBanners,
                    onBannerClick = { bannerItem ->
                        // SỬA: Điều hướng đến màn hình chi tiết khi nhấn Banner
                        navController.navigate("manga_detail/${bannerItem.id}")
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 2. Mới Cập Nhật
            item {
                StorySection(
                    title = "Mới Cập Nhật",
                    stories = mockStories.subList(0, 4),
                    onStoryClick = { story ->
                        // SỬA: Điều hướng đến màn hình chi tiết khi nhấn Truyện
                        navController.navigate("manga_detail/${story.id}")
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 3. Xem Nhiều Nhất
            item {
                StorySection(
                    title = "Xem Nhiều Nhất",
                    stories = mockStories.subList(4, 8),
                    onStoryClick = { story ->
                        // SỬA: Điều hướng đến màn hình chi tiết khi nhấn Truyện
                        navController.navigate("manga_detail/${story.id}")
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 4. Truyện Hoàn Thành
            item {
                StorySection(
                    title = "Truyện Hoàn Thành",
                    stories = mockStories.subList(0, 4),
                    onStoryClick = { story ->
                        // SỬA: Điều hướng đến màn hình chi tiết khi nhấn Truyện
                        navController.navigate("manga_detail/${story.id}")
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// =========================================================================
// Các Composable Màn hình Home
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
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
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
            itemsIndexed(banners) { index: Int, item: BannerItem ->
                // THAY ĐỔI: Gọi onBannerClick với item
                BannerItemCard(item = item, onClick = { onBannerClick(item) })
            }
        }

        // Dots Indicator Placeholder
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
                        .clickable { pagerState.value = index } // Thêm tương tác cho dots
                )
                Spacer(modifier = Modifier.width(6.dp))
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
            // Placeholder cho ảnh banner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Banner Placeholder",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                )
            }

            // Tiêu đề/Text Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
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
        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Xem tất cả",
                color = PrimaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { /* Chuyển màn hình Xem tất cả */ }
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stories) { story: Story ->
                // THAY ĐỔI: Gọi onStoryClick với story
                StoryItem(story = story) {
                    onStoryClick(story)
                }
            }
        }
    }
}

// XÓA ĐỊNH NGHĨA TRÙNG LẶP StoryItem Ở ĐÂY!
/*
@Composable
fun StoryItem(story: Story, onClick: () -> Unit) {
    // ... nội dung đã bị xóa
}
*/

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(navController = rememberNavController())
}