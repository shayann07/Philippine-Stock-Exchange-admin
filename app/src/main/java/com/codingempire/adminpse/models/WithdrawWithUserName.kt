package com.codingempire.adminpse.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.codingempire.adminpse.utils.WithdrawModelConverter

@Entity(tableName = "withdrawals")
data class WithdrawWithUserName(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @TypeConverters(WithdrawModelConverter::class) val withdraw: WithdrawModel,
    val userName: String = "",
    val lastName: String = ""
)
