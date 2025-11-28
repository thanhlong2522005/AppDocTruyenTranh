package com.example.appdoctruyentranh

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.model.Comment
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.MangaDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

fun formatRelativeTime(date: Date?): String {
    if (date == null) return "Không rõ"
    val now = Date().time
    val diff = now - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    return when {
        seconds < 60 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        hours < 24 -> "$hours giờ trước"
        days < 30 -> "$days ngày trước"
        months < 12 -> "$months tháng trước"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
}

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

    UserMangaDetailScreen(navController, mangaDetail, isLoading, error, viewModel, isAdmin)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val tabs = listOf("Thông tin", "Chương", "Bình luận")
    val colorScheme = MaterialTheme.colorScheme

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
            error != null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = error, color = colorScheme.error) }
            mangaDetail != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).background(colorScheme.surface)
                ) {
                    item {
                        DetailSummarySection(detail = mangaDetail, viewModel = viewModel, isAdmin = isAdmin, navController = navController)
                    }
                    stickyHeader {
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = colorScheme.surface,
                            contentColor = PrimaryColor
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, fontWeight = FontWeight.Bold) })
                            }
                        }
                    }
                    item {
                        when (selectedTabIndex) {
                            0 -> InfoTabContent(detail = mangaDetail)
                            1 -> ChapterTabContent(mangaId = mangaDetail.id, chapters = mangaDetail.chapters, navController = navController)
                            2 -> CommentTabContent(mangaId = mangaDetail.id, viewModel = viewModel, navController = navController, isAdmin = isAdmin)                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailSummarySection(
    detail: Story,
    viewModel: MangaDetailViewModel,
    isAdmin: Boolean,
    navController: NavHostController
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth().background(colorScheme.surface).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Card(modifier = Modifier.size(110.dp, 160.dp), shape = RoundedCornerShape(8.dp)) {
                AsyncImage(
                    model = detail.imageUrl,
                    contentDescription = detail.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                    error = painterResource(android.R.drawable.stat_notify_error)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(detail.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = colorScheme.onSurface)
                Text(detail.author, fontSize = 16.sp, color = colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoStat(icon = if (detail.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, text = detail.likes.toString(), tint = if (detail.isLiked) Color.Red else colorScheme.onSurfaceVariant, onClick = { viewModel.toggleLike() })
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoStat(Icons.Default.Visibility, detail.views.toString())
                    Spacer(modifier = Modifier.width(16.dp))
                    InfoStat(Icons.Default.List, detail.totalChapters.toString())
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(
                        rating = detail.rating,
                        onRatingChanged = { newRating ->
                            viewModel.updateRating(newRating)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${String.format("%.1f", detail.rating)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurface
                    )

                    if (detail.ratingCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${detail.ratingCount} lượt)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
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
                AssistChip(onClick = { /* Navigate to genre */ }, label = { Text(genre, fontSize = 14.sp) })
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
fun InfoStat(icon: ImageVector, text: String, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant, onClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 14.sp, color = tint)
    }
}

@Composable
fun RatingBar(rating: Float, onRatingChanged: (Float) -> Unit) {
    var currentRating by remember { mutableFloatStateOf(rating) }
    Row {
        for (i in 1..5) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = if (i <= currentRating) Color(0xFFFFC107) else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(20.dp).clickable { currentRating = i.toFloat(); onRatingChanged(i.toFloat()) })
        }
    }
}

@Composable
fun InfoTabContent(detail: Story) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Trạng thái", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        Text(detail.status, fontSize = 14.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
        Text("Giới thiệu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = detail.description, fontSize = 14.sp, color = colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 20.sp)
    }
}

@Composable
fun ChapterTabContent(mangaId: String, chapters: List<Chapter>, navController: NavHostController) {
    LazyColumn(modifier = Modifier.height(500.dp)) {
        items(chapters) { chapter ->
            ChapterItem(chapter = chapter) { navController.navigate("read/$mangaId/${chapter.id}") }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
        }
    }
}

@Composable
fun ChapterItem(chapter: Chapter, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "Chương ${chapter.number}: ${chapter.title}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = colorScheme.onSurface)
        Text(text = chapter.uploadDate, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CommentTabContent(
    mangaId: String,
    viewModel: MangaDetailViewModel,
    navController: NavHostController,
    isAdmin: Boolean
) {
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isCommentLoading.collectAsState()
    val commentError by viewModel.commentError.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var newCommentContent by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(mangaId) {
        if (comments.isEmpty() && commentError == null) {
            viewModel.loadComments(mangaId)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (currentUser != null) {
            Row(verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = newCommentContent,
                    onValueChange = { newCommentContent = it },
                    label = { Text("Viết bình luận của bạn...") },
                    modifier = Modifier.weight(1f),
                    minLines = 2,
                    maxLines = 4
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newCommentContent.isNotBlank()) {
                            viewModel.postComment(mangaId, newCommentContent, currentUser)
                            newCommentContent = "" // Xóa nội dung input
                        }
                    },
                    enabled = newCommentContent.isNotBlank() && !isLoading,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gửi", tint = PrimaryColor)
                }
            }
            if (commentError != null) {
                Text(commentError!!, color = colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(16.dp))
        } else {
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = colorScheme.onSurfaceVariant)) {
                    append("Vui lòng ")
                }

                pushStringAnnotation(tag = "LOGIN", annotation = "login")
                withStyle(style = SpanStyle(color = PrimaryColor, fontWeight = FontWeight.Bold)) {
                    append("đăng nhập")
                }
                pop()

                withStyle(style = SpanStyle(color = colorScheme.onSurfaceVariant)) {
                    append(" để bình luận")
                }
            }

            ClickableText(
                text = annotatedString,
                onClick = {
                    annotatedString.getStringAnnotations(tag = "LOGIN", start = it, end = it)
                        .firstOrNull()?.let {
                            navController.navigate("login")
                        }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Text("Bình luận (${comments.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        Divider(color = colorScheme.outline)

        when {
            isLoading -> Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) { CircularProgressIndicator(color = PrimaryColor) }
            comments.isEmpty() && commentError == null -> Text("Chưa có bình luận nào.", color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            else -> LazyColumn(modifier = Modifier.heightIn(max = 600.dp)) { // Giới hạn chiều cao cho LazyColumn lồng nhau
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        isAdmin = isAdmin,
                        onDelete = {
                            viewModel.deleteComment(mangaId, comment.id)
                        }
                    )
                    Divider(color = colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    val timeAgo = formatRelativeTime(comment.timestamp)
    val colorScheme = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa bình luận này không?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
        val avatarUrl = comment.userInfo?.avatarUrl ?: ""
        val userName = comment.userInfo?.name ?: "Ẩn danh"

        Box(modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(PrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(userName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colorScheme.onSurface)
                Text(text = timeAgo, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(comment.content, fontSize = 15.sp, color = colorScheme.onSurface.copy(alpha = 0.9f), lineHeight = 20.sp)
        }
        if (isAdmin) {
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Xóa bình luận",
                    tint = colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMangaDetailScreen() {
    MangaDetailScreen(navController = rememberNavController(), mangaId = "preview_123")
}
