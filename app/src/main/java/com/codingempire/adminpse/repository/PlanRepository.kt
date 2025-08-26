package com.codingempire.adminpse.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.codingempire.adminpse.Dao.PlanDao
import com.codingempire.adminpse.models.PlanModel
import com.codingempire.adminpse.models.TeamSettings
import com.codingempire.adminpse.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PlanRepository(private val planDao: PlanDao, private val context: Context) {

    val allPlans = planDao.getAllPlans()
    private val firebaseHelper = FirebaseHelper(context)
    private val firestore = FirebaseFirestore.getInstance()

    // Pass context to check network availability
    suspend fun insert(plan: PlanModel, context: Context) {
        planDao.insertPlan(plan)
        Log.d("PlanRepository", "Plan inserted (repo-room): $plan")

        if (NetworkUtils.isNetworkAvailable(context)) {
            firebaseHelper.savePlanToFirebase(plan)
            Log.d("PlanRepository", "Plan inserted (repo-firebase): $plan")
        }
        else {
            Log.d("PlanRepository", "Plan inserted (repo-offline): $plan")
        }
    }

    // Pass context to check network availability
    suspend fun updatePlan(plan: PlanModel) {
        firebaseHelper.updatePlanInFirebase(plan)

//        if (NetworkUtils.isNetworkAvailable(context)) {
//            firebaseHelper.updatePlanInFirebase(plan)
//        }
    }

    fun addTeamSetting(teamSettings: TeamSettings) {
        firebaseHelper.saveTeamSettingToFirebase(teamSettings)
    }

    fun updateTeamSetting(teamSettings: TeamSettings) {
        firebaseHelper.updateTeamSettingInFirebase(teamSettings)
    }

    fun fetchPlansFromFirebase() : LiveData<List<PlanModel>> {
        val plans = firebaseHelper.fetchPlansFromFirebase()
//        updateRoomDatabase(plans as List<PlanModel>)
        return plans
    }


    suspend fun deleteAll() {
        planDao.deleteAllPlans()
    }

    private suspend fun updateRoomDatabase(plans: List<PlanModel>) {
        planDao.insertPlans(plans) // Make sure insertPlans() exists in PlanDao
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    fun fetchTeamSettingsFromFirebase() {
        val teamSettings: MutableList<TeamSettings> = ArrayList()
        firestore.collection("teamSettings").get()
            .addOnSuccessListener { querySnapshot ->
                val teamSettings = querySnapshot.documents.mapNotNull {
                    it.toObject(TeamSettings::class.java)
                }
                saveTeamSettingsToRoom(teamSettings)

            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "Error fetching team settings", e)
            }


    }

    private fun saveTeamSettingsToRoom(teamSettings: List<TeamSettings>) {
        CoroutineScope(Dispatchers.IO).launch {
            planDao.deleteAllTeamSettings()
            planDao.insertTeamSettings(teamSettings)
        }
    }


    fun fetchTeamSettingsFromRoom(): LiveData<List<TeamSettings>> {
        return planDao.getTeamSettings()
    }
}
