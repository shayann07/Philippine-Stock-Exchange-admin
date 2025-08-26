


package com.codingempire.adminpse.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.codingempire.adminpse.models.UserAccountItem
import com.codingempire.adminpse.models.Announcement
import com.codingempire.adminpse.models.UserModel
import com.codingempire.adminpse.repository.AppDatabase
import com.codingempire.adminpse.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    // Repository with Flow streams
    private val repository: UserRepository = UserRepository(
        userDao = AppDatabase.getInstance(application).userDao(),
        context = application
    )

    // Map to cache fetched withdraw amounts
    private val withdrawMap = mutableMapOf<String, Double>()

    /** raw users for status‐filtering **/
    val allUsers: LiveData<List<UserModel>> = repository
        .allUsersFlow
        .asLiveData()

    // Combined Flow: users + accounts -> UserAccountItem list
    private val mergedFlow: StateFlow<List<UserAccountItem>> = combine(
        repository.allUsersFlow,
        repository.allAccountsFlow
    ) { users, accounts ->
        accounts.mapNotNull { account ->
            users.find { it.uid == account.userId && !it.isBlocked }?.let { user ->
                // Fetch withdraw amount on first encounter
                if (!withdrawMap.containsKey(user.uid)) {
                    fetchWithdrawAmount(user.uid)
                }
                UserAccountItem(
                    userId = user.uid,
                    name = "${user.name} ${user.lastName}",
                    totalDeposit = account.investment.totalDeposit,
                    currentBalance = account.investment.currentBalance,
                    withdraw = withdrawMap[user.uid] ?: 0.0,
                    totalEarned = account.earnings.totalEarned,
                    email = user.email,
                    phone = user.phoneNumber,
                    password = user.password,
                    referralCode = user.referralCode

                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    // Expose as LiveData for UI
    val mergedLiveData = mergedFlow.asLiveData()

    init {
        // Initial sync from Firestore into Room
        viewModelScope.launch {
            repository.refreshDataFromFirebase()
        }
    }

    /**
     * Fetch total approved withdraw amount for a user and cache it.
     */
    private fun fetchWithdrawAmount(userId: String) {
        viewModelScope.launch {
            val amount = repository.getTotalApprovedWithdraw(userId)
            withdrawMap[userId] = amount
        }
    }

    /**
     * Manually refresh data (e.g., pull-to-refresh)
     */
    fun refreshData(): Job {
        _isRefreshing.value = true
        // return the Job so the Fragment could also hook into completion if desired
        return viewModelScope.launch {
            repository.refreshDataFromFirebase()
            _isRefreshing.postValue(false)
        }
    }

    /**
     * Legacy operations, preserved from old logic
     */
    fun registerUser(userModel: UserModel, onResult: (Boolean, Any?) -> Unit) {
        viewModelScope.launch {
            repository.registerUser(userModel, onResult)
        }
    }

    fun addAnnouncement(announcement: Announcement) {
        repository.addAnnouncement(announcement)
    }

    fun deleteAnnouncement(id: String) {
        repository.deleteAnnouncement(id)
    }

    fun fetchAnnouncements() = repository.fetchAnnouncements()

    fun getUsers() = repository.fetchUsers()

    fun updateDeposit(userId: String, deposit: String) {
        repository.updateDeposit(userId, deposit)
    }
}