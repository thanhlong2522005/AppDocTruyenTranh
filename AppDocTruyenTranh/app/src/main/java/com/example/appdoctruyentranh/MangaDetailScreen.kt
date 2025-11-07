@file:OptIn(
    ExperimentalMaterial3Api::class, // Cho TopAppBar, TabRow, AssistChip,...
    ExperimentalLayoutApi::class      // Cho FlowRow
)

package com.example.appdoctruyentranh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.AppBottomNavigationBar
// Import cần thiết
// import com.example.appdoctruyentranh.PrimaryColor

// ------------------------------------------------------------------------
// DỮ LIỆU MẪU (Sử dụng lại Data Class Story đã cập nhật từ CommonComposables)
// ------------------------------------------------------------------------

val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)
data class MangaDetail(
    val id: Int,
    val title: String,
    val author: String,
    val status: String,
    val rating: Float,
    val likes: String,
    val views: String,
    val totalChapters: Int,
    val genres: List<String>,
    val description: String,
    val chapters: List<Chapter>
)

data class Chapter(val id: Int, val number: Int, val title: String, val uploadDate: String)

val mockChapters = (1..1021).map { Chapter(it, it, "Chương $it", "2 ngày trước") }

val mockMangaDetail = MangaDetail(
    id = 1,
    title = "Chân Tiên",
    author = "EK",
    status = "Full",
    rating = 0.0f,
    likes = "45",
    views = "101.2K",
    totalChapters = 1021,
    genres = listOf("Tiên Hiệp", "Kiếm Hiệp"),
    description = "Ở kiếp này, hắn mang trong người pháp quyết vô thượng, trong nội tâm còn có rất nhiều nhiều công pháp tăng tiên tu vi từng cảnh giới khác nhau, cộng thêm thân phận tông sư đan dược, hắn nhất định sẽ đại phóng quang mang trong tu chân giới, đủ loại tiếc nuối kiếp trước, kiếp này sẽ không để nó phát sinh.",
    chapters = mockChapters
)

// ------------------------------------------------------------------------
// MÀN HÌNH CHÍNH: MangaDetailScreen
// ------------------------------------------------------------------------

@Composable
fun MangaDetailScreen(navController: NavHostController, mangaId: Int = 1) {
    val tabs = listOf("Thông tin", "Chương")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    // Lấy chi tiết truyện (tạm thời dùng mock)
    val detail = mockMangaDetail.copy(id = mangaId)

    Scaffold(
        topBar = {
            AppHeader(
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Icon quay lại
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(16.dp)
                            .clickable {
                                navController.popBackStack() // Hành động quay lại
                            }
                    )
                }
            )
        },
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { DetailSummarySection(detail = detail) }

            stickyHeader {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = PrimaryColor
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            item {
                when (selectedTabIndex) {
                    0 -> InfoTabContent(detail = detail)
                    1 -> ChapterTabContent(
                        mangaId = detail.id, // Truyền ID truyện vào
                        chapters = detail.chapters,
                        navController = navController
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// HEADER & TÓM TẮT
// ------------------------------------------------------------------------

@Composable
fun MangaDetailHeader(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryColor
        )
    )
}

@Composable
fun DetailSummarySection(detail: MangaDetail) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Card(
                modifier = Modifier.size(110.dp, 160.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = "Cover", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(detail.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(detail.author, fontSize = 16.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoStat(Icons.Default.Favorite, detail.likes)
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoStat(Icons.Default.Visibility, detail.views)
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoStat(Icons.Default.List, detail.totalChapters.toString())
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = detail.rating)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(detail.rating.toString(), color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Xử lý đánh giá */ },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("0 đánh giá", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            detail.genres.forEach { genre ->
                AssistChip(
                    onClick = { /* Navigate to genre list */ },
                    label = { Text(genre, fontSize = 14.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White,
                        labelColor = TextPrimary
                    ),
                    border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
fun InfoStat(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, color = TextSecondary)
    }
}

@Composable
fun RatingBar(rating: Float) {
    Row {
        (1..5).forEach { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index <= rating.toInt()) Color.Yellow else Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ------------------------------------------------------------------------
// TAB NỘI DUNG
// ------------------------------------------------------------------------

@Composable
fun InfoTabContent(detail: MangaDetail) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Trạng Thái", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(detail.status, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 16.dp))

        Text("Miêu tả", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Giới Thiệu Truyện", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))

        Text(
            text = detail.description,
            fontSize = 14.sp,
            color = TextPrimary.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ChapterTabContent(mangaId: Int, chapters: List<Chapter>, navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.height(500.dp)
    ) {
        items(chapters) { chapter ->
            ChapterItem(chapter = chapter) {
                // SỬA: Điều hướng đến màn hình đọc truyện
                navController.navigate("read/$mangaId/${chapter.id}")
            }
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
        }
    }
}

@Composable
fun ChapterItem(chapter: Chapter, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chương ${chapter.number}: ${chapter.title}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = chapter.uploadDate,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMangaDetailScreen() {
    MangaDetailScreen(navController = rememberNavController())
}