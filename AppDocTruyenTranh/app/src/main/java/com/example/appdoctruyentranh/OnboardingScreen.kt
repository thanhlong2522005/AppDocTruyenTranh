package com.example.appdoctruyentranh // Thay bằng package của bạn

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Đảm bảo bạn đã import các thư viện này
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle

import androidx.navigation.NavController
import com.example.appdoctruyentranh.mangagoFontFamily
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(navController: NavController) {

    // --- PHẦN LOGIC TỰ ĐỘNG CHUYỂN MÀN HÌNH ---
    LaunchedEffect(key1 = true) {
        delay(3000L) // Chờ 3 giây
        navController.navigate("home") {
            popUpTo("onboarding") { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // LỚP 1: ẢNH NỀN
        Image(
            painter = painterResource(id = R.drawable.onboarding_image_1),
            contentDescription = "Onboarding Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // LỚP 2: NỘI DUNG (Chữ)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(all = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Logo MANGAGO (VẪN CÓ VIỀN)
            Box(
                contentAlignment = Alignment.Center // Căn giữa 2 lớp text
            ) {
                // Text viền
                Text(
                    text = "MANGAGO",
                    fontFamily = mangagoFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    style = TextStyle(
                        drawStyle = Stroke(
                            miter = 10f,
                            width = 15f,
                            join = StrokeJoin.Round
                        )
                    )
                )
                // Text chính
                Text(
                    text = "MANGAGO",
                    fontFamily = mangagoFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Slogan
            Text(
                text = "Cập nhật liên tục, trải nghiệm\nđọc trọn vẹn mọi lúc, mọi nơi",
                fontSize = 16.sp,
                color = Color.White, // Chữ màu trắng
                textAlign = TextAlign.Center
            )

            // Thêm một khoảng đệm ở dưới cùng để đẹp hơn
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}