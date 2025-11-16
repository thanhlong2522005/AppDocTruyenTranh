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

import androidx.navigation.navArgument

import androidx.navigation.NavType

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import com.example.appdoctruyentranh.ui.theme.AppDocTruyenTranhTheme

import com.example.appdoctruyentranh.HomeScreen

import com.example.appdoctruyentranh.SearchScreen

import com.example.appdoctruyentranh.GenreScreen

import com.example.appdoctruyentranh.MangaDetailScreen

import com.example.appdoctruyentranh.FavoriteScreen





class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

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



        // 8. Màn hình Tìm kiếm

        composable("search") {

            SearchScreen(navController = navController)

        }



        // 9. Màn hình Yêu thích

        composable("favorite") {

            FavoriteScreen(navController = navController)

        }

        // 10. Màn hình Lịch sử (Dựa trên file HistoryScreen.kt)

        composable("history") {

            HistoryScreen(navController = navController)

        }

        composable(

            "adminUpload?mangaId={mangaId}",

            arguments = listOf(

                navArgument("mangaId") {

                    defaultValue = ""

                    nullable = true

                }

            )

        ) { backStackEntry ->

            val mangaId = backStackEntry.arguments?.getString("mangaId")

            AdminUploadScreen(navController = navController, preselectedMangaId = mangaId)

        }

        composable(

            "editStory/{mangaId}",

            arguments = listOf(navArgument("mangaId") { type = NavType.StringType })

        ) { backStackEntry ->

            val mangaId = backStackEntry.arguments?.getString("mangaId")!!

            EditStoryScreen(navController = navController, mangaId = mangaId)

        }



        composable("story_list/{title}") { backStackEntry ->

            val title = backStackEntry.arguments?.getString("title") ?: ""

            AllStoriesScreen(title, navController = navController)

        }







        // 11. Màn hình Thể loại

        composable("genre") {

            GenreScreen(navController = navController)

        }

        composable(

            route = "genre_detail/{genreId}/{genreName}",

            arguments = listOf(

                navArgument("genreId") {

                    type = NavType.IntType

                    defaultValue = 0

                },

                navArgument("genreName") {

                    type = NavType.StringType

                    defaultValue = ""

                }

            )

        ) { backStackEntry ->

            val genreId = backStackEntry.arguments?.getInt("genreId") ?: 0

            val genreName = backStackEntry.arguments?.getString("genreName") ?: ""



            GenreDetailScreen(

                navController = navController,

                genreId = genreId,

                genreName = genreName

            )

        }



        // 12. Màn hình Chi tiết Truyện

        composable(

            route = "manga_detail/{mangaId}",

            arguments = listOf(

                navArgument("mangaId") { type = NavType.StringType }

            )

        ) { backStackEntry ->

            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: return@composable

            MangaDetailScreen(navController = navController, mangaId = mangaId)

        }



        // ----------------------------------------------------------------

        // ROUTE CHO THÀNH VIÊN 3

        // ----------------------------------------------------------------



        // 13. Màn hình Cá nhân/Hồ sơ (Thay thế cho "history" trong bottom nav)

        composable("profile") {

            ProfileScreen(navController = navController)

        }



        composable("edit_profile") {

            EditProfileScreen(navController = navController)

        }



        // 14. Màn hình Đọc Truyện (từ Chi tiết truyện)

        composable(

            route = "read/{mangaId}/{chapterId}",

            arguments = listOf(

                navArgument("mangaId") { type = NavType.StringType },

                navArgument("chapterId") { type = NavType.StringType }

            )

        ) { backStackEntry ->

            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: return@composable

            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: "1"

            ReadScreen(navController = navController, mangaId = mangaId, chapterId = chapterId)

        }



        // 15. Màn hình Quản lý Tải xuống

        composable("download_manager") {

            DownloadManagerScreen(navController = navController)

        }



        // 16. Màn hình Cài đặt

        composable("settings") {

            SettingScreen(navController = navController)

        }

        // THEM: Man hinh Quan ly Tai xuong
        composable("download_manager") {
            // Can dam bao cac import cho DownloadManagerScreen
            DownloadManagerScreen(navController)
        }

        // THEM: Man hinh Bao cao/Phan hoi
        composable("report_feedback") {
            // Can dam bao import cho ReportFeedbackScreen
            ReportFeedbackScreen(navController)
        }

    }

}
