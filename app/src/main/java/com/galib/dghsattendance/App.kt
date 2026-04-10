package com.galib.dghsattendance

import android.app.Application
import com.galib.dghsattendance.data.AttendanceApi
import com.galib.dghsattendance.data.FacilityDatabase
import com.galib.dghsattendance.data.FacilityRepository

class App : Application() {
    val facilityDatabase by lazy { FacilityDatabase.getInstance(this) }
    val facilityRepository by lazy { FacilityRepository(facilityDatabase) }

    override fun onCreate() {
        super.onCreate()
        AttendanceApi.init(this)
    }
}