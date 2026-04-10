package com.galib.dghsattendance.data

import kotlinx.serialization.Serializable

@Serializable
data class FacilityReport (
    val summary: FacilityReportSummary?,
    val details: List<FacilityReportEntry>
)

@Serializable
data class FacilityReportSummary (
    val name: String,
    val code: String,
    val division: String,
    val district: String,
    val upazila: String,
    val reportDate: String,
    val totalStaffs: String,
    val present: String,
    val leave: String,
    val absent: String
)

@Serializable
data class FacilityReportEntry (
    val name: String,
    val hrisId: String,
    val designation: String,
    val inTime: String,
    val outTime: String,
    val duration: String,
    val type: String,
    val status: String
)
