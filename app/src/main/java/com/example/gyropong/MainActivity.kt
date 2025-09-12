// El main activity de la aplicación, encargada del arranque general.
package com.example.gyropong

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gyropong.data.database.AppDatabase
import com.example.gyropong.data.repository.SessionRepository
import com.example.gyropong.data.repository.UserRepository
import com.example.gyropong.ui.navigation.AppNavHost
import com.example.gyropong.ui.navigation.Screen
import com.example.gyropong.ui.theme.GyroPongTheme
import com.example.gyropong.ui.viewmodels.SessionViewModel
import com.example.gyropong.ui.viewmodels.SessionViewModelFactory
import com.example.gyropong.ui.viewmodels.UserViewModel
import com.example.gyropong.ui.viewmodels.UserViewModelFactory
import com.example.gyropong.hardware.vibration.VibrationManager

class MainActivity : ComponentActivity() {

    // Lista de permisos necesarios con validación Android 12+ o Android <= 11
    private val permissions: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.VIBRATE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.VIBRATE
            )
        }
    }

    //Notifica sobre la necesidad de asignar los permisos.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (!allGranted) {
                Toast.makeText(this, "Se requieren permisos para continuar", Toast.LENGTH_LONG).show()
            }
        }

    private lateinit var userRepository: UserRepository
    lateinit var userViewModel: UserViewModel
        private set
    private lateinit var sessionRepository: SessionRepository
    private lateinit var sessionViewModel: SessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de la base de datos y los VM
        val db = AppDatabase.getInstance(applicationContext)
        userRepository = UserRepository(db.userDao())
        sessionRepository = SessionRepository(db.sessionDao())

        // Inicialización de los ViewModel principales con sus repositorios.
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(userRepository)
        )[UserViewModel::class.java]

        sessionViewModel = ViewModelProvider(
            this,
            SessionViewModelFactory(sessionRepository)
        )[SessionViewModel::class.java]

        checkAndRequestPermissions()
        enableEdgeToEdge()

        setContent {
            GyroPongTheme {
                GyroPongApp(
                    userViewModel = userViewModel,
                    sessionViewModel = sessionViewModel
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }
}

// Composable de manejo de navegación de la aplicación.
@Composable
fun GyroPongApp(
    userViewModel: UserViewModel,
    sessionViewModel: SessionViewModel) {
    val navController = rememberNavController()
    var backPressedTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current

    val vibrationManager = remember { VibrationManager(context) }

    // Handler de retroceso para evitar regresar a pantallas no permitidas.
    BackHandler(enabled = true) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
        val currentTime = System.currentTimeMillis()


        val isInternalScreen = currentRoute.startsWith(Screen.FindMatch.route) ||
                currentRoute.startsWith(Screen.GyroPongGame.route) ||
                currentRoute.startsWith(Screen.Profile.route)

        when {
            currentRoute == Screen.Home.route -> {
                if (currentTime - backPressedTime < 2000) {
                    (context as? ComponentActivity)?.finish()
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(context, "Pulsa otra vez para salir", Toast.LENGTH_SHORT).show()
                    vibrationManager.vibrateSoft()
                }
            }

            currentRoute == Screen.Session.route || currentRoute == Screen.QuickMatchSetup.route -> {
                navController.popBackStack()
            }

            isInternalScreen -> {
                // Doble back para salir
                if (currentTime - backPressedTime < 2000) {
                    (context as? ComponentActivity)?.finish()
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(context, "Pulsa otra vez para salir", Toast.LENGTH_SHORT).show()
                    vibrationManager.vibrateSoft()
                }
            }

            else -> {
                navController.popBackStack()
            }
        }
    }

    // Inicialización del AppNavHost para evitar hacerlo en SetContent
    AppNavHost(navController = navController, userViewModel = userViewModel, sessionViewModel = sessionViewModel)
}
