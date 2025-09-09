package com.example.gyropong.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gyropong.ui.screens.*
import com.example.gyropong.ui.viewmodels.SessionViewModel
import com.example.gyropong.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import com.example.gyropong.R
import com.example.gyropong.ui.viewmodels.BluetoothViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel
) {
    val scope = rememberCoroutineScope()

    // 👇 BluetoothViewModel compartido por todo el flujo
    val bluetoothVM: BluetoothViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // --- SPLASH SCREEN ---
        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                sessionViewModel = sessionViewModel,
                userViewModel = userViewModel,
                splashTime = 2000L
            )
        }

        // --- HOME SCREEN ---
        composable(Screen.Home.route) {
            HomeScreen(
                onQuickMatchClick = {
                    navController.navigate(Screen.QuickMatchSetup.route)
                },
                onSessionClick = {
                    navController.navigate(Screen.Session.route)
                }
            )
        }

        // --- SESSION SCREEN ---
        composable(Screen.Session.route) {
            SessionScreen(
                userViewModel = userViewModel,
                sessionViewModel = sessionViewModel,
                onBack = { /* No volver desde aquí */ },
                onLoginSuccess = {
                    scope.launch {
                        val user = userViewModel.currentUser.value
                        if (user != null) {
                            navController.navigate("${Screen.FindMatch.route}/${user.username}/🐱") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }

        // --- QUICK MATCH SETUP SCREEN ---
        composable(Screen.QuickMatchSetup.route) {
            QuickMatchSetupScreen(
                avatars = listOf(
                    R.drawable.avatar_bear,
                    R.drawable.avatar_lion,
                    R.drawable.avatar_frog,
                    R.drawable.avatar_monkey
                ),
                onBack = { /* No volver desde aquí */ },
                onContinue = { nickname, avatar ->
                    navController.navigate("${Screen.FindMatch.route}/$nickname/$avatar") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // --- FIND MATCH SCREEN ---
        composable(
            route = "${Screen.FindMatch.route}/{nickname}/{avatar}",
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType },
                navArgument("avatar") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nickname = backStackEntry.arguments?.getString("nickname") ?: "Guest"
            val avatar = backStackEntry.arguments?.getString("avatar") ?: "🐱"
            val currentUser by userViewModel.currentUser.collectAsState()

            FindMatchScreen(
                bluetoothVM = bluetoothVM, // 👈 se pasa el mismo VM
                nickname = nickname,
                avatar = avatar,
                currentUser = currentUser,
                navController = navController
            )
        }

        // --- GAME SCREEN ---
        composable(
            route = "${Screen.GyroPongGame.route}/{nickname}/{opponent}",
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType },
                navArgument("opponent") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nickname = backStackEntry.arguments?.getString("nickname") ?: "Guest"
            val opponent = backStackEntry.arguments?.getString("opponent") ?: "???"

            GyroPongGameScreen(
                bluetoothVM = bluetoothVM, // 👈 se pasa el mismo VM
                nickname = nickname,
                initialOpponentNickname = opponent,
                initialIsConnected = bluetoothVM.isConnected.collectAsState().value,
                onExit = { navController.popBackStack() }
            )
        }

        // --- PROFILE SCREEN ---
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                sessionViewModel = sessionViewModel,
                userViewModel = userViewModel
            )
        }
    }
}

/*
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // --- SPLASH SCREEN ---
        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                sessionViewModel = sessionViewModel,
                userViewModel = userViewModel,
                splashTime = 2000L
            )
        }

        // --- HOME SCREEN ---
        composable(Screen.Home.route) {
            HomeScreen(
                onQuickMatchClick = {
                    navController.navigate(Screen.QuickMatchSetup.route)
                },
                onSessionClick = {
                    navController.navigate(Screen.Session.route)
                }
            )
        }

        // --- SESSION SCREEN ---
        composable(Screen.Session.route) {
            SessionScreen(
                userViewModel = userViewModel,
                sessionViewModel = sessionViewModel,
                onBack = { /* No volver desde aquí */ },
                onLoginSuccess = {
                    scope.launch {
                        val user = userViewModel.currentUser.value
                        if (user != null) {
                            navController.navigate("${Screen.FindMatch.route}/${user.username}/🐱") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }

        // --- QUICK MATCH SETUP SCREEN ---
        composable(Screen.QuickMatchSetup.route) {
            QuickMatchSetupScreen(
                avatars = listOf(
                    R.drawable.avatar_bear,
                    R.drawable.avatar_lion,
                    R.drawable.avatar_frog,
                    R.drawable.avatar_monkey
                ),
                onBack = { /* No volver desde aquí */ },
                onContinue = { nickname, avatar ->
                    navController.navigate("${Screen.FindMatch.route}/$nickname/$avatar") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // --- FIND MATCH SCREEN ---
        composable(
            route = "${Screen.FindMatch.route}/{nickname}/{avatar}",
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType },
                navArgument("avatar") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nickname = backStackEntry.arguments?.getString("nickname") ?: "Guest"
            val avatar = backStackEntry.arguments?.getString("avatar") ?: "🐱"
            val currentUser by userViewModel.currentUser.collectAsState()

            FindMatchScreen(
                nickname = nickname,
                avatar = avatar,
                currentUser = currentUser,
                navController = navController
            )
        }

        // --- GAME SCREEN ---
        composable(
            route = "${Screen.GyroPongGame.route}/{nickname}/{opponent}",
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType },
                navArgument("opponent") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nickname = backStackEntry.arguments?.getString("nickname") ?: "Guest"
            val opponent = backStackEntry.arguments?.getString("opponent") ?: "???"
            val bluetoothVM: BluetoothViewModel = viewModel()

            // Obtenemos el estado inicial de conexión desde la VM
            val initialIsConnected = bluetoothVM.isConnected.collectAsState().value

            GyroPongGameScreen(
                bluetoothVM = bluetoothVM,
                nickname = nickname,
                initialOpponentNickname = opponent,
                initialIsConnected = initialIsConnected,
                onExit = { navController.popBackStack() }
            )
        }

        // --- PROFILE SCREEN ---
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                sessionViewModel = sessionViewModel,
                userViewModel = userViewModel
            )
        }
    }
}
*/
