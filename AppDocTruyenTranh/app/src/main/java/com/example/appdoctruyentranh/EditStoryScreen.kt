package com.example.appdoctruyentranh

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.appdoctruyentranh.model.Genre
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.UploadViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStoryScreen(navController: NavHostController, mangaId: String) {
    val authViewModel: AuthViewModel = viewModel()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkAdminStatus()
    }

    if (!isAdmin) {
        Scaffold(
            topBar = {
                 TopAppBar(
                    title = { Text("Lỗi truy cập") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Bạn không có quyền truy cập vào chức năng này!", textAlign = TextAlign.Center, fontSize = 18.sp)
            }
        }
    } else {
        val viewModel: UploadViewModel = viewModel()
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        var story by remember { mutableStateOf<Story?>(null) }
        var allGenres by remember { mutableStateOf<List<Genre>>(emptyList()) }
        var selectedGenreIds by remember { mutableStateOf<List<String>>(emptyList()) }
        var selectedDisplayLists by remember { mutableStateOf<Set<String>>(emptySet()) }

        var isLoading by remember { mutableStateOf(true) }
        var isSaving by remember { mutableStateOf(false) }

        LaunchedEffect(mangaId) {
            viewModel.getStoryById(mangaId) { fetchedStory ->
                story = fetchedStory
            }
            viewModel.getGenres { genres ->
                allGenres = genres
            }
            val displayCollections = listOf(
                "banners", "new_updates", "most_viewed", "completed_stories",
                "favorites", "trending_list", "new_releases"
            )
            val currentLists = mutableSetOf<String>()
            displayCollections.forEach { collectionName ->
                viewModel.isInDisplayList(collectionName, mangaId) { isIn ->
                    if (isIn) currentLists.add(collectionName)
                }
            }
            selectedDisplayLists = currentLists

            isLoading = false
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (story == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy truyện", color = Color.Red, fontWeight = FontWeight.Medium)
            }
            return
        }

        val currentStory = story!!

        var title by remember { mutableStateOf(currentStory.title) }
        var author by remember { mutableStateOf(currentStory.author) }
        var description by remember { mutableStateOf(currentStory.description) }
        var coverImageUrl by remember { mutableStateOf(currentStory.imageUrl) }
        var status by remember { mutableStateOf(currentStory.status) }

        LaunchedEffect(allGenres, currentStory.genres) {
            if (allGenres.isNotEmpty()) {
                selectedGenreIds = allGenres
                    .filter { currentStory.genres.contains(it.name) }
                    .map { it.id.toString() }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chỉnh sửa truyện", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2))
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (coverImageUrl.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        AsyncImage(
                            model = coverImageUrl,
                            contentDescription = "Ảnh bìa",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tên truyện *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Tác giả *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = coverImageUrl, onValueChange = { coverImageUrl = it }, label = { Text("URL ảnh bìa *") }, placeholder = { Text("https://...") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Giới thiệu") }, minLines = 4, modifier = Modifier.fillMaxWidth())

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trạng thái") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Đang ra", "Hoàn thành", "Tạm drop").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text("Thể loại *", fontWeight = FontWeight.Medium)
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allGenres.forEach { genre ->
                        FilterChip(
                            selected = selectedGenreIds.contains(genre.id.toString()),
                            onClick = {
                                selectedGenreIds = if (selectedGenreIds.contains(genre.id.toString())) {
                                    selectedGenreIds - genre.id.toString()
                                } else {
                                    selectedGenreIds + genre.id.toString()
                                }
                            },
                            label = { Text(genre.name) },
                            leadingIcon = {
                                if (selectedGenreIds.contains(genre.id.toString())) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }

                Text("Hiển thị trên trang chủ", fontWeight = FontWeight.Medium)
                val displayOptions = listOf(
                    "banners" to "Banner chính",
                    "new_updates" to "Cập nhật mới",
                    "most_viewed" to "Xem nhiều nhất",
                    "completed_stories" to "Hoàn thành",
                    "favorites" to "Yêu thích",
                    "trending_list" to "Xu hướng",
                    "new_releases" to "Mới phát hành"
                )
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedDisplayLists.contains(key),
                            onClick = {
                                selectedDisplayLists = if (selectedDisplayLists.contains(key)) {
                                    selectedDisplayLists - key
                                } else {
                                    selectedDisplayLists + key
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (title.isBlank() || author.isBlank() || coverImageUrl.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Vui lòng nhập đầy đủ tên, tác giả và ảnh bìa") }
                            return@Button
                        }
                        if (selectedGenreIds.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Chọn ít nhất 1 thể loại") }
                            return@Button
                        }

                        isSaving = true
                        val selectedGenreNames = allGenres
                            .filter { selectedGenreIds.contains(it.id.toString()) }
                            .map { it.name }

                        val updatedStory = currentStory.copy(
                            title = title.trim(),
                            author = author.trim(),
                            description = description.trim(),
                            imageUrl = coverImageUrl.trim(),
                            status = status
                        )

                        viewModel.updateStory(
                            story = updatedStory,
                            imageUrl = coverImageUrl.trim(),
                            selectedGenreNames = selectedGenreNames,
                            displayLists = selectedDisplayLists,
                            onSuccess = {
                                isSaving = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Cập nhật thành công!")
                                    navController.popBackStack()
                                }
                            },
                            onError = { e ->
                                isSaving = false
                                scope.launch { snackbarHostState.showSnackbar("Lỗi: ${e.message}") }
                            }
                        )
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Lưu thay đổi", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
