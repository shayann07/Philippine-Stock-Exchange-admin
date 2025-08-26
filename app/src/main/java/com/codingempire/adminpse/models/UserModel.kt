package com.codingempire.adminpse.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp


@Entity(tableName = "user_table")
data class UserModel(
    @PrimaryKey val uid: String = "",
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val referralCode: String = "",
    val createdAt: Timestamp = Timestamp.now() ,
    val isBlocked: Boolean = false,
    val deviceToken: String = "",
    val status: String = "",
    val docId: String = "",
    val createdByAdmin : Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "docId" to docId,
            "name" to (name ?: ""),
            "lastName" to (lastName ?: ""),
            "email" to (email ?: ""),
            "password" to (password ?: ""),
            "phoneNumber" to (phoneNumber ?: ""),
            "referralCode" to (referralCode ?: ""),
            "deviceToken" to (deviceToken ?: ""),
            "createdAt" to (createdAt ?: Timestamp.now()),
            "isBlocked" to isBlocked,
            "status" to status,
            "createdByAdmin" to createdByAdmin
        )
    }
}

