package com.example.appdoctruyentranh // Phải cùng package

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdoctruyentranh.model.Story

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder, color = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = Color.LightGray,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp), // Bo tròn
        singleLine = true
    )
}

/**
 * Ô nhập liệu MẬT KHẨU CÓ VIỀN TRÒN (có icon Ẩn/Hiện)
 */
@Composable
fun CustomPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder, color = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = Color.LightGray,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        // Ẩn/Hiện mật khẩu
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        // Icon Ẩn/Hiện
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Filled.Visibility
            else Icons.Filled.VisibilityOff

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = "Toggle password visibility")
            }
        }
    )
}


// =========================================================================
// PHẦN HEADER/LOGO (Để dùng chung)
// =========================================================================
val PrimaryColor = Color(0xFF00BFFF) // Màu xanh MANGAGO
val GrayIcon = Color.Gray

// Data class cho Bottom Navigation Item
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)
// Cập nhật Story để phù hợp với việc sử dụng (Mặc dù phần này nên nằm ở model/data)
data class Story(
    val id: Int,
    val title: String,
    val coverUrl: String? = null,
    val latestChapter: String? = null // Thêm trường này cho phong phú
)

// =========================================================================
// 1. AppHeader (Logo và Tên ứng dụng)
// =========================================================================

@Composable
fun AppHeader(
    // Tham số mới: Một Composable lambda tùy chọn cho Icon điều hướng
    navigationIcon: @Composable (() -> Unit)? = null
) {
    // Định nghĩa Icon Menu mặc định
    val defaultNavigationIcon: @Composable (() -> Unit) = {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = Color.White,
            modifier = Modifier
                .size(56.dp) // Kích thước bằng chiều cao của Header
                .padding(16.dp)
                .clickable { /* Mở Drawer Navigation */ }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(PrimaryColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Icon Điều hướng (Nếu navigationIcon không được cung cấp, dùng Icon Menu mặc định)
        (navigationIcon ?: defaultNavigationIcon).invoke()


        // 2. Logo/Tên Ứng dụng
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            // LOGO PLACEHOLDER

            Image(
                painter = painterResource(id = R.drawable.mangago_logo),
                contentDescription = "MangaGo Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )


            Spacer(modifier = Modifier.width(8.dp))

            // Tên Ứng dụng
            Text(
                text = "MANGAGO",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }

        // 3. Placeholder cho không gian trống
        Spacer(modifier = Modifier.width(24.dp))
    }
}
// =========================================================================
// 2. AppBottomNavigationBar
// =========================================================================

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.height(60.dp),
        tonalElevation = 5.dp
    ) {
        val items = listOf(
            BottomNavItem("home", Icons.Default.Home, "Trang chủ"),
            BottomNavItem("genre", Icons.Default.Menu, "Danh mục"),
            BottomNavItem("search", Icons.Default.Search, "Tìm kiếm"),
            BottomNavItem("favorite", Icons.Default.Favorite, "Yêu thích"),
            // Cập nhật route cho Cá nhân
            BottomNavItem("profile", Icons.Default.Person, "Cá nhân")
        )

        items.forEach { item ->
            val isSelected = item.route == currentDestination

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (item.route != currentDestination) {
                        navController.navigate(item.route) {
                            // Cấu hình popUpTo, launchSingleTop, restoreState
                            // Giả định NavController đã được cấu hình đúng.
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) PrimaryColor else GrayIcon
                    )
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 10.sp,
                        color = if (isSelected) PrimaryColor else GrayIcon
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}