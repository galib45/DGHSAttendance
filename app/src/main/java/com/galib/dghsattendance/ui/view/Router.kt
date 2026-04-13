package com.galib.dghsattendance.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.galib.dghsattendance.App
import com.galib.dghsattendance.data.ApiResult
import com.galib.dghsattendance.data.AttendanceApi
import com.galib.dghsattendance.data.FacilityReport
import com.galib.dghsattendance.data.IndividualReport
import com.galib.dghsattendance.domain.SnackbarEvent
import com.galib.dghsattendance.domain.SnackbarManager
import com.galib.dghsattendance.ui.theme.AppTheme
import com.galib.dghsattendance.ui.viewmodel.FacilitySearchViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun Router() {
    val backStack = rememberNavBackStack(RouteSplash)
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val app = LocalContext.current.applicationContext as App

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            SnackbarManager.events.collect { event ->
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = event.duration,
                        withDismissAction = event.onDismiss != null
                    )

                    when (result) {
                        SnackbarResult.Dismissed -> event.onAction?.invoke()
                        SnackbarResult.ActionPerformed -> event.onDismiss?.invoke()
                    }
                }
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        if (backStack.last() != RouteSplash) backStack.add(RouteSplash)
        AttendanceApi.checkIfLoggedIn { result ->
            when (result) {
                is ApiResult.Success -> {
                    if (backStack.size == 1) backStack[0] = RouteHome
                    else backStack.removeLastOrNull()
                }

                is ApiResult.Error -> {
                    scope.launch {
                        SnackbarManager.sendEvent(
                            SnackbarEvent(
                                result.exception.message ?: "",
                                duration = SnackbarDuration.Long
                            )
                        )
                    }
                    backStack.clear()
                    backStack.add(RouteLogin)
                }

                is ApiResult.Redirect -> {
                    backStack.clear()
                    backStack.add(RouteLogin)
                }
            }
        }
        onPauseOrDispose {  }
    }

    AppTheme(dynamicColor = false) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.padding(innerPadding),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<RouteSplash> {
                        SplashPage(backStack)
                    }
                    entry<RouteHome> { HomePage(backStack) }
                    entry<RouteLogin> { LoginPage(backStack) }
                    entry<RouteIndividualSearch> { IndividualSearchPage(backStack) }
                    entry<RouteIndividualReport> { data ->
                        IndividualReportPage(backStack, data.report)
                    }
                    entry<RouteFacilitySearch> {
                        FacilitySearchPage(
                            backStack,
                            viewModel(factory = FacilitySearchViewModel.Factory(app.facilityRepository))
                        )
                    }
                    entry<RouteFacilityReport> { data ->
                        FacilityReportPage(backStack, data.report)
                    }
                },
            )
        }
    }
}

@Serializable
data object RouteSplash : NavKey

@Serializable
data object RouteHome : NavKey

@Serializable
data object RouteLogin : NavKey

@Serializable
data object RouteIndividualSearch : NavKey

@Serializable
data class RouteIndividualReport(
    val report: IndividualReport
) : NavKey

@Serializable
data object RouteFacilitySearch : NavKey

@Serializable
data class RouteFacilityReport(
    val report: FacilityReport
) : NavKey