package com.galib.dghsattendance.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DivisionDao {
    @Query("SELECT * FROM divisions ORDER by name")
    fun getAll() : Flow<List<DivisionEntity>>
}

@Dao
interface DistrictDao {
    @Query("SELECT * FROM districts WHERE division_id = :divisionId ORDER by name")
    fun getByDivision(divisionId: Int) : Flow<List<DistrictEntity>>
}

@Dao
interface UpazilaDao {
    @Query("SELECT * FROM upazilas WHERE district_id = :districtId ORDER by name")
    fun getByDistrict(districtId: Int) : Flow<List<UpazilaEntity>>
}

@Dao
interface FacilityDao {
    @Query("SELECT * from facilities WHERE upazila_id = :upazilaId ORDER by name")
    fun getByUpazila(upazilaId: Int) : Flow<List<FacilityEntity>>

    @Query("SELECT * from facilities WHERE district_id = :districtId ORDER by name")
    fun getByDistrict(districtId: Int) : Flow<List<FacilityEntity>>

    @Query("SELECT * from facilities WHERE division_id = :divisionId ORDER by name")
    fun getByDivision(divisionId: Int) : Flow<List<FacilityEntity>>
}