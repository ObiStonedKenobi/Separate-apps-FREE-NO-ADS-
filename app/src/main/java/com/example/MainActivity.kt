package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TestInstance
import com.example.ui.screens.CreateInstanceDialog
import com.example.ui.screens.DeviceInfoScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InstanceSandboxScreen
import com.example.ui.screens.LimitationsScreen
import com.example.ui.screens.TestLogScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SeparateInstanceTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HarnessScreen
import com.example.ui.viewmodel.HarnessViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HarnessViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SeparateInstanceTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val activeInstance by viewModel.activeSandboxInstance.collectAsStateWithLifecycle()
                val instances by viewModel.filteredInstances.collectAsStateWithLifecycle()
                val rawInstances by viewModel.rawInstances.collectAsStateWithLifecycle()
                val searchQuery by viewModel.instanceSearchQuery.collectAsStateWithLifecycle()
                val logs by viewModel.filteredLogs.collectAsStateWithLifecycle()
                val stateFilter by viewModel.stateFilter.collectAsStateWithLifecycle()
                val networkSnapshot by viewModel.networkState.collectAsStateWithLifecycle()
                val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
                val deviceHealth by viewModel.deviceHealth.collectAsStateWithLifecycle()

                var showCreateDialog by remember { mutableStateOf(false) }
                var instanceToEdit by remember { mutableStateOf<TestInstance?>(null) }

                if (currentScreen == HarnessScreen.SANDBOX && activeInstance != null) {
                    InstanceSandboxScreen(
                        instance = activeInstance!!,
                        networkSnapshot = networkSnapshot,
                        sandboxManager = viewModel.sandboxManager,
                        onBack = {
                            viewModel.navigateTo(HarnessScreen.HOME)
                        },
                        onRecordResult = { canMsg, canOffer, canTrade, platMsg, notes ->
                            viewModel.recordChecklistResult(
                                instance = activeInstance!!,
                                canMessage = canMsg,
                                canTradeOffer = canOffer,
                                canCompleteTrade = canTrade,
                                platformMessage = platMsg,
                                notes = notes
                            )
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Separate Instance",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = CyberCyan.copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (networkSnapshot.isConnected) MintGreen else Color.Red)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "P2P Harness",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = CyberCyan,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = ObsidianSurface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = ObsidianSurface,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == HarnessScreen.HOME,
                                    onClick = { viewModel.navigateTo(HarnessScreen.HOME) },
                                    icon = { Icon(Icons.Default.Layers, contentDescription = "Instances") },
                                    label = { Text("Instances") },
                                    modifier = Modifier.testTag("nav_instances"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == HarnessScreen.TEST_LOGS,
                                    onClick = { viewModel.navigateTo(HarnessScreen.TEST_LOGS) },
                                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Test Logs") },
                                    label = { Text("Test Log") },
                                    modifier = Modifier.testTag("nav_test_logs"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == HarnessScreen.DEVICE_INFO,
                                    onClick = { viewModel.navigateTo(HarnessScreen.DEVICE_INFO) },
                                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = "Device") },
                                    label = { Text("S24 Ultra") },
                                    modifier = Modifier.testTag("nav_device_info"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentScreen == HarnessScreen.LIMITATIONS,
                                    onClick = { viewModel.navigateTo(HarnessScreen.LIMITATIONS) },
                                    icon = { Icon(Icons.Default.Info, contentDescription = "Limits") },
                                    label = { Text("Limitations") },
                                    modifier = Modifier.testTag("nav_limitations"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            when (currentScreen) {
                                HarnessScreen.HOME -> {
                                    HomeScreen(
                                        instances = instances,
                                        searchQuery = searchQuery,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        networkSnapshot = networkSnapshot,
                                        onRefreshTelemetry = { viewModel.refreshNetworkTelemetry() },
                                        deviceHealth = deviceHealth,
                                        sandboxManager = viewModel.sandboxManager,
                                        onCreateNewClick = {
                                            instanceToEdit = null
                                            showCreateDialog = true
                                        },
                                        onLaunchInstance = { inst ->
                                            viewModel.launchInstance(inst, this@MainActivity)
                                        },
                                        onEditInstance = { inst ->
                                            instanceToEdit = inst
                                            showCreateDialog = true
                                        },
                                        onDuplicateInstance = { inst ->
                                            viewModel.duplicateInstance(inst)
                                        },
                                        onDeleteInstance = { inst ->
                                            viewModel.deleteInstance(inst)
                                        }
                                    )
                                }
                                HarnessScreen.TEST_LOGS -> {
                                    TestLogScreen(
                                        logs = logs,
                                        selectedStateFilter = stateFilter,
                                        onSelectStateFilter = { viewModel.setStateFilter(it) },
                                        onClearLogs = { viewModel.clearAllLogs() }
                                    )
                                }
                                HarnessScreen.DEVICE_INFO -> {
                                    DeviceInfoScreen(deviceProfile = viewModel.deviceProfile)
                                }
                                HarnessScreen.LIMITATIONS -> {
                                    LimitationsScreen()
                                }
                                else -> {}
                            }
                        }
                    }

                    if (showCreateDialog) {
                        CreateInstanceDialog(
                            installedApps = installedApps,
                            initialInstance = instanceToEdit,
                            onDismiss = {
                                showCreateDialog = false
                                instanceToEdit = null
                            },
                            onConfirm = { name, targetType, targetPath, targetLabel, t1, t2, host, port, type, notes ->
                                if (instanceToEdit == null) {
                                    viewModel.createInstance(
                                        name = name,
                                        targetType = targetType,
                                        targetPackageOrUrl = targetPath,
                                        targetAppName = targetLabel,
                                        toggle1 = t1,
                                        toggle2 = t2,
                                        proxyHost = host,
                                        proxyPort = port,
                                        proxyType = type,
                                        notes = notes
                                    )
                                } else {
                                    val updated = instanceToEdit!!.copy(
                                        name = name,
                                        targetType = targetType,
                                        targetPackageOrUrl = targetPath,
                                        targetAppName = targetLabel,
                                        toggle1DeviceSeparation = t1,
                                        toggle2HouseholdSeparation = t2,
                                        proxyHost = host,
                                        proxyPort = port,
                                        proxyType = type,
                                        notes = notes
                                    )
                                    viewModel.updateInstance(updated)
                                }
                                showCreateDialog = false
                                instanceToEdit = null
                            }
                        )
                    }
                }
            }
        }
    }
}
