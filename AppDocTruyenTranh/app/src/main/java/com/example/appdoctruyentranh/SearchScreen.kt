// File: SearchScreen.kt
package com.example.appdoctruyentranh

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.model.StoryItem
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.SearchViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@Composable
fun SearchScreen(navController: NavHostController) {
    val viewModel: SearchViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val query by viewModel.searchQuery.collectAsState()
    val genreId by viewModel.selectedGenreId.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Thanh tìm kiếm
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                placeholder = { Text("Tìm kiếm tên truyện, tác giả...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearResults() }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
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

            // Bộ lọc thể loại
            if (genres.isNotEmpty()) {
                GenreFilter(
                    genres = genres,
                    selectedGenreId = genreId,
                    onGenreSelected = { viewModel.selectGenre(it) }
                )
            }

            // Nội dung
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                }
                results.isNotEmpty() -> {
                    SearchResultsGrid(
                        results = results,
                        onStoryClick = { story ->
                            if (story.id.isNotBlank()) {
                                navController.navigate("manga_detail/${story.id}")
                            }
                        }
                    )
                }
                query.isNotBlank() || genreId != null -> {
                    EmptyState(message = "Không tìm thấy kết quả")
                }
                recentSearches.isNotEmpty() -> {
                    RecentSearches(
                        recentQueries = recentSearches,
                        onQueryClick = { viewModel.updateQuery(it) },
                        onClearAll = { viewModel.clearRecentSearches() }
                    )
                }
                else -> {
                    EmptyState(message = "Nhập từ khóa để tìm kiếm")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreFilter(
    genres: List<com.example.appdoctruyentranh.model.Genre>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = genres.find { it.id == selectedGenreId }?.name ?: "Tất cả thể loại",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Tất cả thể loại") },
                onClick = {
                    onGenreSelected(null)
                    expanded = false
                }
            )
            genres.forEach { genre ->
                DropdownMenuItem(
                    text = { Text(genre.name) },
                    onClick = {
                        onGenreSelected(genre.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun RecentSearches(
    recentQueries: List<String>,
    onQueryClick: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tìm kiếm gần đây", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onClearAll) {
                Text("Xóa tất cả", color = PrimaryColor)
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recentQueries.forEach { query ->
                FilterChip(
                    onClick = { onQueryClick(query) },
                    label = { Text(query, fontSize = 13.sp) },
                    selected = false
                )
            }
        }
    }
}

@Composable
fun SearchResultsGrid(results: List<Story>, onStoryClick: (Story) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Kết quả (${results.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { story ->
                StoryItem(story = story, onClick = { onStoryClick(story) })
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(message, color = Color.Gray, fontSize = 16.sp)
    }
}