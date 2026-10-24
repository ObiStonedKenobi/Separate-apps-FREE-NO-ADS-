package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_logs")
data class TestLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val instanceId: Long,
    val instanceName: String,
    val targetApp: String,
    val toggle1State: Boolean,
    val toggle2State: Boolean,
    val stateNumber: Int,
    val stateName: String,
    val canMessage: Boolean,
    val canTradeOffer: Boolean,
    val canCompleteTrade: Boolean,
    val platformMessage: String,
    val externalIp: String,
    val networkType: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
