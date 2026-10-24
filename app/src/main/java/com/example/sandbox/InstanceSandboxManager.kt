package com.example.sandbox

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.data.model.TestInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class InstanceSandboxManager(private val context: Context) {

    private val baseInstancesDir: File
        get() = File(context.filesDir, "instances").apply { if (!exists()) mkdirs() }

    private val baseCacheDir: File
        get() = File(context.cacheDir, "instances").apply { if (!exists()) mkdirs() }

    fun getInstanceStorageDir(instanceId: Long): File {
        return File(baseInstancesDir, "inst_$instanceId").apply { if (!exists()) mkdirs() }
    }

    fun getInstanceCacheDir(instanceId: Long): File {
        return File(baseCacheDir, "inst_$instanceId").apply { if (!exists()) mkdirs() }
    }

    suspend fun calculateInstanceStorage(instanceId: Long): Long = withContext(Dispatchers.IO) {
        val storageDir = getInstanceStorageDir(instanceId)
        val cacheDir = getInstanceCacheDir(instanceId)
        getFolderSize(storageDir) + getFolderSize(cacheDir)
    }

    private fun getFolderSize(dir: File): Long {
        if (!dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            total += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return total
    }

    suspend fun deleteInstanceData(instanceId: Long) = withContext(Dispatchers.IO) {
        val storageDir = getInstanceStorageDir(instanceId)
        val cacheDir = getInstanceCacheDir(instanceId)
        storageDir.deleteRecursively()
        cacheDir.deleteRecursively()
    }

    suspend fun cloneInstanceData(sourceId: Long, targetId: Long) = withContext(Dispatchers.IO) {
        val srcDir = getInstanceStorageDir(sourceId)
        val dstDir = getInstanceStorageDir(targetId)
        if (srcDir.exists()) {
            copyDirectory(srcDir, dstDir)
        }
    }

    private fun copyDirectory(source: File, target: File) {
        if (!target.exists()) target.mkdirs()
        source.listFiles()?.forEach { file ->
            val destFile = File(target, file.name)
            if (file.isDirectory) {
                copyDirectory(file, destFile)
            } else {
                FileInputStream(file).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    data class DeviceHealth(
        val freeStorageMb: Long,
        val totalStorageMb: Long,
        val availableRamMb: Long,
        val totalRamMb: Long,
        val isStorageLow: Boolean,
        val isRamLow: Boolean
    )

    fun getDeviceHealth(): DeviceHealth {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val totalBlocks = stat.blockCountLong

        val freeMb = (availableBlocks * blockSize) / (1024 * 1024)
        val totalMb = (totalBlocks * blockSize) / (1024 * 1024)

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val availRamMb = memInfo.availMem / (1024 * 1024)
        val totalRamMb = memInfo.totalMem / (1024 * 1024)

        return DeviceHealth(
            freeStorageMb = freeMb,
            totalStorageMb = totalMb,
            availableRamMb = availRamMb,
            totalRamMb = totalRamMb,
            isStorageLow = freeMb < 1024, // Warning under 1GB free
            isRamLow = memInfo.lowMemory || availRamMb < 400
        )
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
