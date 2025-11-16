package com.example.appdoctruyentranh

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.ManageStoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStoriesScreen(
    navController: NavHostController,
    viewModel: ManageStoriesViewModel = viewModel()
) {
    val stories by viewModel.stories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadStories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QUẢN LÝ TRUYỆN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("admin_upload")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add story",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {

            // ===== Ô TÌM KIẾM =====
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                placeholder = { Text("Tìm truyện theo tên") },
                singleLine = true
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        stories.filter { it.title.contains(search, ignoreCase = true) },
                        key = { it.id }
                    ) { story ->
                        StoryAdminItem(
                            story = story,
                            onEdit = {
                                navController.navigate("editStory/${story.id}")
                            },
                            onDelete = {
                                // Cần có xác nhận (AlertDialog) trước khi xoá.
                                println("Xoá truyện: ${story.title}")
                            },
                            onClick = {
                               navController.navigate("manga_detail/${story.id}")
                                println("Xem chi tiết truyện: ${story.title}")
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun StoryAdminItem(
    story: Story,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {

            Image(
                painter = rememberAsyncImagePainter(story.imageUrl),
                contentDescription = story.title,
                modifier = Modifier
                    .size(90.dp, 120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    story.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = story.description.take(80) + "...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Chapters: ${story.chapters.size}", fontSize = 13.sp)
                    Text("Views: ${story.views}", fontSize = 13.sp)
                    Text("Likes: ${story.likes}", fontSize = 13.sp)
                }

                Spacer(Modifier.height(10.dp))


                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onEdit) {
                        Text("Sửa")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Xoá")
                    }
                }
            }
        }
    }
}
