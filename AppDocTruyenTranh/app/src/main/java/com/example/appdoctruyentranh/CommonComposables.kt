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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdoctruyentranh.R // Import cần thiết cho R.drawable.mangago_logo
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
val GrayIcon = Color.Gray // Biến này không cần thiết nữa nếu dùng colorScheme

// Data class cho Bottom Navigation Item
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)
data class Story(
    val id: Int,
    val title: String,
    val coverUrl: String? = null,
    val latestChapter: String? = null
)


@Composable
fun MangagoLogoText() {
    Box {
        // Text viền (lớp dưới)
        Text(
            text = "MANGAGO",
            fontFamily = mangagoFontFamily,
            fontSize = 28.sp, // Tăng kích thước
            fontWeight = FontWeight.Bold,
            color = Color.Black, // Màu viền
            style = TextStyle(
                drawStyle = Stroke(
                    miter = 10f,
                    width = 10f, // Tăng độ dày viền
                    join = StrokeJoin.Round
                )
            )
        )
        // Text chính (lớp trên)
        Text(
            text = "MANGAGO",
            fontFamily = mangagoFontFamily,
            fontSize = 28.sp, // Tăng kích thước
            fontWeight = FontWeight.Bold,
            color = Color.White // Màu chữ
        )
    }
}

// =========================================================================
// 1. AppHeader (Logo và Tên ứng dụng)
// =========================================================================

@Composable
fun AppHeader(
    // Tham số mới: Một Composable lambda tùy chọn cho Icon điều hướng
    navigationIcon: @Composable (() -> Unit)? = null
) {
    // Định nghĩa Icon Menu mặc định


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(PrimaryColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

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

            // Tên Ứng dụng - ĐÃ ĐƯỢC THAY THẾ
            MangagoLogoText()
        }

        // 3. Placeholder cho không gian trống
        Spacer(modifier = Modifier.width(24.dp))
    }
}
// =========================================================================
// 2. AppBottomNavigationBar (ĐÃ SỬA ĐỂ HỖ TRỢ DARK MODE)
// =========================================================================

@Composable
fun AppBottomNavigationBar(navController: NavHostController, isAdmin: Boolean) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination?.route

    // Lấy màu từ Theme hiện tại
    val colorScheme = MaterialTheme.colorScheme

    // Định nghĩa màu cho các trạng thái:
    val selectedColor = PrimaryColor // Màu khi chọn (không đổi, vẫn dùng màu xanh Primary)
    // ⭐ Màu khi không chọn, lấy từ onSurfaceVariant (màu chữ/icon phụ) của Theme
    val unselectedColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val allItems = listOf(
        BottomNavItem("home", Icons.Default.Home, "Trang chủ"),
        BottomNavItem("genre", Icons.Default.Menu, "Danh mục"),
        BottomNavItem("search", Icons.Default.Search, "Tìm kiếm"),
        BottomNavItem("favorite", Icons.Default.Favorite, "Yêu thích"),
        BottomNavItem("profile", Icons.Default.Person, "Cá nhân")
    )

    val items = if (isAdmin) allItems.filter { it.route != "favorite" } else allItems

    NavigationBar(
        // ⭐ THAY THẾ MÀU NỀN CỨNG BẰNG MÀU SURFACE CỦA CHỦ ĐỀ
        containerColor = colorScheme.surface,
        modifier = Modifier.height(60.dp),
        tonalElevation = 5.dp
    ) {
        items.forEach { item ->
            val isSelected = item.route == currentDestination

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (item.route != currentDestination) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        // ⭐ SỬ DỤNG MÀU CHỦ ĐỀ (PrimaryColor hoặc màu Theme)
                        tint = if (isSelected) selectedColor else unselectedColor
                    )
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 10.sp,
                        // ⭐ SỬ DỤNG MÀU CHỦ ĐỀ (PrimaryColor hoặc màu Theme)
                        color = if (isSelected) selectedColor else unselectedColor
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    // ⭐ Đảm bảo màu nền khi nhấn (Press/Selected) cũng dùng màu Theme
                    selectedIconColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    selectedTextColor = selectedColor,
                    unselectedTextColor = unselectedColor,
                    // Đặt màu nền khi nhấp chuột (hover/press) trong suốt
                    // indicatorColor = colorScheme.onSurface.copy(alpha = 0.08f)
                )
            )
        }
    }
}