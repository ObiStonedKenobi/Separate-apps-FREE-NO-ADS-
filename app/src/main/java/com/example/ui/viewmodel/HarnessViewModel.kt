package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.TestInstance
import com.example.data.model.TestLogEntry
import com.example.network.NetworkMonitor
import com.example.network.NetworkSnapshot
import com.example.sandbox.InstanceSandboxManager
import com.example.service.InstanceKeepAliveService
import com.example.system.DeviceProfile
import com.example.system.InstalledAppHelper
import com.example.system.InstalledAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HarnessScreen {
    HOME,
    SANDBOX,
    TEST_LOGS,
    DEVICE_INFO,
    LIMITATIONS
}

class HarnessViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val instanceDao = db.instanceDao()
    private val testLogDao = db.testLogDao()

    val sandboxManager = InstanceSandboxManager(application)
    val networkMonitor = NetworkMonitor(application)
    val deviceProfile = DeviceProfile(application)
    private val appHelper = InstalledAppHelper(application)

    val networkState: StateFlow<NetworkSnapshot> = networkMonitor.networkState

    private val _currentScreen = MutableStateFlow(HarnessScreen.HOME)
    val currentScreen: StateFlow<HarnessScreen> = _currentScreen.asStateFlow()

    private val _activeSandboxInstance = MutableStateFlow<TestInstance?>(null)
    val activeSandboxInstance: StateFlow<TestInstance?> = _activeSandboxInstance.asStateFlow()

    private val _instanceSearchQuery = MutableStateFlow("")
    val instanceSearchQuery: StateFlow<String> = _instanceSearchQuery.asStateFlow()

    private val _stateFilter = MutableStateFlow(0) // 0 = all, 1..4
    val stateFilter: StateFlow<Int> = _stateFilter.asStateFlow()

    val rawInstances: StateFlow<List<TestInstance>> = instanceDao.getAllInstances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredInstances: StateFlow<List<TestInstance>> = combine(rawInstances, _instanceSearchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.targetAppName.contains(query, ignoreCase = true) ||
            it.targetPackageOrUrl.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawLogs: StateFlow<List<TestLogEntry>> = testLogDao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredLogs: StateFlow<List<TestLogEntry>> = combine(rawLogs, _stateFilter) { logs, state ->
        if (state == 0) logs
        else logs.filter { it.stateNumber == state }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _deviceHealth = MutableStateFlow(sandboxManager.getDeviceHealth())
    val deviceHealth: StateFlow<InstanceSandboxManager.DeviceHealth> = _deviceHealth.asStateFlow()

    init {
        loadInstalledApps()
        refreshDeviceHealth()
    }

    fun navigateTo(screen: HarnessScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _instanceSearchQuery.value = query
    }

    fun setStateFilter(state: Int) {
        _stateFilter.value = state
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = appHelper.getLaunchableApps()
        }
    }

    fun refreshDeviceHealth() {
        _deviceHealth.value = sandboxManager.getDeviceHealth()
    }

    fun refreshNetworkTelemetry() {
        networkMonitor.updateNetworkInfo()
    }

    fun createInstance(
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
    ) {
        viewModelScope.launch {
            val newInstance = TestInstance(
                name = name.ifBlank { "Test Instance ${rawInstances.value.size + 1}" },
                targetType = targetType,
                targetPackageOrUrl = targetPackageOrUrl,
                targetAppName = targetAppName,
                toggle1DeviceSeparation = toggle1,
                toggle2HouseholdSeparation = toggle2,
                proxyHost = proxyHost.trim(),
                proxyPort = proxyPort,
                proxyType = proxyType,
                notes = notes
            )
            val newId = instanceDao.insertInstance(newInstance)
            // Initialize storage directory for instance
            val storageDir = sandboxManager.getInstanceStorageDir(newId)
            val bytes = sandboxManager.calculateInstanceStorage(newId)
            instanceDao.updateStorageBytes(newId, bytes)
            refreshDeviceHealth()
            updateKeepAliveService()
        }
    }

    fun updateInstance(instance: TestInstance) {
        viewModelScope.launch {
            instanceDao.updateInstance(instance)
            val bytes = sandboxManager.calculateInstanceStorage(instance.id)
            instanceDao.updateStorageBytes(instance.id, bytes)
        }
    }

    fun duplicateInstance(source: TestInstance) {
        viewModelScope.launch {
            val clone = source.copy(
                id = 0,
                name = "${source.name} (Copy)",
                virtualAndroidId = TestInstance.generateVirtualAndroidId(),
                createdAt = System.currentTimeMillis(),
                lastLaunchedAt = 0
            )
            val newId = instanceDao.insertInstance(clone)
            sandboxManager.cloneInstanceData(source.id, newId)
            val bytes = sandboxManager.calculateInstanceStorage(newId)
            instanceDao.updateStorageBytes(newId, bytes)
            refreshDeviceHealth()
            updateKeepAliveService()
        }
    }

    fun deleteInstance(instance: TestInstance) {
        viewModelScope.launch {
            if (_activeSandboxInstance.value?.id == instance.id) {
                _activeSandboxInstance.value = null
                _currentScreen.value = HarnessScreen.HOME
            }
            instanceDao.deleteInstance(instance)
            sandboxManager.deleteInstanceData(instance.id)
            refreshDeviceHealth()
            updateKeepAliveService()
        }
    }

    fun launchInstance(instance: TestInstance, context: Context) {
        viewModelScope.launch {
            instanceDao.updateLastLaunched(instance.id, System.currentTimeMillis())
            val bytes = sandboxManager.calculateInstanceStorage(instance.id)
            instanceDao.updateStorageBytes(instance.id, bytes)

            if (instance.targetType == TestInstance.TARGET_TYPE_WEB) {
                _activeSandboxInstance.value = instance
                _currentScreen.value = HarnessScreen.SANDBOX
            } else {
                // Launch native installed package
                val pm = context.packageManager
                val launchIntent = pm.getLaunchIntentForPackage(instance.targetPackageOrUrl)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "Launched ${instance.targetAppName} in native profile mode", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Package ${instance.targetPackageOrUrl} not found on this device", Toast.LENGTH_LONG).show()
                }
            }
            updateKeepAliveService()
        }
    }

    fun recordChecklistResult(
        instance: TestInstance,
        canMessage: Boolean,
        canTradeOffer: Boolean,
        canCompleteTrade: Boolean,
        platformMessage: String,
        notes: String
    ) {
        viewModelScope.launch {
            val net = networkState.value
            val entry = TestLogEntry(
                instanceId = instance.id,
                instanceName = instance.name,
                targetApp = instance.targetAppName,
                toggle1State = instance.toggle1DeviceSeparation,
                toggle2State = instance.toggle2HouseholdSeparation,
                stateNumber = instance.stateNumber,
                stateName = instance.stateName,
                canMessage = canMessage,
                canTradeOffer = canTradeOffer,
                canCompleteTrade = canCompleteTrade,
                platformMessage = platformMessage.trim(),
                externalIp = net.publicIp,
                networkType = net.networkType,
                notes = notes.trim(),
                timestamp = System.currentTimeMillis()
            )
            testLogDao.insertLog(entry)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            testLogDao.clearAllLogs()
        }
    }

    private fun updateKeepAliveService() {
        val count = rawInstances.value.size
        if (count > 0) {
            InstanceKeepAliveService.start(
                getApplication(),
                "$count active separated test instance(s) configured"
            )
        }
    }
}
