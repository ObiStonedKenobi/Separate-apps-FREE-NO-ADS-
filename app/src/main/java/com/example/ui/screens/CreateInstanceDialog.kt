package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TestInstance
import com.example.system.InstalledAppInfo
import com.example.ui.components.StateMatrixBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreateInstanceDialog(
    installedApps: List<InstalledAppInfo>,
    initialInstance: TestInstance? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        targetType: String,
        targetPackageOrUrl: String,
        targetAppName: String,
        toggle1: Boolean,
        toggle2: Boolean,
        proxyHost: String,
        proxyPort: Int,
        proxyType: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialInstance?.name ?: "") }
    var selectedTargetType by remember {
        mutableStateOf(initialInstance?.targetType ?: TestInstance.TARGET_TYPE_WEB)
    }
    var webUrl by remember {
        mutableStateOf(
            if (initialInstance?.targetType == TestInstance.TARGET_TYPE_WEB) initialInstance.targetPackageOrUrl
            else "https://p2p.testplatform.local"
        )
    }
    var selectedApp by remember {
        mutableStateOf(
            if (initialInstance?.targetType == TestInstance.TARGET_TYPE_NATIVE) {
                installedApps.find { it.packageName == initialInstance.targetPackageOrUrl }
            } else null
        )
    }
    var appSearchQuery by remember { mutableStateOf("") }
    var showAppPickerModal by remember { mutableStateOf(false) }

    // Toggle 1: Default ON
    var toggle1DeviceSeparation by remember {
        mutableStateOf(initialInstance?.toggle1DeviceSeparation ?: true)
    }
    // Toggle 2: Default OFF
    var toggle2HouseholdSeparation by remember {
        mutableStateOf(initialInstance?.toggle2HouseholdSeparation ?: false)
    }

    // Proxy config for Toggle 2
    var proxyHost by remember { mutableStateOf(initialInstance?.proxyHost ?: "127.0.0.1") }
    var proxyPortStr by remember { mutableStateOf(initialInstance?.proxyPort?.toString() ?: "1080") }
    var proxyType by remember { mutableStateOf(initialInstance?.proxyType ?: "SOCKS5") }

    var notes by remember { mutableStateOf(initialInstance?.notes ?: "") }

    val virtualAndroidId = remember {
        initialInstance?.virtualAndroidId ?: TestInstance.generateVirtualAndroidId()
    }

    val stateNumber = TestInstance.getMatrixStateNumber(toggle1DeviceSeparation, toggle2HouseholdSeparation)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("create_instance_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = ObsidianSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialInstance == null) "Create Separate Instance" else "Edit Instance Settings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Configure independent device & household isolation",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instance Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Instance Name / Label") },
                    placeholder = { Text("e.g. Test Account Alpha (Trader 1)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instance_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Mode Selection
                Text(
                    text = "TARGET APPLICATION ENVIRONMENT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Web Sandbox Tab
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTargetType = TestInstance.TARGET_TYPE_WEB }
                            .testTag("target_type_web_card"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedTargetType == TestInstance.TARGET_TYPE_WEB)
                                CyberCyan.copy(alpha = 0.15f) else ObsidianSurfaceElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedTargetType == TestInstance.TARGET_TYPE_WEB) CyberCyan else ObsidianBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Web / P2P Client",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                            Text(
                                text = "Isolated cookies & storage",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    // Native Installed App Tab
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTargetType = TestInstance.TARGET_TYPE_NATIVE }
                            .testTag("target_type_native_card"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedTargetType == TestInstance.TARGET_TYPE_NATIVE)
                                CyberCyan.copy(alpha = 0.15f) else ObsidianSurfaceElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedTargetType == TestInstance.TARGET_TYPE_NATIVE) CyberCyan else ObsidianBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Installed App",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                            Text(
                                text = "Installed packages on S24",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTargetType == TestInstance.TARGET_TYPE_WEB) {
                    OutlinedTextField(
                        value = webUrl,
                        onValueChange = { webUrl = it },
                        label = { Text("Target Web URL / Platform Endpoint") },
                        placeholder = { Text("https://your-platform-domain.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("web_target_url_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                } else {
                    OutlinedButton(
                        onClick = { showAppPickerModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_app_picker_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ObsidianSurfaceElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = selectedApp?.appName ?: "Select an installed application...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                )
                                if (selectedApp != null) {
                                    Text(
                                        text = selectedApp!!.packageName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Choose",
                                style = MaterialTheme.typography.labelMedium.copy(color = CyberCyan)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = ObsidianBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Core Test Toggles Section
                Text(
                    text = "INDEPENDENT TEST TOGGLES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // TOGGLE 1: Device-Level Separation (Default ON)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_1_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (toggle1DeviceSeparation) CyberCyan.copy(alpha = 0.6f) else ObsidianBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TOGGLE 1: Device-Level Separation",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                )
                                Text(
                                    text = "Default: ON. Partitions app files, isolated cookie jars, unique virtual Android ID, and sandboxed storage.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                            Switch(
                                checked = toggle1DeviceSeparation,
                                onCheckedChange = { toggle1DeviceSeparation = it },
                                modifier = Modifier.testTag("toggle_1_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = CyberCyan,
                                    uncheckedThumbColor = Color.DarkGray,
                                    uncheckedTrackColor = ObsidianBorder
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TOGGLE 2: Household Separation (Default OFF)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_2_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (toggle2HouseholdSeparation) AmberGold.copy(alpha = 0.6f) else ObsidianBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TOGGLE 2: Household Separation",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                )
                                Text(
                                    text = "Default: OFF. When ON, isolates household network identity using dedicated egress endpoint without altering device-wide network.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                            Switch(
                                checked = toggle2HouseholdSeparation,
                                onCheckedChange = { toggle2HouseholdSeparation = it },
                                modifier = Modifier.testTag("toggle_2_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = AmberGold,
                                    uncheckedThumbColor = Color.DarkGray,
                                    uncheckedTrackColor = ObsidianBorder
                                )
                            )
                        }

                        if (toggle2HouseholdSeparation) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Dedicated Instance Egress Endpoint",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = proxyHost,
                                    onValueChange = { proxyHost = it },
                                    label = { Text("Proxy/Egress Host") },
                                    modifier = Modifier
                                        .weight(2f)
                                        .testTag("proxy_host_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AmberGold,
                                        unfocusedBorderColor = ObsidianBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                OutlinedTextField(
                                    value = proxyPortStr,
                                    onValueChange = { proxyPortStr = it },
                                    label = { Text("Port") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("proxy_port_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AmberGold,
                                        unfocusedBorderColor = ObsidianBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { proxyType = "SOCKS5" }
                                ) {
                                    RadioButton(
                                        selected = proxyType == "SOCKS5",
                                        onClick = { proxyType = "SOCKS5" }
                                    )
                                    Text("SOCKS5", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { proxyType = "HTTP" }
                                ) {
                                    RadioButton(
                                        selected = proxyType == "HTTP",
                                        onClick = { proxyType = "HTTP" }
                                    )
                                    Text("HTTP", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Matrix State Preview Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current Test State:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    StateMatrixBadge(stateNumber = stateNumber)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Virtual Device Identity Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "VIRTUAL IDENTITY FINGERPRINT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Virtual Android ID: $virtualAndroidId",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan
                            )
                        )
                        Text(
                            text = if (toggle1DeviceSeparation) "Decoupled from host Settings.Secure.ANDROID_ID"
                            else "Using real host Android ID signature (Baseline)",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Tester Notes / Account Info") },
                    placeholder = { Text("e.g. Test account #4912, testing trade offer with account #5021") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instance_notes_input"),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val port = proxyPortStr.toIntOrNull() ?: 1080
                            val targetPath = if (selectedTargetType == TestInstance.TARGET_TYPE_WEB) {
                                webUrl.ifBlank { "https://p2p.testplatform.local" }
                            } else {
                                selectedApp?.packageName ?: "com.android.chrome"
                            }
                            val targetLabel = if (selectedTargetType == TestInstance.TARGET_TYPE_WEB) {
                                "P2P Web Platform"
                            } else {
                                selectedApp?.appName ?: "Installed App"
                            }

                            onConfirm(
                                name.ifBlank { "Test Instance" },
                                selectedTargetType,
                                targetPath,
                                targetLabel,
                                toggle1DeviceSeparation,
                                toggle2HouseholdSeparation,
                                proxyHost,
                                port,
                                proxyType,
                                notes
                            )
                        },
                        modifier = Modifier.testTag("confirm_create_instance_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = if (initialInstance == null) "Create Instance" else "Save Changes",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for choosing an installed app
    if (showAppPickerModal) {
        Dialog(onDismissRequest = { showAppPickerModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .testTag("app_picker_dialog"),
                shape = RoundedCornerShape(16.dp),
                color = ObsidianSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Installed Target App",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = appSearchQuery,
                        onValueChange = { appSearchQuery = it },
                        placeholder = { Text("Search installed packages...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredApps = installedApps.filter {
                        appSearchQuery.isBlank() ||
                        it.appName.contains(appSearchQuery, ignoreCase = true) ||
                        it.packageName.contains(appSearchQuery, ignoreCase = true)
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedApp = app
                                        showAppPickerModal = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                            }
                            HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAppPickerModal = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceElevated)
                    ) {
                        Text("Close", color = TextPrimary)
                    }
                }
            }
        }
    }
}
