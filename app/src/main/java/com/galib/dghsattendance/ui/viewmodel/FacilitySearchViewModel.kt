package com.galib.dghsattendance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.galib.dghsattendance.data.DistrictEntity
import com.galib.dghsattendance.data.DivisionEntity
import com.galib.dghsattendance.data.FacilityEntity
import com.galib.dghsattendance.data.FacilityRepository
import com.galib.dghsattendance.data.UpazilaEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class FacilitySearchViewModel(private val repo: FacilityRepository) : ViewModel() {
    private val _selectedDivision = MutableStateFlow<DivisionEntity?>(null)
    private val _selectedDistrict = MutableStateFlow<DistrictEntity?>(null)
    private val _selectedUpazila  = MutableStateFlow<UpazilaEntity?>(null)
    private val _isLoading = MutableStateFlow<Boolean>(false)

    val selectedDivision = _selectedDivision.asStateFlow()
    val selectedDistrict = _selectedDistrict.asStateFlow()
    val selectedUpazila  = _selectedUpazila.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    val divisions: StateFlow<List<DivisionEntity>> =
        repo.getDivisions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val districts: StateFlow<List<DistrictEntity>> =
        _selectedDivision
            .flatMapLatest { div ->
                div?.let { repo.getDistricts(it.id) } ?: flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val upazilas: StateFlow<List<UpazilaEntity>> =
        _selectedDistrict
            .flatMapLatest { dist ->
                dist?.let { repo.getUpazilas(it.id) } ?: flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // combines all three selection states to pick the narrowest query
    @OptIn(ExperimentalCoroutinesApi::class)
    val facilities: StateFlow<List<FacilityEntity>> =
        combine(_selectedDivision, _selectedDistrict, _selectedUpazila) {
                div, dist, up -> Triple(div, dist, up)
        }
            .flatMapLatest { (div, dist, up) ->
                repo.getFacilities(
                    divisionId = div?.id,
                    districtId = dist?.id,
                    upazilaId  = up?.id
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDivision(division: DivisionEntity) {
        _selectedDivision.value = division
        _selectedDistrict.value = null
        _selectedUpazila.value  = null
    }

    fun selectDistrict(district: DistrictEntity?) {
        _selectedDistrict.value = district
        _selectedUpazila.value  = null
    }

    fun selectUpazila(upazila: UpazilaEntity?) {
        _selectedUpazila.value = upazila
    }

    fun startLoading() { _isLoading.value = true }
    fun finishLoading() { _isLoading.value = false }

    class Factory(private val repo: FacilityRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FacilitySearchViewModel(repo) as T
        }
    }
}