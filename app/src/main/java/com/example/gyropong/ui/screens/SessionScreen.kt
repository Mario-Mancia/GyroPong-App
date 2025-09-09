package com.example.gyropong.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gyropong.domain.models.User
import com.example.gyropong.ui.components.GameButton
import com.example.gyropong.ui.viewmodels.SessionViewModel
import com.example.gyropong.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.TextFieldDefaults

@OptIn(ExperimentalAnimationApi::class)
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

    // --- Detectar login exitoso ---
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            sessionViewModel.startSession(user.id)
            onLoginSuccess()
        }
    }

    // Fondo con degradado
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF3F51B5), Color(0xFF673AB7))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(16.dp)
    ) {
        // Botón de retroceder
        IconButton(
            onClick = { onBack() }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch Login / Registro

        Box(
            modifier = Modifier
                .fillMaxWidth()           // Ocupa todo el ancho del screen
                .height(50.dp),
            contentAlignment = Alignment.Center // Centrar el switch
        ) {
            Box(
                modifier = Modifier
                    .width(320.dp)         // Ancho fijo para el switch
                    .height(50.dp)
                    .background(Color(0xFF2C0B5B), RoundedCornerShape(25.dp))
            ) {
                val transitionOffset by animateDpAsState(
                    targetValue = if (isLogin) 0.dp else 160.dp,
                    animationSpec = tween(durationMillis = 300)
                )

                // Slider dorado
                Box(
                    modifier = Modifier
                        .offset(x = transitionOffset)
                        .fillMaxHeight()
                        .width(160.dp)
                        .background(Color(0xFFFFD700), RoundedCornerShape(25.dp))
                )

                // Textos Login / Registro
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Login",
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { isLogin = true },
                        color = if (isLogin) Color.Black else Color.White, // negro sobre amarillo
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Registro",
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { isLogin = false },
                        color = if (!isLogin) Color.Black else Color.White, // negro sobre amarillo
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Formularios con AnimatedContent para slide
        AnimatedContent(
            targetState = isLogin,
            transitionSpec = {
                if (targetState) {
                    // Login entra, Registro sale
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(400)
                    ) with slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(400)
                    )
                } else {
                    // Registro entra, Login sale
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(400)
                    ) with slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(400)
                    )
                }
            }
        ) { loginVisible ->
            if (loginVisible) {
                Column {
                    SessionTextField("Email", loginEmail) { loginEmail = it }
                    SessionTextField("Password", loginPassword, isPassword = true) { loginPassword = it }

                    if (loginError.isNotEmpty()) {
                        Text(loginError, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GameButton(
                        text = "Iniciar sesión",
                        onClick = {
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail.text).matches()) {
                                loginError = "Email inválido"
                                return@GameButton
                            }
                            loginError = ""
                            userViewModel.login(loginEmail.text, loginPassword.text)
                            scope.launch {
                                delay(200)
                                if (userViewModel.currentUser.value == null) {
                                    Toast.makeText(context, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = true
                    )
                }
            } else {
                Column {
                    SessionTextField("Username", regUsername) { regUsername = it }
                    SessionTextField("Email", regEmail) { regEmail = it }
                    SessionTextField("Password", regPassword, isPassword = true) { regPassword = it }
                    SessionTextField("Confirmar Password", regConfirmPassword, isPassword = true) { regConfirmPassword = it }

                    val birthdateText = regBirthdate?.let { birthdateFormatter.format(Date(it)) } ?: ""
                    SessionTextField(
                        "Fecha de nacimiento",
                        TextFieldValue(birthdateText),
                        readOnly = true,
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
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = Color.White)
                            }
                        },
                        onValueChange = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = regAcceptedTerms,
                            onCheckedChange = { regAcceptedTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFFD700),
                                uncheckedColor = Color.White
                            )
                        )
                        Text("Acepto los términos", color = Color.White)
                    }

                    if (regError.isNotEmpty()) {
                        Text(regError, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GameButton(
                        text = "Crear cuenta",
                        onClick = {
                            regError = ""
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(regEmail.text).matches()) {
                                regError = "Email inválido"
                                return@GameButton
                            }
                            if (regPassword.text != regConfirmPassword.text) {
                                regError = "Passwords no coinciden"
                                return@GameButton
                            }
                            if (regBirthdate == null) {
                                regError = "Selecciona una fecha de nacimiento"
                                return@GameButton
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
                        enabled = true
                    )
                }
            }
        }
    }
}

@Composable
fun SessionTextField(
    label: String,
    value: TextFieldValue,
    isPassword: Boolean = false,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (TextFieldValue) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp)),
        singleLine = true,
        readOnly = readOnly,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White,
            errorTextColor = Color.Red,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            cursorColor = Color.Yellow,
            errorCursorColor = Color.Red,
            focusedBorderColor = Color(0xFFFFD700),
            unfocusedBorderColor = Color(0x99FFFFFF),
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Red,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White,
            disabledLabelColor = Color.White,
            errorLabelColor = Color.Red
        )
    )
}



/*
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
*/