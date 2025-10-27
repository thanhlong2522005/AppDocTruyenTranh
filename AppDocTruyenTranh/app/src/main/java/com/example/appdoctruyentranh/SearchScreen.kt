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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
// Các components và data classes dùng chung được truy cập tự động trong cùng package,
// nếu không, chúng sẽ được import từ AppComponents.kt:
// import com.example.appdoctruyentranh.AppHeader
// import com.example.appdoctruyentranh.AppBottomNavigationBar
// import com.example.appdoctruyentranh.PrimaryColor
// import com.example.appdoctruyentranh.Story // (Giả định nằm trong AppComponents.kt)
// import com.example.appdoctruyentranh.StoryItem // (Giả định nằm trong AppComponents.kt)


// --- Data Model và Mock Data ---
// KHÔNG CẦN định nghĩa lại data class Story ở đây (đã có trong AppComponents.kt)
// KHÔNG CẦN import TextOverflow vì nó chỉ được dùng trong StoryItem (đã chuyển)

val mockRecentSearches = listOf("Tiên hiệp", "Đô thị", "Quỷ Dị")
val mockSearchResults = listOf(
    Story(1, "Toàn Cầu Quỷ Dị Thời Đại"),
    Story(2, "Nguyên Lai Ta Là Tà Tu Tiên Đại Lão"),
    Story(3, "Đệ tử tu luyện"),
    Story(4, "Ngã Dục Phong Thiên"),
    Story(5, "Ta Làm Đạo Sĩ Những Năm Kia"),
    Story(6, "Người Khác Tu Tiên"),
)

// ======================================================================
// Màn hình Chính: SearchScreen
// ======================================================================

@Composable
fun SearchScreen(navController: NavHostController) {
    Scaffold(
        topBar = { AppHeader() },
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Thanh tìm kiếm
            SearchBarComponent(
                onSearch = { query ->
                    println("Searching for: $query")
                }
            )

            val showResults = false

            if (showResults) {
                SearchResultsGrid(
                    results = mockSearchResults,
                    onStoryClick = { story ->
                        println("Clicked on story: ${story.title}")
                    }
                )
            } else {
                RecentSearches(
                    recentQueries = mockRecentSearches,
                    onQueryClick = { query ->
                        println("Quick search: $query")
                    }
                )
            }
        }
    }
}

// ======================================================================
// Các Composable Thành phần
// ======================================================================

@Composable
fun SearchBarComponent(onSearch: (String) -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    // Sử dụng LocalSoftwareKeyboardController và LocalFocusManager
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = text,
        onValueChange = { newText -> text = newText },
        placeholder = { Text("Tìm kiếm tên truyện, tác giả...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Icon")
        },
        trailingIcon = {
            if (text.text.isNotEmpty()) {
                IconButton(onClick = { text = TextFieldValue("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(text.text)
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color.LightGray
        )
    )
}

@Composable
fun RecentSearches(recentQueries: List<String>, onQueryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Tìm kiếm gần đây",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Sử dụng FlowRow cho các chip
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            recentQueries.forEach { query ->
                AssistChip(
                    onClick = { onQueryClick(query) },
                    label = { Text(query) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFE0F7FA),
                        labelColor = PrimaryColor,
                        leadingIconContentColor = PrimaryColor
                    )
                )
            }
        }
    }
}

@Composable
fun SearchResultsGrid(results: List<Story>, onStoryClick: (Story) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Kết quả tìm kiếm (${results.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(results) { story ->
                // SỬ DỤNG StoryItem ĐÃ ĐƯỢC ĐỊNH NGHĨA TRONG AppComponents.kt
                StoryItem(story = story) {
                    onStoryClick(story)
                }
            }
        }
    }
}

// XÓA ĐỊNH NGHĨA TRÙNG LẶP STORYITEM Ở ĐÂY!
// -> Nó được sử dụng từ AppComponents.kt (file dùng chung)

@Preview(showBackground = true)
@Composable
fun PreviewSearchScreen() {
    SearchScreen(navController = rememberNavController())
}