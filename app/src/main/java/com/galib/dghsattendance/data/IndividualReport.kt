package com.galib.dghsattendance.data

import kotlinx.serialization.Serializable

@Serializable
data class IndividualReport(
    val summary: IndividualReportSummary?,
    val details: List<IndividualReportEntry>
)

@Serializable
data class IndividualReportSummary(
    val name: String,
    val hrisId: String,
    val designation: String,
    val biometricImage: String,
    val hrmImageUrl: String,
    val dateRange: String,
    val totalDays: Int,
    val present: Int,
    val leave: Int,
    val weekend: Int,
    val holiday: Int,
    val absent: Int,
    val percentage: Float
)

@Serializable
data class IndividualReportEntry(
    val date: String,
    val facilityName: String,
    val inTime: String,
    val outTime: String,
    val duration: String,
    val type: String,
    val status: String
)
