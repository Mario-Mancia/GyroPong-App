package com.example.gyropong.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Session : Screen("session")
    object QuickMatchSetup : Screen("quick_match_setup")

    // Pantallas internas
    object FindMatch: Screen("find_match")
    object Game : Screen("game")
    object Profile : Screen("profile")
}