package com.codingempire.adminpse.models

import java.io.Serializable

data class UserAccountItem(
    val userId: String = "",
    val name: String = "",
    val totalDeposit: Double = 0.0,
    val currentBalance: Double = 0.0,
    val withdraw: Double,
    val totalEarned: Double = 0.0,
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val referralCode: String = "",
) : Serializable
