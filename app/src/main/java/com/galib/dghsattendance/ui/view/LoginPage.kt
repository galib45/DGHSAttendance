package com.galib.dghsattendance.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.galib.dghsattendance.R
import com.galib.dghsattendance.data.ApiResult
import com.galib.dghsattendance.data.AttendanceApi
import com.galib.dghsattendance.domain.SnackbarEvent
import com.galib.dghsattendance.domain.SnackbarManager
import com.galib.dghsattendance.ui.theme.AppTypography
import kotlinx.coroutines.launch

@Composable
fun LoginPage(backStack: NavBackStack<NavKey>) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun validate(): Boolean {
        emailError = if (email.isBlank()) "Required" else ""
        passwordError = if (password.isBlank()) "Required" else ""

        return emailError.isEmpty() && passwordError.isEmpty()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Biometric \nAttendance System",
                style = AppTypography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (it.isNotBlank()) emailError = ""
                    },
                    label = { Text("HRM Email") },
                    isError = emailError.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )
                if (emailError.isNotEmpty()) {
                    Text(
                        emailError,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (it.isNotBlank()) passwordError = ""
                    },
                    label = { Text("HRM Password") },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = passwordVisible.not() }
                        ) {
                            Icon(
                                painterResource(
                                    if (passwordVisible) R.drawable.round_visibility_off_24 else R.drawable.round_visibility_24
                                ), contentDescription = "Show password"
                            )
                        }
                    },
                    isError = passwordError.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )
                if (passwordError.isNotEmpty()) {
                    Text(
                        passwordError,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (validate()) {
                        isLoading = true
                        AttendanceApi.login(email, password) { result ->
                            isLoading = false
                            when(result) {
                                is ApiResult.Error -> {}
                                is ApiResult.Success -> {
                                    scope.launch {
                                        SnackbarManager.sendEvent(SnackbarEvent("Could not log in"))
                                    }
                                }
                                is ApiResult.Redirect -> {
                                    if (result.location == "${AttendanceApi.BASE_URL}/login") {
                                        scope.launch {
                                            SnackbarManager.sendEvent(SnackbarEvent("Could not log in"))
                                        }
                                    } else {
                                        scope.launch {
                                            SnackbarManager.sendEvent(SnackbarEvent("Successfully logged in"))
                                        }
                                        backStack.removeLastOrNull()
                                        backStack.add(RouteHome)
                                    }
                                }
                            }
                        }
                    }
                }
            ) {
                if (isLoading) CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                ) else Text("LOGIN")
            }
        }
    }
}