package com.galib.dghsattendance.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.galib.dghsattendance.ui.viewmodel.SplashViewModel

@Composable
fun SplashPage(backStack: NavBackStack<NavKey>, viewModel: SplashViewModel = viewModel()) {
    val isLoggedIn = viewModel.isLoggedIn.collectAsState().value
    when(isLoggedIn) {
        true -> {
            backStack.removeLastOrNull()
            backStack.add(RouteHome)
        }
        false -> {
            backStack.removeLastOrNull()
            backStack.add(RouteLogin)
        }
        null -> Box (
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text("Verifying session")
            }
        }
    }
}