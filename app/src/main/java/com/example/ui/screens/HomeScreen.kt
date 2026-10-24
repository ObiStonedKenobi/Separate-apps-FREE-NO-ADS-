package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestInstance
import com.example.network.NetworkSnapshot
import com.example.sandbox.InstanceSandboxManager
import com.example.ui.components.LegalNoticeCard
import com.example.ui.components.LiveTelemetryBar
import com.example.ui.components.StateMatrixBadge
import com.example.ui.components.StorageWarningBanner
import com.example.ui.components.ToggleIndicators
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    instances: List<TestInstance>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    networkSnapshot: NetworkSnapshot,
    onRefreshTelemetry: () -> Unit,
    deviceHealth: InstanceSandboxManager.DeviceHealth,
    sandboxManager: InstanceSandboxManager,
    onCreateNewClick: () -> Unit,
    onLaunchInstance: (TestInstance) -> Unit,
    onEditInstance: (TestInstance) -> Unit,
    onDuplicateInstance: (TestInstance) -> Unit,
    onDeleteInstance: (TestInstance) -> Unit,
    modifier: Modifier = Modifier
) {
    var instanceToDelete by remember { mutableStateOf<TestInstance?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Legal Disclaimer Banner (Mandatory Notice)
            item {
                LegalNoticeCard()
            }

            // Live Network Telemetry
            item {
                LiveTelemetryBar(
                    networkSnapshot = networkSnapshot,
                    onRefresh = onRefreshTelemetry
                )
            }

            // Low Storage / Memory Warning
            item {
                StorageWarningBanner(
                    freeMb = deviceHealth.freeStorageMb,
                    isLow = deviceHealth.isStorageLow || deviceHealth.isRamLow
                )
            }

            // Search Bar & Instance Stats
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SEPARATE INSTANCES (${instances.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Unlimited • No Root",
                            style = MaterialTheme.typography.labelSmall.copy(color = CyberCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Filter instances by name or package...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("instance_search_bar"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }

            // Empty State
            if (instances.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("empty_instances_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Separate Instances Yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the '+' button to spin up your first separated test instance. Default creates a Device-Isolated instance (Toggle 1 ON, Toggle 2 OFF).",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCreateNewClick,
                                modifier = Modifier.testTag("empty_state_create_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyan,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create First Instance", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Instance Cards
            items(instances, key = { it.id }) { instance ->
                InstanceCard(
                    instance = instance,
                    dateFormat = dateFormat,
                    sandboxManager = sandboxManager,
                    onLaunch = { onLaunchInstance(instance) },
                    onEdit = { onEditInstance(instance) },
                    onDuplicate = { onDuplicateInstance(instance) },
                    onDelete = { instanceToDelete = instance }
                )
            }
        }

        // Floating Action Button to Create New Instance
        FloatingActionButton(
            onClick = onCreateNewClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_create_instance"),
            containerColor = CyberCyan,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create New Instance", modifier = Modifier.size(28.dp))
        }
    }

    // Delete Confirmation Dialog
    if (instanceToDelete != null) {
        val target = instanceToDelete!!
        AlertDialog(
            onDismissRequest = { instanceToDelete = null },
            title = {
                Text(
                    text = "Delete Instance: ${target.name}?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently remove this instance's private storage, cache, and session data. Other instances and the original app will NOT be touched.",
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteInstance(target)
                        instanceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Delete Data", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { instanceToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = ObsidianSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun InstanceCard(
    instance: TestInstance,
    dateFormat: SimpleDateFormat,
    sandboxManager: InstanceSandboxManager,
    onLaunch: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("instance_card_${instance.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title & State Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = instance.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${instance.targetAppName} • ${instance.targetType}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                StateMatrixBadge(stateNumber = instance.stateNumber)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggles and Virtual Identity Preview
            ToggleIndicators(
                deviceSeparation = instance.toggle1DeviceSeparation,
                householdSeparation = instance.toggle2HouseholdSeparation
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Virtual ID: ${instance.virtualAndroidId.take(8)}...",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan
                    )
                )
                Text(
                    text = "Storage: ${sandboxManager.formatBytes(instance.storageBytes)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }

            if (instance.lastLaunchedAt > 0) {
                Text(
                    text = "Last active: ${dateFormat.format(Date(instance.lastLaunchedAt))}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_instance_btn_${instance.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Instance", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("duplicate_instance_btn_${instance.id}")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Clone Instance", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_instance_btn_${instance.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Instance", tint = CoralRed, modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onLaunch,
                    modifier = Modifier.testTag("launch_instance_btn_${instance.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Launch Test", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
