package com.codingempire.adminpse.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.firebase.Timestamp

@Entity(tableName = "withdraw_table")
data class WithdrawModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String = "",
    val amount: Double = 0.0,
    val balanceUpdated: Boolean = false,
    val status: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val transactionId: String = "",
    val type: String = "",
    val userId: String = ""
)