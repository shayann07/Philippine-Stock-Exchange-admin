package com.codingempire.adminpse.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "accounts_table")
data class AccountModel(
    @PrimaryKey val userId: String = "",
    val accountId: String = "",
    val status: String = "",
    val createdAt: Timestamp? = Timestamp.now(),
    val investment: InvestmentModel = InvestmentModel(),
    val earnings: EarningsModel = EarningsModel()
)
    {
        fun toMap(): Map<String, Any> {
            return mapOf(
                "userId" to userId,
                "accountId" to accountId,
                "status" to status,
                "createdAt" to (createdAt ?: Timestamp.now()),
                "investment" to investment.toMap(),
                "earnings" to earnings.toMap(),
            )
        }
}
