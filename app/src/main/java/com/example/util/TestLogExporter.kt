package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.TestLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TestLogExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun generateCsv(logs: List<TestLogEntry>): String {
        val sb = StringBuilder()
        // Header
        sb.append("ID,Timestamp,Instance Name,Target App,Toggle 1 (Device),Toggle 2 (Household),State Number,State Name,Can Message,Can Trade Offer,Can Complete Trade,Platform Message,External IP,Network Type,Notes\n")

        for (log in logs) {
            val dateStr = dateFormat.format(Date(log.timestamp))
            sb.append("${log.id},")
            sb.append("\"$dateStr\",")
            sb.append("\"${escapeCsv(log.instanceName)}\",")
            sb.append("\"${escapeCsv(log.targetApp)}\",")
            sb.append(if (log.toggle1State) "ON" else "OFF").append(",")
            sb.append(if (log.toggle2State) "ON" else "OFF").append(",")
            sb.append("${log.stateNumber},")
            sb.append("\"${escapeCsv(log.stateName)}\",")
            sb.append(if (log.canMessage) "YES" else "NO").append(",")
            sb.append(if (log.canTradeOffer) "YES" else "NO").append(",")
            sb.append(if (log.canCompleteTrade) "YES" else "NO").append(",")
            sb.append("\"${escapeCsv(log.platformMessage)}\",")
            sb.append("\"${escapeCsv(log.externalIp)}\",")
            sb.append("\"${escapeCsv(log.networkType)}\",")
            sb.append("\"${escapeCsv(log.notes)}\"\n")
        }
        return sb.toString()
    }

    fun generateTextSummary(logs: List<TestLogEntry>): String {
        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("SEPARATE INSTANCE — P2P BLOCKER TEST MATRIX REPORT\n")
        sb.append("Samsung Galaxy S24 Ultra (One UI 9.0 / Android 17)\n")
        sb.append("Date Generated: ${dateFormat.format(Date())}\n")
        sb.append("Total Test Sessions Logged: ${logs.size}\n")
        sb.append("====================================================\n\n")

        val state1 = logs.filter { it.stateNumber == 1 }
        val state2 = logs.filter { it.stateNumber == 2 }
        val state3 = logs.filter { it.stateNumber == 3 }
        val state4 = logs.filter { it.stateNumber == 4 }

        fun formatStateSummary(title: String, list: List<TestLogEntry>) {
            sb.append("--- $title (${list.size} tests) ---\n")
            if (list.isEmpty()) {
                sb.append("  No test runs recorded for this state yet.\n\n")
                return
            }
            val msgAllowed = list.count { it.canMessage }
            val tradeOfferAllowed = list.count { it.canTradeOffer }
            val tradeCompleteAllowed = list.count { it.canCompleteTrade }
            val blocked = list.count { !it.canCompleteTrade }

            sb.append("  Messaging Allowed: $msgAllowed / ${list.size}\n")
            sb.append("  Trade Offer Allowed: $tradeOfferAllowed / ${list.size}\n")
            sb.append("  Trade Completed: $tradeCompleteAllowed / ${list.size}\n")
            sb.append("  Blocked Rate: $blocked / ${list.size}\n\n")
        }

        formatStateSummary("State 1: Both OFF (Baseline Control)", state1)
        formatStateSummary("State 2: Device Separation ONLY (Default)", state2)
        formatStateSummary("State 3: Household Separation ONLY", state3)
        formatStateSummary("State 4: Both ON (Full Separation)", state4)

        sb.append("----------------------------------------------------\n")
        sb.append("CHRONOLOGICAL SESSION LOGS:\n")
        sb.append("----------------------------------------------------\n")
        logs.forEachIndexed { index, entry ->
            val dateStr = dateFormat.format(Date(entry.timestamp))
            sb.append("[#${index + 1}] $dateStr | ${entry.instanceName} (${entry.targetApp})\n")
            sb.append("  Config: T1(Device)=${if (entry.toggle1State) "ON" else "OFF"}, T2(Household)=${if (entry.toggle2State) "ON" else "OFF"} [State ${entry.stateNumber}]\n")
            sb.append("  Results: Can Message=${if (entry.canMessage) "YES" else "NO"} | Trade Offer=${if (entry.canTradeOffer) "YES" else "NO"} | Trade Done=${if (entry.canCompleteTrade) "YES" else "NO"}\n")
            if (entry.platformMessage.isNotBlank()) {
                sb.append("  Platform Notice: \"${entry.platformMessage}\"\n")
            }
            sb.append("  Network: ${entry.networkType} | IP: ${entry.externalIp}\n")
            if (entry.notes.isNotBlank()) {
                sb.append("  Notes: ${entry.notes}\n")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(str: String): String {
        return str.replace("\"", "\"\"").replace("\n", " ")
    }

    fun shareReport(context: Context, csvContent: String, summaryText: String, asCsv: Boolean = true) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (asCsv) "text/csv" else "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Separate Instance P2P Blocker Test Report")
            putExtra(Intent.EXTRA_TEXT, if (asCsv) csvContent else summaryText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export P2P Blocker Test Report"))
    }
}
