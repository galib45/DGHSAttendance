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
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.galib.dghsattendance.R
import com.galib.dghsattendance.domain.SnackbarEvent
import com.galib.dghsattendance.domain.SnackbarManager
import com.galib.dghsattendance.data.ApiResult
import com.galib.dghsattendance.data.AttendanceApi
import com.galib.dghsattendance.data.ReportParser
import com.galib.dghsattendance.ui.theme.AppTypography
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun IndividualSearchPage(backStack: NavBackStack<NavKey>) {
    var hrisId by remember { mutableStateOf("") }
    var dateRange by remember { mutableStateOf("") }
    var hrisIdError by remember { mutableStateOf("") }
    var dateRangeError by remember { mutableStateOf("") }
    val dateRangePickerState = rememberDateRangePickerState()
    var isDatePickerDialogOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val format = "dd/MM/yyyy"

    fun Long?.formattedText(pattern: String) : String {
        return Instant
            .ofEpochMilli(this ?: System.currentTimeMillis())
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(pattern))
    }

    fun DateRangePickerState.dateRangeString() : String {
        val startDateText = this.selectedStartDateMillis.formattedText(format)
        val endDateText = this.selectedEndDateMillis.formattedText(format)
        return "$startDateText to $endDateText"
    }

    fun validate(): Boolean {
        hrisIdError = when {
            hrisId.isBlank() -> "Required"
            hrisId.toIntOrNull() == null -> "Not a valid HRIS Id"
            else -> ""
        }
        dateRangeError = if (dateRange.isBlank()) "Required" else ""

        return hrisIdError.isEmpty() && dateRangeError.isEmpty()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Search for \nIndividual Report",
                style = AppTypography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                OutlinedTextField(
                    value = hrisId,
                    onValueChange = {
                        hrisId = it
                        if (it.isNotBlank()) hrisIdError = ""
                    },
                    label = { Text("HRIS Id") },
                    isError = hrisIdError.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                if (hrisIdError.isNotEmpty()) {
                    Text(
                        hrisIdError,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column {
                OutlinedTextField(
                    value = dateRange,
                    onValueChange = {
                        dateRange = it
                    },
                    label = { Text("Date Range") },
                    isError = dateRangeError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { isDatePickerDialogOpen = true }) {
                            Icon(
                                painterResource(R.drawable.round_date_range_24),
                                contentDescription = "Select a date range"
                            )
                        }
                    }
                )
                if (dateRangeError.isNotEmpty()) {
                    Text(
                        dateRangeError,
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
                        val fromDate = dateRangePickerState.selectedStartDateMillis.formattedText(format)
                        val toDate = dateRangePickerState.selectedEndDateMillis.formattedText(format)
                        AttendanceApi.searchIndividual(hrisId, fromDate, toDate) { result ->
                            isLoading = false
                            when(result) {
                                is ApiResult.Error -> {
                                    scope.launch {
                                        SnackbarManager.sendEvent(SnackbarEvent("Something went wrong"))
                                    }
                                }
                                is ApiResult.Redirect -> {
                                    if (result.location == "${AttendanceApi.BASE_URL}/login") {
                                        scope.launch {
                                            SnackbarManager.sendEvent(SnackbarEvent("Session expired. Please log in"))
                                        }
                                        backStack.removeLastOrNull()
                                        backStack.add(RouteLogin)
                                    }
                                }
                                is ApiResult.Success -> {
                                    val html = result.data
                                    val report = ReportParser.parseIndividualReport(html)
                                    backStack.add(RouteIndividualReport(report))
                                }
                            }
                        }
                    }
                }
            ) {
                if (isLoading) CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                ) else Text("SEARCH")
            }
            if (isDatePickerDialogOpen) {
                DatePickerDialog(
                    onDismissRequest = { isDatePickerDialogOpen = false },
                    confirmButton = {
                        TextButton(onClick = {
                            isDatePickerDialogOpen = false
                            dateRange = dateRangePickerState.dateRangeString()
                            dateRangeError = ""
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {isDatePickerDialogOpen = false}) { Text("CANCEL") }
                    }
                ) {
                    DateRangePicker(dateRangePickerState)
                }
            }
        }
    }
}