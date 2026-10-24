package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TestInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanceDao {
    @Query("SELECT * FROM test_instances ORDER BY lastLaunchedAt DESC, createdAt DESC")
    fun getAllInstances(): Flow<List<TestInstance>>

    @Query("SELECT * FROM test_instances WHERE id = :id")
    suspend fun getInstanceById(id: Long): TestInstance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: TestInstance): Long

    @Update
    suspend fun updateInstance(instance: TestInstance)

    @Delete
    suspend fun deleteInstance(instance: TestInstance)

    @Query("UPDATE test_instances SET lastLaunchedAt = :timestamp WHERE id = :id")
    suspend fun updateLastLaunched(id: Long, timestamp: Long)

    @Query("UPDATE test_instances SET storageBytes = :bytes WHERE id = :id")
    suspend fun updateStorageBytes(id: Long, bytes: Long)

    @Query("SELECT COUNT(*) FROM test_instances")
    fun getInstanceCount(): Flow<Int>
}
