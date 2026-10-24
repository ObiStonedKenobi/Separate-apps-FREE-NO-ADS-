package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TestLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TestLogDao {
    @Query("SELECT * FROM test_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TestLogEntry>>

    @Query("SELECT * FROM test_logs WHERE instanceId = :instanceId ORDER BY timestamp DESC")
    fun getLogsForInstance(instanceId: Long): Flow<List<TestLogEntry>>

    @Query("SELECT * FROM test_logs WHERE stateNumber = :stateNumber ORDER BY timestamp DESC")
    fun getLogsForState(stateNumber: Int): Flow<List<TestLogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(entry: TestLogEntry): Long

    @Delete
    suspend fun deleteLog(entry: TestLogEntry)

    @Query("DELETE FROM test_logs")
    suspend fun clearAllLogs()
}
