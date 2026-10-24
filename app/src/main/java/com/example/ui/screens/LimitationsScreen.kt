package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LimitationsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "HONESTY & HARDWARE LIMITS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "Architectural boundary disclosure on Samsung Galaxy S24 Ultra without root",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        // What IS separated
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("capabilities_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, MintGreen.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "What THIS App Successfully Isolates (Non-Root)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MintGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val supported = listOf(
                        Pair("App-Private Storage & Cache", "Completely separate partitioned file directories (/data/user/0/.../instances/<id>/). No file leaks."),
                        Pair("Login Sessions & Cookie Jar", "Isolated WebStorage, IndexedDB, and distinct cookie jars per instance."),
                        Pair("Virtual Android ID", "Distinct virtual Settings.Secure.ANDROID_ID presented to each instance."),
                        Pair("Virtual Advertising ID (UUID)", "Randomized advertising identifier per instance."),
                        Pair("User-Agent & Client Signature", "Virtual user-agent identity header suffix per instance."),
                        Pair("Household Network Egress (Toggle 2)", "Dedicated SOCKS5/HTTP proxy endpoint per instance for decoupled public IP without affecting device-wide network.")
                    )

                    supported.forEachIndexed { index, (title, desc) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
                            Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp))
                        }
                        if (index < supported.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = ObsidianBorder.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }

        // What CANNOT be separated without root
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("limitations_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = CoralRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "What CANNOT Be Separated Without Root",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CoralRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val limitations = listOf(
                        Pair("Real Hardware IMEI / MEID", "Protected by READ_PRIVILEGED_PHONE_STATE (system UID / root only). Apps cannot spoof telephony hardware."),
                        Pair("Hardware Serial Number", "Build.getSerial() requires privileged system permissions on Android 10+."),
                        Pair("Knox StrongBox & eSE Attestation", "Hardware-backed Samsung Knox keybox and cryptographic attestation cannot be spoofed."),
                        Pair("Cellular IMSI / Baseband Cell Tower ID", "Bound to physical SIM card and modem baseband hardware."),
                        Pair("Physical Wi-Fi MAC Address", "Enforced by kernel/firmware; Android randomizes MAC per network SSID, not per app container.")
                    )

                    limitations.forEachIndexed { index, (title, desc) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = CoralRed))
                            Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp))
                        }
                        if (index < limitations.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = ObsidianBorder.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }

        // How to interpret test results
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Help, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interpreting Blocker Test Results",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. If Blocker A is defeated by State 2 (Device ONLY):\nYour platform's shared-account detection relies on app-level signals (session tokens, cookie jar, local storage, Android ID, or browser fingerprinting). Device-level instance isolation is effective.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "2. If Blocker A STILL triggers in State 2:\nYour platform is using deep hardware signals or hardware-backed attestation (such as Play Integrity / Knox StrongBox) which unrooted Android cannot virtualize. The test result proves hardware-level protection is working.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "3. If Blocker B is defeated by State 3 (Household ONLY):\nYour platform's household trade block relies on public IP or routing egress. Changing egress IP via Toggle 2 successfully isolates the accounts.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "4. If Blocker B STILL triggers in State 3:\nYour platform is correlating accounts via additional household vectors (e.g. verified physical address, payment instruments, or location telemetry).",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, lineHeight = 18.sp)
                    )
                }
            }
        }
    }
}
