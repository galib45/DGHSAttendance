package com.galib.dghsattendance.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.galib.dghsattendance.R
import com.galib.dghsattendance.data.IndividualReport
import com.galib.dghsattendance.data.IndividualReportEntry
import com.galib.dghsattendance.data.IndividualReportSummary

@Composable
fun IndividualReportPage(backStack: NavBackStack<NavKey>, report: IndividualReport) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        report.summary?.let { summary ->
            item { SummaryCard(summary = summary) }
        }
        if (report.details.isNotEmpty()) {
            item {
                Text(
                    text = "Attendance Details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(report.details) { entry ->
                AttendanceEntryCard(entry = entry)
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: IndividualReportSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile header
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = summary.hrmImageUrl.ifBlank { summary.biometricImage },
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(summary.name, style = MaterialTheme.typography.titleLarge)
                    Text(summary.designation, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("HRIS: " + summary.hrisId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Date range
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.round_date_range_24), contentDescription = null,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(summary.dateRange, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))

            // Attendance percentage bar
            Text(
                text = "Attendance: ${"%.1f".format(summary.percentage)}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { summary.percentage / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    summary.percentage >= 80f -> MaterialTheme.colorScheme.primary
                    summary.percentage >= 60f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )

            Spacer(Modifier.height(12.dp))

            // Stats grid
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Total", summary.totalDays, Modifier.weight(1f))
                StatChip("Present", summary.present, Modifier.weight(1f), isPositive = true)
                StatChip("Absent", summary.absent, Modifier.weight(1f), isNegative = true)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Leave", summary.leave, Modifier.weight(1f))
                StatChip("Weekend", summary.weekend, Modifier.weight(1f))
                StatChip("Holiday", summary.holiday, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    isPositive: Boolean = false,
    isNegative: Boolean = false
) {
    val bgColor = when {
        isPositive -> MaterialTheme.colorScheme.primaryContainer
        isNegative -> MaterialTheme.colorScheme.errorContainer
        else       -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isPositive -> MaterialTheme.colorScheme.onPrimaryContainer
        isNegative -> MaterialTheme.colorScheme.onErrorContainer
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(modifier = modifier, color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$value", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = textColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
        }
    }
}

@Composable
private fun AttendanceEntryCard(entry: IndividualReportEntry) {
    val status = if (entry.status.trim().isBlank()) entry.type else entry.status
    val statusColor = when (status.lowercase()) {
        "present" -> MaterialTheme.colorScheme.primary
        "absent"   -> MaterialTheme.colorScheme.error
        "late"    -> MaterialTheme.colorScheme.tertiary
        "holiday"  -> MaterialTheme.colorScheme.secondary
        else       -> MaterialTheme.colorScheme.outline
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator stripe
            Box(
                modifier = Modifier.width(4.dp).height(48.dp)
                    .background(statusColor, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entry.date, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                    StatusBadge(status, statusColor)
                }
                Spacer(Modifier.height(2.dp))
                Text(entry.facilityName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TimeItem("In", entry.inTime)
                    TimeItem("Out", entry.outTime)
                    TimeItem("Duration", entry.duration)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun TimeItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium)
    }
}