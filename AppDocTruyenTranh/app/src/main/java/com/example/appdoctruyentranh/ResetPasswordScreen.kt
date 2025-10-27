package com.example.appdoctruyentranh

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavController) {

    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val buttonColor = Color(0xFFC0C8FF) // Màu xanh nhạt của nút

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Đặt lại mật khẩu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nhập mã khôi phục mật khẩu được gửi về",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Ô nhập mã
            CustomTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = "Nhập mã khôi phục",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ô mật khẩu mới
            CustomPasswordTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = "Nhập mật khẩu mới"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ô xác nhận mật khẩu
            CustomPasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Xác nhận mật khẩu mới"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nút Hoàn Tất
            Button(
                onClick = {
                    // Xong thì quay về màn hình Đăng nhập
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Hoàn Tất", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link Gửi lại mã
            TextButton(onClick = { /* TODO: Xử lý logic gửi lại mã */ }) {
                Text(text = "gửi lại mã", color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}