package com.example.gyropong.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
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
import com.example.gyropong.hardware.sensors.accelerometer.AccelerometerManager
import com.example.gyropong.hardware.sensors.gyroscope.GyroscopeManager
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
                        val avatarRes = R.drawable.avatar_frog
                        if (user != null) {
                            navController.navigate("${Screen.FindMatch.route}/${user.username}/$avatarRes") {
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
            route = "${Screen.FindMatch.route}/{nickname}/{avatarRes}",
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType },
                navArgument("avatarRes") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val nickname = backStackEntry.arguments?.getString("nickname") ?: "Guest"
            val avatarRes = backStackEntry.arguments?.getInt("avatarRes") ?: R.drawable.avatar_frog
            val currentUser by userViewModel.currentUser.collectAsState()

            FindMatchScreen(
                bluetoothVM = bluetoothVM,
                nickname = nickname,
                avatar = avatarRes,
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

            // Esto ya está dentro de un Composable, así que LocalContext.current funciona
            val context = LocalContext.current
            val gyroManager = remember { GyroscopeManager(context) }

            val nickname = backStackEntry.arguments?.getString("nickname") ?: "Guest"
            val opponent = backStackEntry.arguments?.getString("opponent") ?: "???"

            GyroPongGameScreen(
                bluetoothVM = bluetoothVM,
                gyroscopeManager = gyroManager,
                nickname = nickname,
                initialOpponentNickname = opponent,
                onExit = { navController.popBackStack() }
            )
        }

        // --- GAME SCREEN ---
        composable(
            route = "${Screen.RpsGame.route}/{nickname}/{avatarRes}/{opponent}/{opponentAvatar}",
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType },
                navArgument("avatarRes") { type = NavType.IntType },
                navArgument("opponent") { type = NavType.StringType },
                navArgument("opponentAvatar") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val accelerometerManager = remember { AccelerometerManager(context) }

            val nickname = backStackEntry.arguments?.getString("nickname") ?: "Guest"
            val avatar = backStackEntry.arguments?.getInt("avatarRes") ?: R.drawable.avatar_frog
            val opponent = backStackEntry.arguments?.getString("opponent") ?: "???"
            val opponentAvatar = backStackEntry.arguments?.getInt("opponentAvatar") ?: R.drawable.avatar_lion

            RpsGameScreen(
                bluetoothVM = bluetoothVM,
                accelerometerManager = accelerometerManager,
                userViewModel = userViewModel,
                nickname = nickname,
                avatar = avatar,
                opponentNickname = opponent,
                opponentAvatar = opponentAvatar,
                onExit = {
                    Log.d("RpsGameScreen", "Salir presionado, desconectando Bluetooth")
                    bluetoothVM.disconnect()
                    bluetoothVM.stopDiscovery()
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true } // limpia todo el backstack
                        launchSingleTop = true
                    }
                }
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
