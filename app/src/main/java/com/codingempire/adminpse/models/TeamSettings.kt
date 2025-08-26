package com.codingempire.adminpse.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "team_settings_table")
data class TeamSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val docId: String = "",
    val level: Int = 0,
    val profitPercentage: Double = 0.0,
    val requiredMembers: Int = 0
) : Serializable
