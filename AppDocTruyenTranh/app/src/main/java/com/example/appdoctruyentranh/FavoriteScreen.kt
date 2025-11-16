package com.example.appdoctruyentranh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.FavoriteViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FavoriteScreen(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val authViewModel: AuthViewModel = viewModel()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkAdminStatus()
    }

    if (currentUser == null) {
        PleaseLoginScreen(navController = navController, title = "Truyện yêu thích")
    } else {
        val viewModel: FavoriteViewModel = viewModel()
        val stories by viewModel.favoriteStories.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        var showDeleteDialog by remember { mutableStateOf<Story?>(null) }
        LaunchedEffect(true) {
            viewModel.refreshFavorites()
        }
        Scaffold(
            topBar = {
                AppHeader(
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier
                                .size(56.dp)
                                .padding(16.dp)
                                .clickable { navController.popBackStack() }
                        )
                    }
                )
            },
            bottomBar = { AppBottomNavigationBar(navController = navController, isAdmin = isAdmin) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Truyện Yêu Thích",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(16.dp)
                )
                Divider(color = Color.LightGray, thickness = 1.dp)

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryColor)
                        }
                    }
                    stories.isEmpty() -> {
                        EmptyListMessage(message = "Bạn chưa thêm truyện nào vào danh sách yêu thích.")
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(stories) { story ->
                                FavoriteStoryItem(
                                    story = story,
                                    onStoryClick = {
                                        navController.navigate("manga_detail/${story.id}")
                                    },
                                    onRemoveClick = {
                                        showDeleteDialog = story
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        showDeleteDialog?.let { story ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Xóa khỏi yêu thích?") },
                text = { Text("Bạn có chắc muốn xóa \"${story.title}\" khỏi danh sách yêu thích?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeFromFavorites(story.id) {
                            showDeleteDialog = null
                        }
                    }) {
                        Text("Xóa", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
fun FavoriteStoryItem(
    story: Story,
    onStoryClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable(onClick = onStoryClick)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(modifier = Modifier.size(70.dp), shape = RoundedCornerShape(4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (story.imageUrl.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(story.imageUrl),
                        contentDescription = "Bìa truyện",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = PrimaryColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = story.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tác giả: ${story.author}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Xóa",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun EmptyListMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}
