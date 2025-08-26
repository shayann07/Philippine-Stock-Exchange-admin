package com.codingempire.adminpse.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingempire.adminpse.models.PlanModel
import com.codingempire.adminpse.models.TeamSettings

@Dao
interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<PlanModel>)

    // Delete a single plan
    @Delete
    suspend fun deletePlan(plan: PlanModel)

    // Get all plans
    @Query("SELECT * FROM plans")
    fun getAllPlans(): LiveData<List<PlanModel>>

    @Query("DELETE FROM plans")
    suspend fun deleteAllPlans(): Int


    //////////////////////////// Team Settings////////////////////////////////////////

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTeamSettings(teamSettings: List<TeamSettings>)

    @Query("DELETE FROM team_settings_table")
    fun deleteAllTeamSettings()

    @Query("SELECT * FROM team_settings_table")
    fun getTeamSettings(): LiveData<List<TeamSettings>>

}
