@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
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
import com.example.appdoctruyentranh.model.Story
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.viewmodel.MangaDetailViewModel

val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)

// ===============================================================
// MÀN HÌNH CHÍNH: MangaDetailScreen
// ===============================================================

@Composable
fun MangaDetailScreen(navController: NavHostController, mangaId: String) {
    val viewModel: MangaDetailViewModel = viewModel(
        key = "manga_detail_$mangaId"
    )
    val mangaDetail by viewModel.mangaDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Thông tin", "Chương")

    // 🔹 Gọi load dữ liệu khi vào màn hình
    LaunchedEffect(mangaId) {
        viewModel.loadMangaDetail(mangaId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mangaDetail?.title ?: "Chi tiết truyện",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        },
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { paddingValues ->

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Đã xảy ra lỗi", color = Color.Red)
                }
            }

            mangaDetail != null -> {
                val detail = mangaDetail!!
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
                                mangaId = detail.id,
                                chapters = detail.chapters,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

// ===============================================================
// CÁC THÀNH PHẦN PHỤ
// ===============================================================

@Composable
fun DetailSummarySection(detail: Story) {
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
                Text(detail.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
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
                    onClick = { /* Navigate to genre */ },
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

@Composable
fun InfoTabContent(detail: Story) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Trạng thái", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(detail.status, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 16.dp))

        Text("Giới thiệu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail.description,
            fontSize = 14.sp,
            color = TextPrimary.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ChapterTabContent(mangaId: String, chapters: List<Chapter>, navController: NavHostController) {
    LazyColumn(
        modifier = Modifier.height(500.dp)
    ) {
        items(chapters) { chapter ->
            ChapterItem(chapter = chapter) {
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
        Text(
            text = "Chương ${chapter.number}: ${chapter.title}",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
    MangaDetailScreen(
        navController = rememberNavController(),
        mangaId = "preview_123" // ← String
    )
}