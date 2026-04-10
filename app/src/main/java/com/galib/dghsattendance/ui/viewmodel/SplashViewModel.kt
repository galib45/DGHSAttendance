package com.galib.dghsattendance.ui.viewmodel

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.galib.dghsattendance.data.ApiResult
import com.galib.dghsattendance.data.AttendanceApi
import com.galib.dghsattendance.domain.SnackbarEvent
import com.galib.dghsattendance.domain.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            AttendanceApi.checkIfLoggedIn { result ->
                when(result) {
                    is ApiResult.Success -> { _isLoggedIn.value = true }
                    is ApiResult.Error -> {
                        _isLoggedIn.value = false
                        viewModelScope.launch {
                            SnackbarManager.sendEvent(
                                SnackbarEvent(
                                    result.exception.message ?: "",
                                    duration = SnackbarDuration.Long
                                )
                            )
                        }
                    }
                    is ApiResult.Redirect  -> { _isLoggedIn.value = false }
                }
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel() as T
        }
    }
}