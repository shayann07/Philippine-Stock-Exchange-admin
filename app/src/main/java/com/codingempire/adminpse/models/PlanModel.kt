package com.codingempire.adminpse.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.io.Serializable

@Entity(tableName = "plans")
data class PlanModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val docId: String = "",
    val planName: String = "",
    val minAmount: Int = 0,
    val maxAmount: Int? = null,         // ← NEW
    val dailyPercentage: Float = 0f,
    val directProfit: Float = 0f,
    val totalPayout: Float = 0f,          // ← NEW (200-500 %)
    val timestamp: Timestamp = Timestamp.now()
) : Serializable
