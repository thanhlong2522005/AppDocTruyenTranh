package com.example.appdoctruyentranh // Thay bằng package của bạn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation // <-- Đảm bảo có import này
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- Định nghĩa màu sắc (giữ nguyên) ---
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE1E0FF), Color(0xFFF8F8FF))
    )
    val facebookButtonColor = Color(0xFF1877F2)
    val loginButtonColor = Color(0xFFB0B0B0)
    val lightTextColor = Color.Gray

    // --- Bố cục chính (SỬA DÙNG BOX) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding() // <-- CHÌA KHÓA LÀ ĐẶT NÓ Ở ĐÂY
    ) {
        // --- PHẦN 1: NỘI DUNG TRÊN (NỀN TRẮNG) ---
        // (Phần này phải cuộn được)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Cho phép cuộn khi nội dung bị đẩy lên
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Tiêu đề
            Text(
                text = "Đăng Nhập",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Sign in to start",
                fontSize = 16.sp,
                color = lightTextColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Nút Đăng nhập Google
            Button(
                onClick = { /* TODO: Xử lý logic Google Login */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Đăng nhập bằng Google",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Đăng nhập Facebook
            Button(
                onClick = { /* TODO: Xử lý logic Facebook Login */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = facebookButtonColor,
                    contentColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_facebook),
                    contentDescription = "Facebook Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Đăng nhập bằng Facebook",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Link "Sign up"
            ClickableText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = lightTextColor, fontSize = 14.sp)) {
                        append("Haven't account? ")
                    }
                    withStyle(style = SpanStyle(
                        color = facebookButtonColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )) {
                        append("Sign up!")
                    }
                },
                onClick = { offset ->
                    if (offset > 17) {
                        navController.navigate("register")
                    }
                }
            )

            // Thêm một khoảng đệm ở cuối để cuộn cho đẹp
            // (Khi form tím đẩy lên, phần này sẽ bị che)
            Spacer(modifier = Modifier.height(300.dp))
        }

        // --- PHẦN 2: NỘI DUNG DƯỚI (NỀN CONG) ---
        // (Phần này căn đáy)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter) // <-- Luôn dính ở đáy Box
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(gradientBrush)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Ô nhập Email
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Nhập email hoặc số điện thoại",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ô nhập Mật khẩu
            CustomPasswordTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Mật khẩu"
            )

            // Link Quên mật khẩu
            TextButton(
                onClick = { navController.navigate("forgot_password") }, // <-- ĐÃ SỬA
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Quên mật khẩu",
                    color = lightTextColor,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Đăng nhập
            Button(
                onClick = { /* TODO: Xử lý logic Đăng nhập */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = loginButtonColor,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Đăng Nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

