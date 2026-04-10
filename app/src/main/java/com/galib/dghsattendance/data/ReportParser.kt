package com.galib.dghsattendance.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object ReportParser {
    private fun String.parseFirstIntOrNull() : Int? {
        return Regex("\\d+").find(this)?.value?.toIntOrNull()
    }

    private fun parseIndividualReportSummary(table: Element) : IndividualReportSummary {
        val bolds = table.select("b")
        val name = bolds[0].text()
        val hrisId = bolds[1].text()
        val designation = bolds[2].text()
        val totalDays = bolds[3].text().toIntOrNull() ?: 0
        val dateRange = "${bolds[4].text()} to ${bolds[5].text()}"

        val images = table.select("h3 > div img")
        val biometricImage = images[0].attr("src")
        val hrmImageUrl = images[1].attr("src")

        val paragraphs = table.select("td > div > p")
        val present = paragraphs[0].text().parseFirstIntOrNull() ?: 0
        val leave = paragraphs[1].text().parseFirstIntOrNull() ?: 0
        val weekend = paragraphs[2].text().parseFirstIntOrNull() ?: 0
        val holiday = paragraphs[3].text().parseFirstIntOrNull() ?: 0
        val absent = paragraphs[4].text().parseFirstIntOrNull() ?: 0

        val percentage = table.select("td > div > div")
            .first()?.text()?.trimEnd('%')?.toFloatOrNull()
            ?: 0f

        return IndividualReportSummary(
            name,
            hrisId,
            designation,
            biometricImage,
            hrmImageUrl,
            dateRange,
            totalDays,
            present,
            leave,
            weekend,
            holiday,
            absent,
            percentage
        )
    }

    private fun parseIndividualReportDetails(table: Element) : List<IndividualReportEntry> {
        val details = mutableListOf<IndividualReportEntry>()
        val rows = table.select("tbody > tr")
        for (row in rows) {
            val cells = row.select("td")
            val date = cells[1].text()
            val facilityName = cells[2].text()
            val inTime = cells[3].text()
            val outTime = cells[4].text()
            val duration = cells[5].text()
            val type = cells[6].text()
            val status = cells[7].text()

            details.add(IndividualReportEntry(
                date, facilityName, inTime, outTime, duration, type, status
            ))
        }
        return details
    }
    
    fun parseIndividualReport(html: String) : IndividualReport {
        val document = Jsoup.parse(html)
        val tables = document.select("div#ptable3 table")
        val summaryTable = tables.first()
        val detailsTable = tables.last()
        val summary = summaryTable?.let {
            parseIndividualReportSummary(summaryTable)
        }
        val details = detailsTable?.let {
            parseIndividualReportDetails(detailsTable)
        } ?: emptyList()

        return IndividualReport(summary, details)
    }
    
    fun parseFacilityReport(html: String) : FacilityReport {
        val document = Jsoup.parse(html)
        val tables = document.select("div#ptable3 table")
        val summaryTable = tables.first()
        val detailsTable = tables.last()
        val summary = summaryTable?.let {
            parseFacilityReportSummary(summaryTable)
        }
        val details = detailsTable?.let {
            parseFacilityReportDetails(detailsTable)
        } ?: emptyList()

        return FacilityReport(summary, details)
    }

    private val LOCATION_REGEX = Regex(
        """Division\s*:\s*(.*?)\s*\|\s*District\s*:\s*(.*?)\s*\|\s*Upazila\s*:\s*(.*)"""
    )
    private val FACILITY_REGEX = Regex("""Organization\s*:\s*(.*?),\s*(\d+)""")
    private val STATS_REGEX = Regex(
        """Total\s*Staff\s*:\s*(\d+)\s*\|\s*Present\s*:\s*(\d+)\s*\|\s*Leave\s*:\s*(\d+)\s*\|\s*Absent\s*:\s*(\d+)"""
    )

    private fun parseFacilityReportSummary(table: Element) : FacilityReportSummary {
        val paragraphs = table.select("td > h3 > p")
        val locationText = paragraphs[1].text()
        val (division, district, upazila) = LOCATION_REGEX
            .find(locationText)
            ?.destructured
            ?.let { (d1, d2, d3) ->
                Triple(d1.trim(), d2.trim(), d3.trim())
            } ?: Triple("N/A", "N/A", "N/A")

        val facilityText = paragraphs[2].text()
        val (name, code) = FACILITY_REGEX
            .find(facilityText)
            ?.destructured
            ?.let { (d1, d2) ->
                Pair(d1.trim(), d2.trim())
            } ?: Pair("N/A", "N/A")

        val reportDate = paragraphs[3].text().split(" : ").last()
        val statsText = paragraphs[5].text()
        val (totalStaffs, present, leave, absent) = STATS_REGEX
            .find(statsText)
            ?.destructured
            ?.let { (d1, d2, d3, d4) ->
                listOf(d1.trim(), d2.trim(), d3.trim(), d4.trim())
            } ?: List(4) { "N/A" }

        return FacilityReportSummary(
            name,
            code,
            division,
            district,
            upazila,
            reportDate,
            totalStaffs,
            present,
            leave,
            absent
        )
    }

    private fun parseFacilityReportDetails(table: Element) : List<FacilityReportEntry> {
        val details = mutableListOf<FacilityReportEntry>()
        val rows = table.select("tbody > tr")
        for (row in rows) {
            val cells = row.select("td")
            val hrisId = cells[1].text()
            val name = cells[2].text()
            val designation = cells[3].text()
            val inTime = cells[4].text()
            val outTime = cells[5].text()
            val duration = cells[6].text()
            val type = cells[7].text()
            val status = cells[8].text()

            details.add(FacilityReportEntry(
                name, hrisId, designation, inTime, outTime, duration, type, status
            ))
        }
        return details
    }
}