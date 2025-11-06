package com.example.appdoctruyentranh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.model.Story
import androidx.compose.foundation.layout.PaddingValues

// === Mock data ===
val mockRecentSearches = listOf("Tiên hiệp", "Đô thị", "Quỷ Dị")
val mockSearchResults = listOf(
    Story(1, "Toàn Cầu Quỷ Dị Thời Đại", "https://example.com/cover1.jpg"),
    Story(2, "Nguyên Lai Ta Là Tà Tu Tiên Đại Lão", "https://example.com/cover2.jpg"),
    Story(3, "Đệ tử tu luyện", "https://example.com/cover3.jpg"),
    Story(4, "Ngã Dục Phong Thiên", "https://example.com/cover4.jpg"),
)

@Composable
fun SearchScreen(navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var recentSearches by remember { mutableStateOf(mockRecentSearches) }
    var searchResults by remember { mutableStateOf<List<Story>?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

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
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding: PaddingValues -> // <-- khai báo kiểu rõ ràng
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // dùng biến innerPadding kiểu PaddingValues
        ) {
            // Thanh tìm kiếm
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Tìm kiếm tên truyện, tác giả...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (query.isNotBlank()) {
                            // Thêm vào lịch sử (tránh trùng)
                            recentSearches = (listOf(query) + recentSearches.filter { it != query }).take(6)
                            // Giả lập tìm kiếm
                            searchResults = mockSearchResults
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // Hiển thị kết quả hoặc lịch sử
            if (searchResults != null) {
                SearchResultsGrid(
                    results = searchResults!!,
                    onStoryClick = { story ->
                        navController.navigate("manga_detail/${story.id}")
                    }
                )
            } else {
                RecentSearches(
                    recentQueries = recentSearches,
                    onQueryClick = { clickedQuery ->
                        query = clickedQuery
                        // Tự động tìm kiếm
                        searchResults = mockSearchResults
                    }
                )
            }
        }
    }
}

// (phần còn lại giữ nguyên như trước)
@Composable
fun RecentSearches(recentQueries: List<String>, onQueryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Tìm kiếm gần đây",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var remaining = recentQueries.size
        Column {
            while (remaining > 0) {
                val rowItems = recentQueries.drop(recentQueries.size - remaining).take(3)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { query ->
                        FilterChip(
                            onClick = { onQueryClick(query) },
                            label = { Text(query, fontSize = 13.sp) },
                            selected = false,
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFE0F7FA),
                                labelColor = PrimaryColor
                            )
                        )
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                remaining -= rowItems.size
            }
        }
    }
}

@Composable
fun SearchResultsGrid(results: List<Story>, onStoryClick: (Story) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Kết quả tìm kiếm (${results.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,

        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { story ->
                StoryItem(story = story) {
                    onStoryClick(story)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchScreen() {
    SearchScreen(navController = rememberNavController())
}
