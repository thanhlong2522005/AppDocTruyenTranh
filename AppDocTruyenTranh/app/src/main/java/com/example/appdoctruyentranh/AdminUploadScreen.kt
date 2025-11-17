package com.example.appdoctruyentranh

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.appdoctruyentranh.model.Chapter
import com.example.appdoctruyentranh.model.Genre
import com.example.appdoctruyentranh.model.Story
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.UploadViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUploadScreen(navController: NavHostController, preselectedMangaId: String? = null) {
    val authViewModel: AuthViewModel = viewModel()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkAdminStatus()
    }

    if (!isAdmin) {
        // Giao diện khi không có quyền truy cập
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
        // ========================================================
        // BẮT ĐẦU GIAO DIỆN ADMIN BÌNH THƯỜNG (MÃ NGUỒN CŨ CỦA BẠN)
        // ========================================================
        val viewModel: UploadViewModel = viewModel()
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        var selectedMode by remember { mutableStateOf(if (preselectedMangaId != null) 1 else 0) }
        val tabs = listOf("Thêm truyện mới", "Thêm chương")

        // === Form: Thêm/Sửa truyện ===
        var title by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var coverImageUrl by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("Đang ra") }

        val allGenres = remember { mutableStateListOf<Genre>() }
        val selectedGenreIds = remember { mutableStateListOf<String>() }

        val selectedDisplayLists = remember { mutableStateOf(setOf<String>()) }

        // === Form: Thêm chương ===
        var selectedStoryId by remember { mutableStateOf<String?>(preselectedMangaId) }
        var selectedStoryTitle by remember { mutableStateOf("Chọn truyện") }
        var chapterNumber by remember { mutableStateOf("") }
        var chapterTitle by remember { mutableStateOf("") }
        var pageUrls by remember { mutableStateOf("") }
        var showStoryDropdown by remember { mutableStateOf(false) }

        val allStories = remember { mutableStateListOf<Story>() }
        var isLoading by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.getAllStories { stories ->
                allStories.clear()
                allStories.addAll(stories)
                preselectedMangaId?.let { id ->
                    stories.find { it.id == id }?.let { story ->
                        selectedStoryTitle = story.title
                    }
                }
            }
            viewModel.getGenres { genres ->
                allGenres.clear()
                allGenres.addAll(genres)
            }
        }
        // Thêm state cho số chương tiếp theo
        var nextChapterNumber by remember { mutableStateOf(1) }

        // Khi chọn truyện → lấy lastChapterNumber + 1
        LaunchedEffect(selectedStoryId) {
            selectedStoryId?.let { id ->
                viewModel.getLastChapterNumber(id) { last ->
                    nextChapterNumber = (last + 1).toInt()                    // Tự động điền vào input nếu trống
                    if (chapterNumber.isBlank()) {
                        chapterNumber = nextChapterNumber.toString()
                    }
                }
            } ?: run {
                nextChapterNumber = 1
            }
        }

        LaunchedEffect(preselectedMangaId) {
            if (preselectedMangaId != null && selectedMode == 0) {
                viewModel.getStoryById(preselectedMangaId) { story ->
                    story?.let {
                        title = it.title
                        author = it.author
                        description = it.description
                        coverImageUrl = it.imageUrl
                        status = it.status
                        selectedGenreIds.clear()
                        selectedGenreIds.addAll(allGenres.filter { g -> it.genres.contains(g.name) }.map { g -> g.id.toString() })
                    }
                }

                val displayCollections = listOf(
                    "banners", "new_updates", "most_viewed", "completed_stories",
                    "favorites", "trending_list", "new_releases"
                )
                val currentLists = mutableSetOf<String>()
                displayCollections.forEach { collectionName ->
                    viewModel.isInDisplayList(collectionName, preselectedMangaId) { isIn ->
                        if (isIn) currentLists.add(collectionName)
                    }
                }
                selectedDisplayLists.value = currentLists
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1557682250-33bd709cbe85?w=800&q=80",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Quản trị - Upload", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xCC1976D2))
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color.Transparent
            ) { padding ->

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    item {
                        TabRow(selectedTabIndex = selectedMode, containerColor = Color(0x221976D2)) {
                            tabs.forEachIndexed { index, tabTitle ->
                                Tab(
                                    selected = selectedMode == index,
                                    onClick = {
                                        selectedMode = index
                                        title = ""; author = ""; description = ""; coverImageUrl = ""
                                        chapterNumber = ""; chapterTitle = ""; pageUrls = ""
                                        selectedGenreIds.clear()
                                        selectedDisplayLists.value = emptySet()
                                    },
                                    text = { Text(tabTitle, color = if (selectedMode == index) MaterialTheme.colorScheme.primary else Color.Gray) }
                                )
                            }
                        }
                    }

                    when (selectedMode) {
                        0 -> item {
                            UploadStoryForm(
                                title = title, onTitleChange = { title = it },
                                author = author, onAuthorChange = { author = it },
                                description = description, onDescriptionChange = { description = it },
                                coverImageUrl = coverImageUrl, onCoverImageChange = { coverImageUrl = it },
                                status = status, onStatusChange = { status = it },
                                allGenres = allGenres,
                                selectedGenreIds = selectedGenreIds,
                                onGenreToggle = { genreId ->
                                    if (selectedGenreIds.contains(genreId)) {
                                        selectedGenreIds.remove(genreId)
                                    } else {
                                        selectedGenreIds.add(genreId)
                                    }
                                },
                                selectedDisplayLists = selectedDisplayLists.value,
                                onDisplayListToggle = { listName ->
                                    selectedDisplayLists.value = if (selectedDisplayLists.value.contains(listName)) {
                                        selectedDisplayLists.value - listName
                                    } else {
                                        selectedDisplayLists.value + listName
                                    }
                                },
                                isLoading = isLoading,
                                isEditMode = preselectedMangaId != null,
                                onSubmit = {
                                    if (title.isBlank() || author.isBlank() || coverImageUrl.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("Vui lòng nhập đầy đủ tên, tác giả và ảnh bìa") }
                                        return@UploadStoryForm
                                    }
                                    if (selectedGenreIds.isEmpty()) {
                                        scope.launch { snackbarHostState.showSnackbar("Chọn ít nhất 1 thể loại") }
                                        return@UploadStoryForm
                                    }

                                    isLoading = true
                                    val selectedGenreNames = allGenres
                                        .filter { selectedGenreIds.contains(it.id.toString()) }
                                        .map { it.name }

                                    val newStory = Story(
                                        id = preselectedMangaId ?: "",
                                        title = title.trim(),
                                        author = author.trim(),
                                        description = description.trim(),
                                        status = status
                                    )

                                    if (preselectedMangaId != null) {
                                        viewModel.updateStory(
                                            story = newStory,
                                            imageUrl = coverImageUrl,
                                            selectedGenreNames = selectedGenreNames,
                                            displayLists = selectedDisplayLists.value,
                                            onSuccess = {
                                                isLoading = false
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Cập nhật truyện thành công!")
                                                    navController.popBackStack()
                                                }
                                            },
                                            onError = {
                                                isLoading = false
                                                scope.launch { snackbarHostState.showSnackbar("Lỗi: ${it.message}") }
                                            }
                                        )
                                    } else {
                                        viewModel.createStory(
                                            story = newStory,
                                            imageUrl = coverImageUrl,
                                            selectedGenreNames = selectedGenreNames,
                                            displayLists = selectedDisplayLists.value,
                                            onSuccess = { storyId ->
                                                isLoading = false
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Thêm truyện thành công!")
                                                    title = ""; author = ""; description = ""; coverImageUrl = ""
                                                    selectedGenreIds.clear()
                                                    selectedDisplayLists.value = emptySet()
                                                }
                                            },
                                            onError = {
                                                isLoading = false
                                                scope.launch { snackbarHostState.showSnackbar("Lỗi: ${it.message}") }
                                            }
                                        )
                                    }
                                }
                            )
                        }

                        1 -> item {
                            UploadChapterForm(
                                stories = allStories,
                                selectedStoryTitle = selectedStoryTitle,
                                onStorySelected = { story ->
                                    selectedStoryId = story.id
                                    selectedStoryTitle = story.title
                                    showStoryDropdown = false
                                },
                                showDropdown = showStoryDropdown,
                                onDropdownToggle = { showStoryDropdown = it },
                                chapterNumber = chapterNumber,
                                onChapterNumberChange = { chapterNumber = it },
                                chapterTitle = chapterTitle,
                                onChapterTitleChange = { chapterTitle = it },
                                pageUrls = pageUrls,
                                onPageUrlsChange = { pageUrls = it },
                                isLoading = isLoading,
                                nextChapterNumber = nextChapterNumber,
                                onSubmit = {
                                    if (selectedStoryId == null) {
                                        scope.launch { snackbarHostState.showSnackbar("Vui lòng chọn truyện") }
                                        return@UploadChapterForm
                                    }

                                    val pages = pageUrls.lines().map { it.trim() }.filter { it.isNotEmpty() && it.startsWith("http") }
                                    if (pages.isEmpty()) {
                                        scope.launch { snackbarHostState.showSnackbar("Vui lòng nhập ít nhất 1 link ảnh hợp lệ") }
                                        return@UploadChapterForm
                                    }

                                    isLoading = true
                                    val newChapter = Chapter(
                                        title = chapterTitle.ifBlank { "Chương ${chapterNumber}" },
                                        pages = pages
                                    )

                                    viewModel.addChapter(
                                        mangaId = selectedStoryId!!,
                                        chapter = newChapter,
                                        userInputNumber = chapterNumber.takeIf { it.isNotBlank() }, // Chỉ gửi nếu có nhập
                                        onSuccess = {
                                            isLoading = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Thêm chương thành công!")
                                                chapterNumber = ""
                                                chapterTitle = ""
                                                pageUrls = ""
                                                // Cập nhật lại số chương tiếp theo
                                                viewModel.getLastChapterNumber(selectedStoryId!!) { last ->
                                                    nextChapterNumber = (last + 1).toInt()
                                                }
                                            }
                                        },
                                        onError = {
                                            isLoading = false
                                            scope.launch { snackbarHostState.showSnackbar("Lỗi: ${it.message}") }
                                        }
                                    )
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

// === Form Thêm/Sửa Truyện ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadStoryForm(
    title: String, onTitleChange: (String) -> Unit,
    author: String, onAuthorChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    coverImageUrl: String, onCoverImageChange: (String) -> Unit,
    status: String, onStatusChange: (String) -> Unit,
    allGenres: List<Genre>,
    selectedGenreIds: List<String>,
    onGenreToggle: (String) -> Unit,
    selectedDisplayLists: Set<String>,
    onDisplayListToggle: (String) -> Unit,
    isLoading: Boolean,
    isEditMode: Boolean,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Tên truyện *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = author, onValueChange = onAuthorChange, label = { Text("Tác giả *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = coverImageUrl, onValueChange = onCoverImageChange, label = { Text("URL ảnh bìa *") }, placeholder = { Text("https://...") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = onDescriptionChange, label = { Text("Giới thiệu") }, minLines = 3, modifier = Modifier.fillMaxWidth())

            // Status
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = status, onValueChange = {}, readOnly = true,
                    label = { Text("Trạng thái") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Đang ra", "Hoàn thành", "Tạm drop").forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { onStatusChange(it); expanded = false })
                    }
                }
            }

            // Genres
            Text("Thể loại *", fontWeight = FontWeight.Medium)
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allGenres.forEach { genre ->
                    FilterChip(
                        selected = selectedGenreIds.contains(genre.id.toString()),
                        onClick = { onGenreToggle(genre.id.toString()) },
                        label = { Text(genre.name) },
                        leadingIcon = {
                            if (selectedGenreIds.contains(genre.id.toString())) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }

            // Display Lists
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
                        onClick = { onDisplayListToggle(key) },
                        label = { Text(label) }
                    )
                }
            }

            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        if (isEditMode) "Cập nhật truyện" else "Thêm truyện mới",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
// === Form Thêm Chương ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadChapterForm(
    stories: List<Story>,
    selectedStoryTitle: String,
    onStorySelected: (Story) -> Unit,
    showDropdown: Boolean,
    onDropdownToggle: (Boolean) -> Unit,
    chapterNumber: String,
    onChapterNumberChange: (String) -> Unit,
    nextChapterNumber: Int,
    chapterTitle: String,
    onChapterTitleChange: (String) -> Unit,
    pageUrls: String,
    onPageUrlsChange: (String) -> Unit,
    isLoading: Boolean,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Chọn truyện
        ExposedDropdownMenuBox(expanded = showDropdown, onExpandedChange = onDropdownToggle) {
            OutlinedTextField(
                value = selectedStoryTitle,
                onValueChange = {},
                readOnly = true,
                label = { Text("Chọn truyện *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = showDropdown, onDismissRequest = { onDropdownToggle(false) }) {
                stories.forEach { story ->
                    DropdownMenuItem(
                        text = { Text(story.title) },
                        onClick = { onStorySelected(story) }
                    )
                }
            }
        }

        Text(
            text = "Số chương : $nextChapterNumber",
            modifier = Modifier.padding(8.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(value = chapterTitle, onValueChange = onChapterTitleChange, label = { Text("Tên chương (tùy chọn)") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = pageUrls,
            onValueChange = onPageUrlsChange,
            label = { Text("Dán link ảnh (mỗi link 1 dòng)") },
            placeholder = { Text("https://...\nhttps://...\nhttps://...") },
            minLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSubmit,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            else Text("Thêm chương mới", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}