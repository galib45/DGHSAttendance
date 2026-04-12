package com.galib.dghsattendance.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.galib.dghsattendance.R
import com.galib.dghsattendance.data.AttendanceApi

@Composable
fun HomePage(backStack: NavBackStack<NavKey>) {
    Scaffold(
        floatingActionButton = {
            FilledIconButton(onClick = {
                AttendanceApi.logout()
                backStack.removeLastOrNull()
                backStack.add(RouteSplash)
            }) {
                Icon(painterResource(R.drawable.round_logout_24), contentDescription = "Logout")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElevatedButton(onClick = { backStack.add(RouteFacilitySearch) }) { Text("Daily Summary") }
                ElevatedButton(onClick = { backStack.add(RouteIndividualSearch) }) { Text("Individual Report") }
            }
        }
    }
}