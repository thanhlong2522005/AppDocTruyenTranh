package com.example.appdoctruyentranh

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.GenreDetailViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreDetailScreen(
    navController: NavHostController,
    genreId: Int,
    genreName: String,
    viewModel: GenreDetailViewModel = viewModel()
) {
    val stories by viewModel.stories
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    LaunchedEffect(genreId) {
        viewModel.loadStories(genreId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = genreName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }

                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Lỗi kết nối",
                                color = Color.Red,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = error!!,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }

                stories.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chưa có truyện nào trong thể loại này",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(stories, key = { it.id }) { story ->  // Thêm key để tối ưu
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("manga_detail/${story.id}")
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = story.imageUrl,
                                            placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                                        ),
                                        contentDescription = story.title,
                                        modifier = Modifier
                                            .size(width = 90.dp, height = 120.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = story.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = story.description.let {
                                                if (it.length > 100) it.take(100) + "..." else it
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.DarkGray,
                                            maxLines = 3,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(8.dp))

                                        // === GENRES - CHIP ===
                                       /* if (story.genres.isNotEmpty()) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                story.genres.forEach { genre ->
                                                    AssistChip(
                                                        onClick = { },
                                                        label = {
                                                            Text(
                                                                text = genre,
                                                                fontSize = 12.sp,
                                                                color = PrimaryColor // Giữ màu chữ là PrimaryColor
                                                            )
                                                        },
                                                        colors = AssistChipDefaults.assistChipColors(
                                                            // Đặt màu nền containerColor thành Color.Transparent để loại bỏ nền
                                                            containerColor = Color.Transparent,
                                                            labelColor = PrimaryColor
                                                        ),
                                                        // Bỏ thuộc tính 'border' để loại bỏ viền
                                                        // border = BorderStroke(1.dp, PrimaryColor) // Dòng này đã bị loại bỏ
                                                    )

                                                }
                                            }
                                            Spacer(Modifier.height(8.dp))

                                        }

                                        */

                                        // Số chapter
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Chap ${story.chapters?.size ?: 0}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}