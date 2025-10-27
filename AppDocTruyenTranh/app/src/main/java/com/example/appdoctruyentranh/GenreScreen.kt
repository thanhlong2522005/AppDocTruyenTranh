package com.example.appdoctruyentranh

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.AppHeader
import com.example.appdoctruyentranh.AppBottomNavigationBar
import com.example.appdoctruyentranh.PrimaryColor

// --- Data Model và Mock Data Riêng ---
data class Genre(val id: Int, val name: String, val iconResId: Int)

val mockGenres = listOf(
    // Sử dụng 0 làm placeholder ID cho icon
    Genre(1, "Hành Động", 0),
    Genre(2, "Huyền Huyễn", 0),
    Genre(3, "Xuyên Không", 0),
    Genre(4, "Tình Cảm", 0),
    Genre(5, "Học Đường", 0),
    Genre(6, "Phiêu Lưu", 0),
    Genre(7, "Hài Hước", 0),
    Genre(8, "Kinh Dị", 0),
    Genre(9, "Cổ Trang", 0),
    Genre(10, "Giả Tưởng", 0),
    Genre(11, "Võ Thuật", 0),
    Genre(12, "Siêu Năng", 0),
)

// =========================================================================
// Màn hình Chính: GenreScreen
// =========================================================================

@Composable
fun GenreScreen(navController: NavHostController) {
    Scaffold(
        // TRUYỀN COMPOSABLE CHO NÚT TRỞ VỀ
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tiêu đề lớn "Danh mục"
            Text(
                text = "Danh mục",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // LazyVerticalGrid để hiển thị các mục theo dạng lưới
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mockGenres) { genre ->
                    GenreItem(genre = genre) {
                        // Xử lý khi click: Chuyển đến màn hình danh sách truyện theo thể loại
                    }
                }
            }
        }
    }
}


// =========================================================================
// Các Composable Màn hình Genre
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreItem(genre: Genre, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F0F0) // Nền màu xám nhạt
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Thể loại (Placeholder)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu, // Icon placeholder
                    contentDescription = genre.name,
                    tint = PrimaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tên Thể loại
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


@Preview(showBackground = true)
@Composable
fun PreviewGenreScreen() {
    GenreScreen(navController = rememberNavController())
}