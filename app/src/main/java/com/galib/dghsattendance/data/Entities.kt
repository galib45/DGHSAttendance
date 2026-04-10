package com.galib.dghsattendance.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Facility Entity
@Entity(
    tableName = "facilities",
    foreignKeys = [
        ForeignKey(
            entity = DivisionEntity::class,
            parentColumns = ["id"],
            childColumns = ["division_id"]
        ),
        ForeignKey(
            entity = DistrictEntity::class,
            parentColumns = ["id"],
            childColumns = ["district_id"]
        ),
        ForeignKey(
            entity = UpazilaEntity::class,
            parentColumns = ["id"],
            childColumns = ["upazila_id"]
        )
    ],
    indices = [ Index("type"), Index("upazila_id"), Index("district_id"), Index("division_id") ]
)
data class FacilityEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val code: String,
    val type: String,
    @ColumnInfo(name = "division_id") val divisionId: Int?,
    @ColumnInfo(name = "district_id") val districtId: Int?,
    @ColumnInfo(name = "upazila_id") val upazilaId: Int?,
)

// Division Entity
@Entity(tableName = "divisions", indices = [Index("name")])
data class DivisionEntity(
    @PrimaryKey val id: Int,
    val name: String
)

// District Entity
@Entity(
    tableName = "districts",
    foreignKeys = [
        ForeignKey(
            entity = DivisionEntity::class,
            parentColumns = ["id"],
            childColumns = ["division_id"]
        )
    ],
    indices = [ Index("name"), Index("division_id") ]
)
data class DistrictEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "division_id") val divisionId: Int?
)

// Upazila Entity
@Entity(
    tableName = "upazilas",
    foreignKeys = [
        ForeignKey(
            entity = DistrictEntity::class,
            parentColumns = ["id"],
            childColumns = ["district_id"]
        )
    ],
    indices = [ Index("name"), Index("district_id") ]
)
data class UpazilaEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "district_id") val districtId: Int?
)
