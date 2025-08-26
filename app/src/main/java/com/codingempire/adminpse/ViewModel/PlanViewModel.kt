package com.codingempire.adminpse.ViewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.codingempire.adminpse.models.PlanModel
import com.codingempire.adminpse.models.TeamSettings
import com.codingempire.adminpse.repository.PlanRepository
import kotlinx.coroutines.launch

class PlanViewModel(application: Application, private val repository: PlanRepository) : AndroidViewModel(application) {

    val allPlans: LiveData<List<PlanModel>> = repository.allPlans

    init {
        repository.fetchTeamSettingsFromFirebase()
    }

    fun insertPlan(plan: PlanModel, context: Context) = viewModelScope.launch {
        repository.insert(plan, context)
        Log.d("PlanViewModel", "Plan inserted (viewmodel): $plan")
    }

    fun updatePlan(plan: PlanModel) = viewModelScope.launch {
        repository.updatePlan(plan)
    }

    fun clearPlans() = viewModelScope.launch {
        repository.deleteAll()
    }
    fun refreshData() {
        viewModelScope.launch {
            // Trigger the refresh process in the repository
            repository.fetchPlansFromFirebase()

            //
        }
    }
    fun fetchPlansFromFirebase() : LiveData<List<PlanModel>>  {
        return repository.fetchPlansFromFirebase()
    }

    ///////////////////////////////////////////////////////////////////

    fun fetchTeamSettingsFromRoom(): LiveData<List<TeamSettings>> {
        return repository.fetchTeamSettingsFromRoom()
    }

    fun addTeamSetting(ts: TeamSettings) = viewModelScope.launch {
        repository.addTeamSetting(ts)
    }

    fun updateTeamSetting(teamSettings: TeamSettings) = viewModelScope.launch {
        repository.updateTeamSetting(teamSettings)
    }
}
