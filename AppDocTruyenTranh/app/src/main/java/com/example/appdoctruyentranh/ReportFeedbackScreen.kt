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
import com.example.appdoctruyentranh.PrimaryColor // Sử dụng màu chính

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFeedbackScreen(navController: NavController) {
    var subject by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo & Phản hồi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gửi phản hồi, báo cáo lỗi ảnh, lỗi dịch, hoặc đề xuất tính năng.",
                color = Color.Gray,
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
                    if (subject.isNotBlank() && details.isNotBlank()) {
                        // TODO: Thêm logic gửi dữ liệu lên Firebase/Backend tại đây
                        Toast.makeText(context, "Đã gửi phản hồi thành công!", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(text = "Gửi Báo cáo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}