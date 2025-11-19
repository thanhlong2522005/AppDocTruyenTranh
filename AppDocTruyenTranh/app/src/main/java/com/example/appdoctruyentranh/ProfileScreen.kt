package com.example.appdoctruyentranh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val authViewModel: AuthViewModel = viewModel()
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    val isAdmin by authViewModel.isAdmin.collectAsState()

    // Tự động làm mới khi quay lại màn hình
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentUser = auth.currentUser
                authViewModel.checkAdminStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = { AppHeader(navigationIcon = null) },
        bottomBar = { AppBottomNavigationBar(navController = navController, isAdmin = isAdmin) },
        // SỬA: Đảm bảo nền của Scaffold theo theme
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (currentUser != null) {
                LoggedInProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    currentUser = currentUser, // Truyền currentUser xuống
                    onSignOut = {
                        currentUser = null
                        authViewModel.checkAdminStatus()
                    }
                )
            } else {
                GuestProfileScreen(navController = navController)
            }
        }
    }
}

@Composable
fun LoggedInProfileScreen(navController: NavHostController, authViewModel: AuthViewModel, currentUser: FirebaseUser?, onSignOut: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            // SỬA: Đảm bảo nền của LazyColumn theo theme
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            ProfileHeader(navController = navController, isAdmin = isAdmin, currentUser = currentUser) // Truyền currentUser xuống
        }

        if (!isAdmin) {
            // SỬA: Dùng màu theme cho Divider
            item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { ProfileMenuItem(icon = Icons.Default.CloudDownload, title = "Truyện đã tải", onClick = { navController.navigate("download_manager") }) }
            item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { ProfileMenuItem(icon = Icons.Default.History, title = "Lịch sử đọc", onClick = { navController.navigate("history") }) }
            item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { ProfileMenuItem(icon = Icons.Default.Favorite, title = "Truyện yêu thích", onClick = { navController.navigate("favorite") }) }
        }

        // SỬA: Dùng màu theme cho Divider dày
        item { Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 8.dp) }
        item { ProfileMenuItem(icon = Icons.Default.Settings, title = "Cài đặt ứng dụng", onClick = { navController.navigate("settings") }) }
        item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
        item { ProfileMenuItem(icon = Icons.Default.Info, title = "Giới thiệu & Hỗ trợ", onClick = { /*TODO*/ }) }
        item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }

        if (isAdmin) {
            item { ProfileMenuItem(icon = Icons.Default.AdminPanelSettings, title = "Admin Panel", onClick = { navController.navigate("ManageStoriesScreen") }) }
            item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
        }

        item { ProfileMenuItem(icon = Icons.Default.Logout, title = "Đăng xuất", isLogout = true, onClick = {
            LoginManager.getInstance().logOut()
            auth.signOut()
            onSignOut()
        }) }
        item { Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
    }
}

@Composable
fun GuestProfileScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize()
            // SỬA: Đảm bảo nền theo theme
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // SỬA: Dùng màu theme cho Icon Avatar
                Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            // SỬA: Dùng màu theme cho Text
            Text("Khách", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SỬA: Dùng màu theme cho Text
            Text("Đăng nhập để có trải nghiệm tốt nhất", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            // SỬA: Dùng màu theme cho Text
            Text("Lưu truyện yêu thích, lịch sử đọc và nhiều hơn nữa!", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Đăng nhập / Đăng ký", fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun ProfileHeader(navController: NavHostController, isAdmin: Boolean, currentUser: FirebaseUser?) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var gender by remember { mutableStateOf("") }

    val imageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .build()

    if (!isAdmin) {
        LaunchedEffect(currentUser) {
            if (currentUser != null) {
                firestore.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            gender = document.getString("gender") ?: ""
                        }
                    }
            }
        }
    }

    val userName = if (isAdmin) "Admin" else currentUser?.displayName ?: "Tên Người Dùng"
    val userEmail = currentUser?.email ?: "user@example.com"
    val userPhotoUrl = if (isAdmin) null else currentUser?.photoUrl

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
            // SỬA: Đảm bảo nền của header theo theme
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(70.dp).clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (userPhotoUrl != null) {
                AsyncImage(
                    model = userPhotoUrl,
                    imageLoader = imageLoader,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    // SỬA: Dùng màu theme cho nền avatar mặc định
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person
                    // SỬA: Dùng màu theme cho Icon
                    Icon(imageVector = icon, contentDescription = "Default Avatar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // SỬA: Dùng màu theme cho Text Tên
            Text(text = userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (!isAdmin) {
                // SỬA: Dùng màu theme cho Text Email
                Text(text = userEmail, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (gender.isNotBlank()) {
                    // SỬA: Dùng màu theme cho Text Giới tính
                    Text(text = "Giới tính: $gender", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        if (!isAdmin) {
            IconButton(onClick = { navController.navigate("edit_profile") }) {
                // SỬA: Dùng màu theme cho Icon
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Chỉnh sửa hồ sơ", tint = MaterialTheme.colorScheme.onSurface)
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
        // Màu chữ và Icon sẽ là màu Theme, trừ khi là Đăng xuất
        val itemColor = if (isLogout) Color.Red else MaterialTheme.colorScheme.onSurface
        val iconTint = if (isLogout) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                // SỬA: Dùng màu itemColor
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                // SỬA: Dùng màu itemColor
                color = itemColor
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            // SỬA: Dùng màu theme
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}