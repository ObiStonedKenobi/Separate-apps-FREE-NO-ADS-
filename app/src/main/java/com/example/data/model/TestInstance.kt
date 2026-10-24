package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "test_instances")
data class TestInstance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetType: String = TARGET_TYPE_WEB, // TARGET_TYPE_WEB or TARGET_TYPE_NATIVE
    val targetPackageOrUrl: String,
    val targetAppName: String,
    val toggle1DeviceSeparation: Boolean = true, // Default ON
    val toggle2HouseholdSeparation: Boolean = false, // Default OFF
    val proxyHost: String = "127.0.0.1",
    val proxyPort: Int = 1080,
    val proxyType: String = "SOCKS5", // SOCKS5 or HTTP
    val virtualAndroidId: String = generateVirtualAndroidId(),
    val virtualAdId: String = UUID.randomUUID().toString(),
    val storageDirName: String = "instance_${System.currentTimeMillis()}",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLaunchedAt: Long = 0,
    val storageBytes: Long = 0
) {
    companion object {
        const val TARGET_TYPE_WEB = "WEB"
        const val TARGET_TYPE_NATIVE = "NATIVE"

        fun generateVirtualAndroidId(): String {
            val chars = "0123456789abcdef"
            return (1..16).map { chars.random() }.joinToString("")
        }

        fun getMatrixStateNumber(deviceSep: Boolean, householdSep: Boolean): Int {
            return when {
                !deviceSep && !householdSep -> 1
                deviceSep && !householdSep -> 2
                !deviceSep && householdSep -> 3
                else -> 4
            }
        }

        fun getMatrixStateName(deviceSep: Boolean, householdSep: Boolean): String {
            return when {
                !deviceSep && !householdSep -> "State 1: Both OFF (Baseline Control)"
                deviceSep && !householdSep -> "State 2: Device Separation ONLY (Default)"
                !deviceSep && householdSep -> "State 3: Household Separation ONLY"
                else -> "State 4: Both ON (Full Separation)"
            }
        }
    }

    val stateNumber: Int get() = getMatrixStateNumber(toggle1DeviceSeparation, toggle2HouseholdSeparation)
    val stateName: String get() = getMatrixStateName(toggle1DeviceSeparation, toggle2HouseholdSeparation)
}
