// Definición de rutas y objetos de pantalla para manejarlos en el AppNavHost
package com.example.gyropong.ui.navigation

sealed class Screen(val route: String) {
    // Pantallas de acceso público.
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Session : Screen("session")
    object QuickMatchSetup : Screen("quick_match_setup")

    // Pantallas internas.
    object FindMatch: Screen("find_match")
    object GyroPongGame : Screen("game")
    object Profile : Screen("profile")

    object RpsGame : Screen("game_rps")
}