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
        // ... (các màn hình khác giữ nguyên)

        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("onboarding") {
            OnboardingScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
        composable("reset_password") {
            ResetPasswordScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("favorite") {
            FavoriteScreen(navController = navController)
        }
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

        composable("genre") {
            GenreScreen(navController = navController)
        }
        composable(
            route = "genre_detail/{genreId}/{genreName}",
            arguments = listOf(
                navArgument("genreId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("genreName") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getInt("genreId") ?: 0
            val genreName = backStackEntry.arguments?.getString("genreName") ?: ""
            GenreDetailScreen(navController = navController, genreId = genreId, genreName = genreName)
        }
        composable(
            route = "manga_detail/{mangaId}",
            arguments = listOf(navArgument("mangaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: return@composable
            MangaDetailScreen(navController = navController, mangaId = mangaId)
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }
        composable("ManageStoriesScreen") {
            ManageStoriesScreen(navController = navController)
        }

        // --- ROUTE ADMIN ĐƯỢC THÊM VÀO ---
        composable(
            route = "admin_upload?mangaId={mangaId}",
            arguments = listOf(navArgument("mangaId") { nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId")
            AdminUploadScreen(navController = navController, preselectedMangaId = mangaId)
        }
        composable(
            route = "edit_story/{mangaId}",
            arguments = listOf(navArgument("mangaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId")!!
            EditStoryScreen(navController = navController, mangaId = mangaId)
        }
        // --- HẾT PHẦN THÊM ADMIN ROUTE ---

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
        composable("download_manager") {
            DownloadManagerScreen(navController = navController)
        }
        composable("settings") {
            SettingScreen(navController = navController)
        }
    }
}