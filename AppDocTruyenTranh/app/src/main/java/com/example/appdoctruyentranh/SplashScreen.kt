package com.example.appdoctruyentranh


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    // --- PHẦN LOGIC ---
    // Chạy một coroutine (tác vụ nền)
    LaunchedEffect(key1 = true) {
        delay(2000L) // Chờ 2 giây

        // Chuyển màn hình
        navController.navigate("onboarding") {
            // Xóa màn hình Splash khỏi backstack
            // để người dùng không thể nhấn "Back" quay lại
            popUpTo("splash") {
                inclusive = true
            }
        }
    }

    // --- PHẦN GIAO DIỆN (UI) ---
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Logo
                Image(
                    painter = painterResource(id = R.drawable.mangago_logo),
                    contentDescription = "Mangago Logo"
                )

                // 2. Tên App "MANGAGO"
                Box {
                    // Text viền (lớp dưới)
                    Text(
                        text = "MANGAGO",
                        fontFamily = mangagoFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black, // Màu viền
                        style = TextStyle(
                            drawStyle = Stroke(
                                miter = 10f,
                                width = 15f, // Độ dày viền logo
                                join = StrokeJoin.Round
                            )
                        )
                    )
                    // Text chính (lớp trên)
                    Text(
                        text = "MANGAGO",
                        fontFamily = mangagoFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White // Màu chữ
                    )
                }

                // 3. Slogan
                Text(
                    text = "Truyện Hay Mỗi Ngày, \"Read & Enjoy\"",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}