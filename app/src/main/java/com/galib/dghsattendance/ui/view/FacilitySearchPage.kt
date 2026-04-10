package com.galib.dghsattendance.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.galib.dghsattendance.R
import com.galib.dghsattendance.data.ApiResult
import com.galib.dghsattendance.data.AttendanceApi
import com.galib.dghsattendance.data.FacilityEntity
import com.galib.dghsattendance.data.ReportParser
import com.galib.dghsattendance.domain.SnackbarEvent
import com.galib.dghsattendance.domain.SnackbarManager
import com.galib.dghsattendance.ui.viewmodel.FacilitySearchViewModel
import kotlinx.coroutines.launch

@Composable
fun FacilitySearchPage(backStack: NavBackStack<NavKey>, viewModel: FacilitySearchViewModel) {
    val divisions by viewModel.divisions.collectAsStateWithLifecycle()
    val districts by viewModel.districts.collectAsStateWithLifecycle()
    val upazilas  by viewModel.upazilas.collectAsStateWithLifecycle()
    val facilities by viewModel.facilities.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val selectedDivision by viewModel.selectedDivision.collectAsStateWithLifecycle()
    val selectedDistrict by viewModel.selectedDistrict.collectAsStateWithLifecycle()
    val selectedUpazila  by viewModel.selectedUpazila.collectAsStateWithLifecycle()

    var nameQuery by remember { mutableStateOf("") }

    val filteredFacilities = remember(facilities, nameQuery) {
        if (nameQuery.isBlank()) facilities
        else facilities.filter { it.name.contains(nameQuery.trim(), ignoreCase = true) }
    }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FacilityDropdown(
                label     = "Division",
                items     = divisions,
                selected  = selectedDivision,
                itemLabel = { it.name },
                onSelect  = viewModel::selectDivision
            )
            FacilityDropdown(
                label     = "District",
                items     = districts,
                selected  = selectedDistrict,
                itemLabel = { it.name },
                onSelect  = viewModel::selectDistrict,
                onClear   = { viewModel.selectDistrict(null) },
                enabled   = selectedDivision != null
            )
            FacilityDropdown(
                label     = "Upazila",
                items     = upazilas,
                selected  = selectedUpazila,
                itemLabel = { it.name },
                onSelect  = viewModel::selectUpazila,
                onClear   = { viewModel.selectUpazila(null) },
                enabled   = selectedDistrict != null
            )

            HorizontalDivider()

            OutlinedTextField(
                value         = nameQuery,
                onValueChange = { nameQuery = it },
                placeholder   = { Text("Search by name") },
                leadingIcon   = { Icon(painterResource(R.drawable.round_search_24), contentDescription = null) },
                trailingIcon  = {
                    if (nameQuery.isNotEmpty()) {
                        IconButton(onClick = { nameQuery = "" }) {
                            Icon(painterResource(R.drawable.round_clear_24), contentDescription = "Clear search")
                        }
                    }
                },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            Text(
                text  = "${filteredFacilities.size} facilities",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredFacilities, key = { it.id }) { facility ->
                    FacilityCard(facility, onClick = {
                        viewModel.startLoading()
                        AttendanceApi.searchFacility(facility.code) { result ->
                            viewModel.finishLoading()
                            when (result) {
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
                                    val report = ReportParser.parseFacilityReport(html)
                                    backStack.add(RouteFacilityReport(report))
                                }
                            }
                        }
                    })
                }
            }
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},  // consume touches while loading
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun FacilityCard(facility: FacilityEntity, onClick: (FacilityEntity) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(facility) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(facility.name, style = MaterialTheme.typography.titleSmall)
            Text(
                facility.type, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Code: ${facility.code}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FacilityDropdown(
    label: String,
    items: List<T>,
    selected: T?,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    onClear: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded         = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value         = selected?.let(itemLabel) ?: "Select $label",
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            enabled       = enabled,
            trailingIcon  = {
                if (selected != null && onClear != null) {
                    IconButton(onClick = {
                        onClear()
                        expanded = false
                    }) {
                        Icon(painterResource(R.drawable.round_clear_24), contentDescription = "Clear $label")
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            },
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text    = { Text(itemLabel(item)) },
                    onClick = { onSelect(item); expanded = false }
                )
            }
        }
    }
}