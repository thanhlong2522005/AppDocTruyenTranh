package com.example.appdoctruyentranh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.appdoctruyentranh.model.Genre
import java.net.URLEncoder
import com.example.appdoctruyentranh.model.UiState
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.GenreViewModel

// ========================================================================
// Item thể loại riêng (TÁCH RA NGOÀI ĐỂ DÙNG TRƯỚC)
// ========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreItem(genre: Genre, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Icon hoặc hình ảnh ---
            if (genre.icon.isNotEmpty()) {
                AsyncImage(
                    model = genre.icon,
                    contentDescription = genre.name,
                    modifier = Modifier
                        .size(50.dp)
                        .background(PrimaryColor.copy(alpha = 0.1f), CircleShape)
                        .padding(10.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = genre.name,
                    tint = PrimaryColor,
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryColor.copy(alpha = 0.1f), CircleShape)
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Tên thể loại ---
            Text(
                text = genre.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}

// ========================================================================
// Màn hình chính hiển thị danh sách thể loại
// ========================================================================

@Composable
fun GenreScreen(
    navController: NavHostController,
    viewModel: GenreViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkAdminStatus()
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
            // --- Tiêu đề ---
            Text(
                text = "Danh mục truyện",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // --- Trạng thái dữ liệu ---
            when (uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }

                is UiState.Success -> {
                    val genres = (uiState as UiState.Success<List<Genre>>).data
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(genres) { genre ->
                            GenreItem(genre = genre) {
                                navController.navigate("genre_detail/${genre.id}/${genre.name}")                            }
                        }
                    }
                }

                UiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có thể loại nào", fontSize = 16.sp, color = Color.Gray)
                    }
                }

                is UiState.Error -> {
                    val errorMessage = (uiState as UiState.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lỗi tải dữ liệu", color = Color.Red, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage, color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.retry() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Text("Thử lại", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========================================================================
// Preview
// ========================================================================

@Preview(showBackground = true)
@Composable
fun PreviewGenreScreen() {
    GenreScreen(navController = rememberNavController())
}
