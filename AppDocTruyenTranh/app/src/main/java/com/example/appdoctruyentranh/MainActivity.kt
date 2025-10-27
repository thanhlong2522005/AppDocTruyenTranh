package com.example.appdoctruyentranh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.appdoctruyentranh.ui.theme.AppDocTruyenTranhTheme
import com.example.appdoctruyentranh.HomeScreen
import com.example.appdoctruyentranh.SearchScreen
import com.example.appdoctruyentranh.GenreScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Bước 1: Kích hoạt Splash Screen (từ Bước 2)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            AppDocTruyenTranhTheme { // Theme Compose của bạn
                AppNavigation()
            }
        }
    }
}

// Nơi quản lý tất cả các màn hình
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        // 1. Màn hình Splash
        composable("splash") {
            SplashScreen(navController = navController)
            // (Không thay đổi gì ở màn hình Splash)
        }

        // 2. Màn hình Onboarding (Giới thiệu)
        composable("onboarding") {
            // Chúng ta sẽ gọi màn hình mới ở đây
            // Đảm bảo bạn đã import OnboardingScreen
            OnboardingScreen(navController = navController)
        }

        // 3. Màn hình Đăng nhập (Chúng ta sẽ thêm sau)
        composable("login") {
            // Thay thế Box placeholder bằng màn hình thật
            LoginScreen(navController = navController)
        }

        // 4. Màn hình Đăng ký (Thêm sẵn placeholder)
        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
        composable("reset_password") {
            ResetPasswordScreen(navController = navController)
        }
        // 7. Màn hình Trang chủ
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("genre") {
            GenreScreen(navController = navController)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }


    }
}