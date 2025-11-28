package com.example.appdoctruyentranh

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Chưa xác định") }
    var isLoading by remember { mutableStateOf(false) }

    val defaultAvatars = remember { mutableStateListOf<String>() }
    // State để lưu trữ URL avatar hiện tại, sẽ được tải từ Firestore
    var selectedAvatarUrl by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Tải dữ liệu ban đầu từ Firestore
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // Tải thông tin người dùng (name, gender, imageUrl) từ Firestore
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        name = document.getString("name") ?: ""
                        gender = document.getString("gender") ?: "Chưa xác định"
                        selectedAvatarUrl = document.getString("imageUrl") ?: "" // Lấy avatar từ đây
                    }
                }

            // Tải danh sách các avatar mặc định
            firestore.collection("default_avatars").get()
                .addOnSuccessListener { result ->
                    val urls = result.documents.mapNotNull { it.getString("imageUrl") }
                    defaultAvatars.addAll(urls)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ") },
                navigationIcon = {
                    IconButton(onClick = { if (!isLoading) navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        TextButton(onClick = {
                            // --- SỬA LẠI LOGIC LƯU ---
                            if (currentUser == null) return@TextButton
                            if (name.isBlank()) {
                                Toast.makeText(context, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val userDocRef = firestore.collection("users").document(currentUser.uid)

                                    // Tạo map dữ liệu để cập nhật
                                    val userData = hashMapOf(
                                        "name" to name.trim(),
                                        "gender" to gender,
                                        "imageUrl" to selectedAvatarUrl // << QUAN TRỌNG: Cập nhật cả imageUrl
                                    )

                                    // Dùng set với merge=true để chỉ cập nhật các trường có trong map,
                                    // không xóa các trường khác như 'email' hay 'createdAt'
                                    userDocRef.set(userData, SetOptions.merge()).await()

                                    // Cập nhật Firebase Auth (tùy chọn nhưng nên làm để đồng bộ)
                                    // val profileBuilder = UserProfileChangeRequest.Builder()
                                    //     .setDisplayName(name.trim())
                                    //     .setPhotoUri(android.net.Uri.parse(selectedAvatarUrl))
                                    // currentUser.updateProfile(profileBuilder.build()).await()

                                    // Hoàn tất
                                    isLoading = false
                                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()

                                } catch (e: Exception) {
                                    isLoading = false
                                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Text("Lưu", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            // ... Phần UI còn lại giữ nguyên ...
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            AsyncImage(
                model = selectedAvatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Chọn ảnh đại diện", fontWeight = FontWeight.Medium)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(defaultAvatars) { avatarUrl ->
                    val isSelected = selectedAvatarUrl == avatarUrl
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Default Avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { selectedAvatarUrl = avatarUrl }
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên hiển thị") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }
            val genderOptions = listOf("Nam", "Nữ", "Khác", "Chưa xác định")
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Giới tính") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
