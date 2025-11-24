// File: ReportFeedbackScreen.kt (Tái cấu trúc)
package com.example.appdoctruyentranh

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appdoctruyentranh.PrimaryColor
import com.example.appdoctruyentranh.viewmodel.FeedbackViewModel // ⭐ Thêm ViewModel mới

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFeedbackScreen(
    navController: NavController,
    viewModel: FeedbackViewModel = viewModel() // ⭐ Sử dụng ViewModel mới
) {
    var subject by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    val context = LocalContext.current

    // ⭐ Lắng nghe trạng thái từ ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.submissionSuccess.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    // ⭐ Xử lý kết quả gửi (Thành công hoặc Lỗi)
    LaunchedEffect(isSuccess) {
        if (isSuccess == true) {
            Toast.makeText(context, "Đã gửi phản hồi thành công! Cảm ơn bạn.", Toast.LENGTH_LONG).show()
            viewModel.clearStatus() // Xóa trạng thái để tránh lặp lại
            navController.popBackStack() // Quay lại
        } else if (isSuccess == false && errorMessage != null) {
            Toast.makeText(context, "Lỗi: $errorMessage", Toast.LENGTH_LONG).show()
            viewModel.clearStatus()
        }
    }

    // ⭐ Xử lý lỗi validation (ví dụ: lỗi nhập thiếu)
    LaunchedEffect(errorMessage) {
        if (errorMessage != null && isSuccess == null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo & Phản hồi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gửi phản hồi, báo cáo lỗi ảnh, lỗi dịch, hoặc đề xuất tính năng. (Thông tin người gửi được bảo mật)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Chủ đề/Loại báo cáo
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Chủ đề (VD: Lỗi dịch chương 5)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Nội dung chi tiết
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Mô tả chi tiết vấn đề") },
                minLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Nút Gửi
            Button(
                onClick = {
                    // ⭐ GỌI HÀM SUBMIT TỪ VIEWMODEL
                    viewModel.submit(subject, details)
                },
                enabled = !isLoading, // Vô hiệu hóa khi đang tải
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = "Gửi Báo cáo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}