package com.example.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

data class SystemSpec(
    val title: String,
    val value: String,
    val category: String
)

class DeviceProfile(private val context: Context) {

    fun isBatteryOptimizationIgnored(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun getBatteryOptimizationIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getAppDetailsSettingsIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getTargetSpecs(): List<SystemSpec> {
        return listOf(
            SystemSpec("Device Model", "Samsung Galaxy S24 Ultra (SM-S928U)", "Hardware"),
            SystemSpec("One UI Version", "One UI 9.0", "OS & Platform"),
            SystemSpec("Android OS Version", "Android 17 (Upside Down Cake / V)", "OS & Platform"),
            SystemSpec("Kernel Version", "6.1.162-android14-11-34343818-abS928USQU6EZIF", "Kernel"),
            SystemSpec("Build Number", "CP2A.260605.016.S928USQU6EZIF", "Build"),
            SystemSpec("SE for Android", "Enforcing (SEPF_SM-S928U_14_0001)", "Security"),
            SystemSpec("Knox Version", "Knox 3.14 (API Level 41, DualDAR 1.9.0)", "Samsung Knox"),
            SystemSpec("Security Software", "MDF v3.3 R1, BT v1.0, WLAN v1.0, FIPS BoringSSL v1.11, SKC v2.3", "Security"),
            SystemSpec("Security Patch Level", "September 5, 2026", "Security Patch"),
            SystemSpec("Google Play System", "September 1, 2026", "Updates"),
            SystemSpec("Root Status", "Non-Rooted (Locked Bootloader, Warranty Bit 0x0)", "Integrity")
        )
    }

    fun getOneUiOptimizationChecklist(): List<Pair<String, String>> {
        return listOf(
            Pair(
                "1. Battery Optimization: Unrestricted",
                "Settings -> Apps -> Separate Instance -> Battery -> Select 'Unrestricted'. Prevents CPU throttling during background P2P transactions."
            ),
            Pair(
                "2. Sleeping Apps Exclusion",
                "Settings -> Device Care -> Battery -> Background usage limits -> Ensure Separate Instance is NOT in 'Sleeping apps' or 'Deep sleeping apps'. Add to 'Never sleeping apps'."
            ),
            Pair(
                "3. Auto-disable Unused Permissions",
                "Settings -> Apps -> Separate Instance -> Permissions -> Turn OFF 'Pause app activity if unused'. Prevents Android 17 from revoking network/foreground permissions."
            ),
            Pair(
                "4. Allow Background Data",
                "Settings -> Apps -> Separate Instance -> Mobile Data -> Turn ON 'Allow background data usage' and 'Allow data usage while Data saver is on'."
            ),
            Pair(
                "5. Samsung Memory Guardian Exemption (Good Lock)",
                "If Good Lock Memory Guardian is installed on One UI 9.0, add Separate Instance to the memory cleaner exclusion list."
            )
        )
    }
}
