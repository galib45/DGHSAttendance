package com.galib.dghsattendance.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.collections.emptyList

class FacilityRepository(private val db: FacilityDatabase) {
    fun getDivisions(): Flow<List<DivisionEntity>> =
        db.divisionDao().getAll()

    fun getDistricts(divisionId: Int): Flow<List<DistrictEntity>> =
        db.districtDao().getByDivision(divisionId)

    fun getUpazilas(districtId: Int): Flow<List<UpazilaEntity>> =
        db.upazilaDao().getByDistrict(districtId)

    // narrows as user drills down
    fun getFacilities(
        divisionId: Int? = null,
        districtId: Int? = null,
        upazilaId: Int?  = null
    ): Flow<List<FacilityEntity>> = when {
        upazilaId  != null -> db.facilityDao().getByUpazila(upazilaId)
        districtId != null -> db.facilityDao().getByDistrict(districtId)
        divisionId != null -> db.facilityDao().getByDivision(divisionId)
        else               -> flowOf(emptyList())
    }
}