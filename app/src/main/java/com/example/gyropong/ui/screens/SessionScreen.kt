package com.example.gyropong.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.gyropong.domain.models.User
import com.example.gyropong.ui.viewmodels.SessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.gyropong.ui.viewmodels.UserViewModel

@Composable
fun SessionScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    sessionViewModel: SessionViewModel,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }

    // --- Login fields ---
    var loginEmail by remember { mutableStateOf(TextFieldValue("")) }
    var loginPassword by remember { mutableStateOf(TextFieldValue("")) }
    var loginError by remember { mutableStateOf("") }

    // --- Register fields ---
    var regEmail by remember { mutableStateOf(TextFieldValue("")) }
    var regUsername by remember { mutableStateOf(TextFieldValue("")) }
    var regPassword by remember { mutableStateOf(TextFieldValue("")) }
    var regConfirmPassword by remember { mutableStateOf(TextFieldValue("")) }
    var regAcceptedTerms by remember { mutableStateOf(false) }
    var regBirthdate by remember { mutableStateOf<Long?>(null) }
    var regError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val calendar = Calendar.getInstance()
    val birthdateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // --- Detectar login exitoso y crear sesión ---
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // 1️⃣ Iniciar sesión en DB
            sessionViewModel.startSession(user.id)

            // 2️⃣ Callback de login exitoso
            onLoginSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botón de retroceder
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch Login / Registro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { isLogin = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLogin) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
            ) { Text("Login") }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { isLogin = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isLogin) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
            ) { Text("Registro") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLogin) {
            // --- LOGIN FORM ---
            OutlinedTextField(
                value = loginEmail,
                onValueChange = { loginEmail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = loginPassword,
                onValueChange = { loginPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            if (loginError.isNotEmpty()) {
                Text(
                    text = loginError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isLoginEnabled = loginEmail.text.isNotBlank() && loginPassword.text.isNotBlank()

            Button(
                onClick = {
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail.text).matches()) {
                        loginError = "Email inválido"
                        return@Button
                    }
                    loginError = ""
                    userViewModel.login(loginEmail.text, loginPassword.text)

                    // Toast si login fallido
                    scope.launch {
                        delay(200) // esperar que currentUser se actualice
                        if (userViewModel.currentUser.value == null) {
                            Toast.makeText(context, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = isLoginEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }

        } else {
            // --- REGISTER FORM ---
            OutlinedTextField(
                value = regUsername,
                onValueChange = { regUsername = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            OutlinedTextField(
                value = regConfirmPassword,
                onValueChange = { regConfirmPassword = it },
                label = { Text("Confirmar Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            // --- Birthdate picker ---
            OutlinedTextField(
                value = regBirthdate?.let { birthdateFormatter.format(Date(it)) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de nacimiento") },
                trailingIcon = {
                    IconButton(onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                                val selectedTime = calendar.timeInMillis
                                if (selectedTime > System.currentTimeMillis()) {
                                    Toast.makeText(context, "La fecha no puede ser futura", Toast.LENGTH_SHORT).show()
                                } else {
                                    regBirthdate = selectedTime
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = regAcceptedTerms,
                    onCheckedChange = { regAcceptedTerms = it }
                )
                Text("Acepto los términos")
            }

            if (regError.isNotEmpty()) {
                Text(
                    text = regError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isRegisterEnabled = regUsername.text.isNotBlank() &&
                    regEmail.text.isNotBlank() &&
                    regPassword.text.isNotBlank() &&
                    regConfirmPassword.text.isNotBlank() &&
                    regAcceptedTerms &&
                    regBirthdate != null

            Button(
                onClick = {
                    regError = ""

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(regEmail.text).matches()) {
                        regError = "Email inválido"
                        return@Button
                    }
                    if (regPassword.text != regConfirmPassword.text) {
                        regError = "Passwords no coinciden"
                        return@Button
                    }
                    if (regBirthdate == null) {
                        regError = "Selecciona una fecha de nacimiento"
                        return@Button
                    }

                    userViewModel.isEmailRegistered(regEmail.text) { exists ->
                        if (exists) {
                            regError = "Email ya registrado"
                        } else {
                            val user = User(
                                id = 0L,
                                username = regUsername.text,
                                email = regEmail.text,
                                birthdate = regBirthdate!!,
                                createdAt = System.currentTimeMillis(),
                                userPoints = 0
                            )
                            userViewModel.registerUser(user, regPassword.text)
                            Toast.makeText(context, "Usuario registrado", Toast.LENGTH_SHORT).show()
                            isLogin = true
                        }
                    }
                },
                enabled = isRegisterEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta")
            }
        }
    }
}
