package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestLogEntry
import com.example.ui.components.StateMatrixBadge
import com.example.ui.components.ToggleIndicators
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TestLogExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TestLogScreen(
    logs: List<TestLogEntry>,
    selectedStateFilter: Int,
    onSelectStateFilter: (Int) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    // Aggregate statistics
    val totalRuns = logs.size
    val messagingAllowedCount = logs.count { it.canMessage }
    val tradeOffersAllowedCount = logs.count { it.canTradeOffer }
    val tradeCompletedCount = logs.count { it.canCompleteTrade }
    val tradeBlockedCount = logs.count { !it.canCompleteTrade }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Screen Header & Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "P2P TEST MATRIX LOG",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "$totalRuns session runs recorded",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        val csv = TestLogExporter.generateCsv(logs)
                        val summary = TestLogExporter.generateTextSummary(logs)
                        TestLogExporter.shareReport(context, csv, summary, asCsv = true)
                    },
                    modifier = Modifier.testTag("export_csv_btn"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear All Logs", tint = CoralRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Matrix State Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val states = listOf(
                Pair(0, "All States ($totalRuns)"),
                Pair(1, "State 1: Both OFF"),
                Pair(2, "State 2: Device ONLY"),
                Pair(3, "State 3: Household ONLY"),
                Pair(4, "State 4: Both ON")
            )
            items(states) { (stateId, label) ->
                FilterChip(
                    selected = selectedStateFilter == stateId,
                    onClick = { onSelectStateFilter(stateId) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = ObsidianSurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStateFilter == stateId,
                        borderColor = ObsidianBorder,
                        selectedBorderColor = CyberCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Metric Strip
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Messaging", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("$messagingAllowedCount / $totalRuns", style = MaterialTheme.typography.titleSmall.copy(color = MintGreen, fontWeight = FontWeight.Bold))
                }
                HorizontalDivider(modifier = Modifier.width(1.dp).height(24.dp), color = ObsidianBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Offers Created", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("$tradeOffersAllowedCount / $totalRuns", style = MaterialTheme.typography.titleSmall.copy(color = AmberGold, fontWeight = FontWeight.Bold))
                }
                HorizontalDivider(modifier = Modifier.width(1.dp).height(24.dp), color = ObsidianBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Trades Completed", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("$tradeCompletedCount / $totalRuns", style = MaterialTheme.typography.titleSmall.copy(color = if (tradeCompletedCount > 0) MintGreen else CoralRed, fontWeight = FontWeight.Bold))
                }
                HorizontalDivider(modifier = Modifier.width(1.dp).height(24.dp), color = ObsidianBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Blocked", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("$tradeBlockedCount", style = MaterialTheme.typography.titleSmall.copy(color = CoralRed, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Log List
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Test Entries for this Filter",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                    Text(
                        text = "Launch an instance and record results from the checklist drawer.",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    TestLogCard(log = log, dateFormat = dateFormat)
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Test Logs?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will delete all historical QA session records from the local database.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLogs()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Clear Logs", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = ObsidianSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun TestLogCard(
    log: TestLogEntry,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("test_log_item_${log.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Instance Name & Matrix Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.instanceName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${log.targetApp} • ${dateFormat.format(Date(log.timestamp))}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
                StateMatrixBadge(stateNumber = log.stateNumber)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle States
            ToggleIndicators(
                deviceSeparation = log.toggle1State,
                householdSeparation = log.toggle2State
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Test Results Grid
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ObsidianSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Msg: ${if (log.canMessage) "ALLOWED" else "BLOCKED"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (log.canMessage) MintGreen else CoralRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Offer: ${if (log.canTradeOffer) "ALLOWED" else "BLOCKED"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (log.canTradeOffer) MintGreen else CoralRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Trade: ${if (log.canCompleteTrade) "COMPLETED" else "BLOCKED"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (log.canCompleteTrade) MintGreen else CoralRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (log.platformMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Platform Notice: \"${log.platformMessage}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CoralRed,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Egress IP: ${log.externalIp}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = log.networkType,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            if (log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${log.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
