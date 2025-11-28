package com.example.appdoctruyentranh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.ProfileViewModel
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val authViewModel: AuthViewModel = viewModel()
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    val isAdmin by authViewModel.isAdmin.collectAsState()

    // Sử dụng AuthStateListener để lắng nghe thay đổi đăng nhập trong thời gian thực
    DisposableEffect(auth) {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            authViewModel.checkAdminStatus()
        }
        auth.addAuthStateListener(authStateListener)
        onDispose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    Scaffold(
        topBar = { AppHeader(navigationIcon = null) },
        bottomBar = { AppBottomNavigationBar(navController = navController, isAdmin = isAdmin) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // SỬA: Truyền ProfileViewModel xuống ProfileHeader
                ProfileHeader(navController = navController, isAdmin = isAdmin, currentUser = currentUser)
            }

            // ... (Phần còn lại của file giữ nguyên)
            // Các mục chỉ dành cho người dùng thường đã đăng nhập
            if (currentUser != null && !isAdmin) {
                item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
                item { ProfileMenuItem(icon = Icons.Default.CloudDownload, title = "Truyện đã tải", onClick = { navController.navigate("download_manager") }) }
                item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
                item { ProfileMenuItem(icon = Icons.Default.History, title = "Lịch sử đọc", onClick = { navController.navigate("history") }) }
                item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
                item { ProfileMenuItem(icon = Icons.Default.Favorite, title = "Truyện yêu thích", onClick = { navController.navigate("favorite") }) }
            }

            // Các mục chung
            item { Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 8.dp) }
            item { ProfileMenuItem(icon = Icons.Default.Settings, title = "Cài đặt ứng dụng", onClick = { navController.navigate("settings") }) }
            item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
            // ⭐ KÍCH HOẠT ĐIỀU HƯỚNG TỚI MÀN HÌNH MỚI
            item { ProfileMenuItem(icon = Icons.Default.Info, title = "Giới thiệu & Hỗ trợ", onClick = { navController.navigate("about_support") }) }
            item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }

            // Mục dành riêng cho Admin
            if (isAdmin) {
                item { ProfileMenuItem(icon = Icons.Default.AdminPanelSettings, title = "Admin Panel", onClick = { navController.navigate("ManageStoriesScreen") }) }
                item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
            }

            // Nút đăng xuất (chỉ hiển thị khi đã đăng nhập)
            if (currentUser != null) {
                item { ProfileMenuItem(icon = Icons.Default.Logout, title = "Đăng xuất", isLogout = true, onClick = {
                    LoginManager.getInstance().logOut()
                    auth.signOut()
                }) }
                item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    navController: NavHostController,
    isAdmin: Boolean,
    currentUser: FirebaseUser?,
    profileViewModel: ProfileViewModel = viewModel() // SỬA: Khởi tạo viewModel ở đây
) {
    // SỬA: Lấy dữ liệu từ ProfileViewModel thay vì tự tính toán
    val userName by profileViewModel.userName.collectAsState()
    val userEmail by profileViewModel.userEmail.collectAsState()
    val userAvatarUrl by profileViewModel.userAvatarUrl.collectAsState()
    val gender by profileViewModel.gender.collectAsState()

    // SỬA: Sử dụng LaunchedEffect để ViewModel tải dữ liệu khi có thay đổi
    // Key là currentUser và navController.currentBackStackEntry để tải lại khi quay về từ màn hình EditProfile
    LaunchedEffect(currentUser, navController.currentBackStackEntry) {
        if (currentUser != null && !isAdmin) {
            profileViewModel.loadUserProfile()
        }
    }

    if (currentUser == null) {
        // Giao diện cho khách (chưa đăng nhập) -> Giữ nguyên
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Khách", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = { navController.navigate("login") }) {
                    Text("Đăng nhập / Đăng ký")
                }
            }
        }
    } else {
        // Giao diện cho người dùng đã đăng nhập (User hoặc Admin)
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // SỬA: Sử dụng userAvatarUrl từ ViewModel
                val avatarToShow = if (isAdmin) "" else userAvatarUrl
                if (avatarToShow.isNotBlank()) {
                    AsyncImage(
                        model = avatarToShow,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person
                        Icon(imageVector = icon, contentDescription = "Default Avatar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // SỬA: Sử dụng userName và userEmail từ ViewModel
                val nameToShow = if (isAdmin) "Admin" else userName
                val emailToShow = if (isAdmin) "admin@app.com" else userEmail
                Text(text = nameToShow, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                if (!isAdmin) {
                    Text(text = emailToShow, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (gender.isNotBlank()) {
                        Text(text = "Giới tính: $gender", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            if (!isAdmin) {
                IconButton(onClick = { navController.navigate("edit_profile") }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Chỉnh sửa hồ sơ", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}


@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isLogout: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val itemColor = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        val iconTint = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = itemColor
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}