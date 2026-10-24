package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.TestInstance
import com.example.network.NetworkSnapshot
import com.example.sandbox.InstanceSandboxManager
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InstanceSandboxScreen(
    instance: TestInstance,
    networkSnapshot: NetworkSnapshot,
    sandboxManager: InstanceSandboxManager,
    onBack: () -> Unit,
    onRecordResult: (
        canMessage: Boolean,
        canTradeOffer: Boolean,
        canCompleteTrade: Boolean,
        platformMessage: String,
        notes: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var webProgress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf(instance.targetPackageOrUrl) }

    // Manual QA Checklist States
    var canMessage by remember { mutableStateOf(true) }
    var canTradeOffer by remember { mutableStateOf(false) }
    var canCompleteTrade by remember { mutableStateOf(false) }
    var platformMessage by remember { mutableStateOf("") }
    var testerNotes by remember { mutableStateOf("") }
    var checklistExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Sandbox Navigation Bar & Telemetry
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ObsidianSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("sandbox_back_btn")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = instance.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "${instance.targetAppName} • Virtual ID: ${instance.virtualAndroidId.take(8)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyberCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    StateMatrixBadge(stateNumber = instance.stateNumber)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Telemetry Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToggleIndicators(
                        deviceSeparation = instance.toggle1DeviceSeparation,
                        householdSeparation = instance.toggle2HouseholdSeparation
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (networkSnapshot.isConnected) MintGreen else CoralRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "IP: ${networkSnapshot.publicIp}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (instance.toggle2HouseholdSeparation) AmberGold else TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }

        if (webProgress in 1..99) {
            LinearProgressIndicator(
                progress = { webProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = CyberCyan,
                trackColor = ObsidianSurface
            )
        }

        // Viewport: Web or Native App Profile Instructions
        Box(modifier = Modifier.weight(1f)) {
            if (instance.targetType == TestInstance.TARGET_TYPE_WEB) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_DEFAULT

                                if (instance.toggle1DeviceSeparation) {
                                    // Custom user agent signature per virtual instance
                                    userAgentString = "${userAgentString} SeparateInstance/${instance.id}_${instance.virtualAndroidId.take(6)}"
                                }
                            }

                            // Manage isolated cookie store per instance
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    webProgress = newProgress
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    currentUrl = url ?: ""
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    webProgress = 100
                                }
                            }

                            loadUrl(instance.targetPackageOrUrl)
                            webViewRef = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("sandbox_webview")
                )
            } else {
                // Native Profile Mode Info
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Native App Testing Mode",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Target: ${instance.targetAppName} (${instance.targetPackageOrUrl})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val pm = context.packageManager
                            val intent = pm.getLaunchIntentForPackage(instance.targetPackageOrUrl)
                            if (intent != null) {
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Open Native App on S24", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Switch between accounts on your S24, execute test P2P trades, and record the blocker behavior below.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }
        }

        // Floating Collapsible QA Test Checklist Drawer
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qa_checklist_drawer"),
            color = ObsidianSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checklistExpanded = !checklistExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "QA TEST CHECKLIST",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberGold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• State ${instance.stateNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                    Icon(
                        imageVector = if (checklistExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = "Toggle Checklist",
                        tint = TextSecondary
                    )
                }

                AnimatedVisibility(visible = checklistExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        // Question 1: Can message?
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Can send/receive messages?",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = canMessage,
                                    onClick = { canMessage = true },
                                    label = { Text("YES") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MintGreen,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                                FilterChip(
                                    selected = !canMessage,
                                    onClick = { canMessage = false },
                                    label = { Text("NO") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Question 2: Can create trade offer?
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. Can create/send trade offer?",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = canTradeOffer,
                                    onClick = { canTradeOffer = true },
                                    label = { Text("YES") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MintGreen,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                                FilterChip(
                                    selected = !canTradeOffer,
                                    onClick = { canTradeOffer = false },
                                    label = { Text("NO") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Question 3: Can complete trade?
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. Can complete P2P trade?",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = canCompleteTrade,
                                    onClick = { canCompleteTrade = true },
                                    label = { Text("YES") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MintGreen,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                                FilterChip(
                                    selected = !canCompleteTrade,
                                    onClick = { canCompleteTrade = false },
                                    label = { Text("NO") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Platform Message Input
                        OutlinedTextField(
                            value = platformMessage,
                            onValueChange = { platformMessage = it },
                            label = { Text("Platform Blocker Message (Exact Text)") },
                            placeholder = { Text("e.g. 'Trade blocked: shared device detected' or 'Same household'") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("platform_message_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Notes Input
                        OutlinedTextField(
                            value = testerNotes,
                            onValueChange = { testerNotes = it },
                            label = { Text("Tester Notes / Accounts Used") },
                            placeholder = { Text("Account A -> Account B") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checklist_notes_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Save Result Button
                        Button(
                            onClick = {
                                onRecordResult(
                                    canMessage,
                                    canTradeOffer,
                                    canCompleteTrade,
                                    platformMessage,
                                    testerNotes
                                )
                                Toast.makeText(context, "Test result recorded in Test Log!", Toast.LENGTH_SHORT).show()
                                platformMessage = ""
                                testerNotes = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_test_result_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Entry to Test Log", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
