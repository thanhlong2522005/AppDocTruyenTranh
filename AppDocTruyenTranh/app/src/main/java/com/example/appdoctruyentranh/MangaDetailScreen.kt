package com.example.appdoctruyentranh

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.MangaDetailViewModel

val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailScreen(navController: NavHostController, mangaId: String) {
    val authViewModel: AuthViewModel = viewModel()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    val viewModel: MangaDetailViewModel = viewModel(key = "manga_detail_$mangaId")
    val mangaDetail by viewModel.mangaDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(mangaId) { 
        viewModel.loadMangaDetail(mangaId)
        authViewModel.checkAdminStatus()
    }

    if (isAdmin) {
        AdminMangaDetailScreen(navController, mangaDetail, isLoading, error)
    } else {
        UserMangaDetailScreen(navController, mangaDetail, isLoading, error, viewModel, isAdmin)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMangaDetailScreen(navController: NavHostController, mangaDetail: Story?, isLoading: Boolean, error: String?) {
     Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mangaDetail?.title ?: "Quản lý truyện", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        }
    ) { paddingValues ->
         when {
            isLoading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(error) }
            mangaDetail != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("Bạn đang ở chế độ Quản trị viên cho truyện này.", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                    
                    // Admin Actions
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { navController.navigate("edit_story/${mangaDetail.id}") }, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sửa thông tin")
                        }
                        Button(onClick = { navController.navigate("admin_upload?mangaId=${mangaDetail.id}") }, modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Thêm chương")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Chapter List for Admin
                    Text("Danh sách chương (${mangaDetail.chapters.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (mangaDetail.chapters.isEmpty()) {
                        Text("Chưa có chương nào.", color = TextSecondary, modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        Column {
                             mangaDetail.chapters.forEach { chapter ->
                                ChapterItem(chapter = chapter) {
                                    navController.navigate("read/${mangaDetail.id}/${chapter.id}")
                                }
                                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMangaDetailScreen(
    navController: NavHostController, 
    mangaDetail: Story?, 
    isLoading: Boolean, 
    error: String?, 
    viewModel: MangaDetailViewModel,
    isAdmin: Boolean
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Thông tin", "Chương")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mangaDetail?.title ?: "Chi tiết truyện", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        bottomBar = { AppBottomNavigationBar(navController = navController, isAdmin = isAdmin) }
    ) { paddingValues ->
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryColor) }
            error != null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = error, color = Color.Red) }
            mangaDetail != null -> {
                val detail = mangaDetail
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    item {
                        DetailSummarySection(detail, viewModel, isAdmin, navController)
                    }
                    stickyHeader {
                        TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White, contentColor = PrimaryColor) {
                            tabs.forEachIndexed { index, title ->
                                Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, fontWeight = FontWeight.Bold) })
                            }
                        }
                    }
                    item {
                        when (selectedTabIndex) {
                            0 -> InfoTabContent(detail = detail)
                            1 -> ChapterTabContent(mangaId = detail.id, chapters = detail.chapters, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSummarySection(
    detail: Story,
    viewModel: MangaDetailViewModel,
    isAdmin: Boolean,
    navController: NavHostController
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Card(modifier = Modifier.size(110.dp, 160.dp), shape = RoundedCornerShape(8.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Bookmark, contentDescription = "Cover", tint = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(detail.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(detail.author, fontSize = 16.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoStat(icon = if (detail.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, text = detail.likes.toString(), tint = if (detail.isLiked) Color.Red else TextSecondary, onClick = { viewModel.toggleLike() })
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoStat(Icons.Default.Visibility, detail.views.toString())
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoStat(Icons.Default.List, detail.totalChapters.toString())
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = detail.rating, onRatingChanged = { rating -> viewModel.updateRating(rating) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(detail.rating.toString(), color = TextSecondary)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val firstChapter = detail.chapters.firstOrNull()
                if (firstChapter != null) {
                    viewModel.incrementViewCount()
                    val chapterIdString = firstChapter.id.toString()
                    viewModel.saveReadHistory(storyId = detail.id, chapterId = chapterIdString)
                    navController.navigate("read/${detail.id}/${chapterIdString}")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đọc ngay", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            detail.genres.forEach { genre ->
                AssistChip(onClick = { /* Navigate to genre */ }, label = { Text(genre, fontSize = 14.sp) }, colors = AssistChipDefaults.assistChipColors(containerColor = Color.White, labelColor = TextPrimary), border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f)))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isAdmin) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { navController.navigate("edit_story/${detail.id}") }, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sửa truyện")
                }
                OutlinedButton(onClick = { navController.navigate("admin_upload?mangaId=${detail.id}") }, modifier = Modifier.weight(1f).padding(start = 8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE91E63))) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Thêm chương")
                }
            }
        }
    }
}

@Composable
fun InfoStat(icon: ImageVector, text: String, tint: Color = TextSecondary, onClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, color = TextSecondary)
    }
}

@Composable
fun RatingBar(rating: Float, onRatingChanged: (Float) -> Unit) {
    var currentRating by remember { mutableFloatStateOf(rating) }
    Row {
        for (i in 1..5) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = if (i <= currentRating) Color.Yellow else Color.LightGray, modifier = Modifier.size(20.dp).clickable { currentRating = i.toFloat(); onRatingChanged(i.toFloat()) })
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
        Text(text = detail.description, fontSize = 14.sp, color = TextPrimary.copy(alpha = 0.8f), lineHeight = 20.sp)
    }
}

@Composable
fun ChapterTabContent(mangaId: String, chapters: List<Chapter>, navController: NavHostController) {
    LazyColumn(modifier = Modifier.height(500.dp)) {
        items(chapters) { chapter ->
            ChapterItem(chapter = chapter) { navController.navigate("read/$mangaId/${chapter.id}") }
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
        }
    }
}

@Composable
fun ChapterItem(chapter: Chapter, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "Chương ${chapter.number}: ${chapter.title}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = chapter.uploadDate, fontSize = 12.sp, color = TextSecondary)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMangaDetailScreen() {
    MangaDetailScreen(navController = rememberNavController(), mangaId = "preview_123")
}
