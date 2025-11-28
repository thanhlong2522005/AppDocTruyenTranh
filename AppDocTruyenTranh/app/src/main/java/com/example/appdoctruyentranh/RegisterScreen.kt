package com.example.appdoctruyentranh

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appdoctruyentranh.viewmodel.AuthViewModel
import com.example.appdoctruyentranh.viewmodel.RegistrationState

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel() // Giờ sẽ không lỗi nữa
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Lấy màu từ Theme
    val colorScheme = MaterialTheme.colorScheme
    val facebookButtonColor = Color(0xFF1877F2)

    val registrationState by authViewModel.registrationState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    // Lắng nghe trạng thái đăng ký từ ViewModel
    LaunchedEffect(registrationState) {
        isLoading = registrationState is RegistrationState.Loading

        when (val state = registrationState) {
            is RegistrationState.Success -> {
                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }
            is RegistrationState.Error -> {
                Toast.makeText(context, "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
            }
            else -> {} // Trạng thái Idle
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text("Đăng Ký", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Text("Sign up to start", fontSize = 16.sp, color = colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(40.dp))

            ClickableText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = colorScheme.onSurfaceVariant, fontSize = 14.sp)) {
                        append("Have account? ")
                    }
                    withStyle(style = SpanStyle(color = facebookButtonColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                        append("Sign in!")
                    }
                },
                onClick = { offset ->
                    if (offset > 17) {
                        navController.navigate("login")
                    }
                }
            )

            Spacer(modifier = Modifier.height(300.dp))
        }

        // Form đăng ký dưới đáy
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 40.dp,
                        topEnd = 40.dp
                    )
                )
                .background(colorScheme.surfaceVariant)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Tên hiển thị",
                keyboardType = KeyboardType.Text // SỬA: Thêm KeyboardType
            )
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Nhập email",
                keyboardType = KeyboardType.Email // SỬA: Thêm KeyboardType
            )
            Spacer(modifier = Modifier.height(16.dp))

            CustomPasswordTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Mật khẩu"
            )
            Spacer(modifier = Modifier.height(16.dp))

            CustomPasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Nhập lại mật khẩu"
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Nút Đăng ký
            Button(
                onClick = {
                    // SỬA: Gọi đến ViewModel thay vì xử lý trực tiếp
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Gọi hàm trong ViewModel để thực hiện đăng ký
                        authViewModel.registerUser(email, password, name)
                    }
                },
                enabled = !isLoading, // Vô hiệu hóa nút khi đang tải
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(text = "Đăng Ký", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Nút quay về
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 32.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay về", tint = colorScheme.onSurface)
        }
    }
}
