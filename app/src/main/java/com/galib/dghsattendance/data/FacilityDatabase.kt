package com.galib.dghsattendance.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DivisionEntity::class,
        DistrictEntity::class,
        UpazilaEntity::class,
        FacilityEntity::class
    ],
    version = 1
)
abstract class FacilityDatabase : RoomDatabase() {
    abstract fun divisionDao(): DivisionDao
    abstract fun districtDao(): DistrictDao
    abstract fun upazilaDao(): UpazilaDao
    abstract fun facilityDao(): FacilityDao

    companion object {
        @Volatile private var INSTANCE: FacilityDatabase? = null

        fun getInstance(context: Context): FacilityDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FacilityDatabase::class.java,
                    "dghs-public-facilities.db"
                )
                .createFromAsset("dghs-public-facilities.db")
                .build()
                .also { INSTANCE = it }
            }
    }
}