package com.codingempire.adminpse.repository


import android.content.Context
import androidx.lifecycle.LiveData
import com.codingempire.adminpse.Dao.WithdrawDao
import com.codingempire.adminpse.models.WithdrawModel
import com.codingempire.adminpse.models.WithdrawWithUserName
import com.codingempire.adminpse.repository.FirebaseHelper

class WithdrawRepository(private val withdrawDao: WithdrawDao, private val context: Context) {

    private val firebaseHelper = FirebaseHelper(context)

    val allWithdrawRequests: LiveData<List<WithdrawWithUserName>> = withdrawDao.getAllWithdrawRequests()

    suspend fun insert(withdraw: WithdrawModel) {
        withdrawDao.insertWithdrawRequest(withdraw)
    }

    suspend fun refreshDataFromFirebase() {
        val requestsWithNames = firebaseHelper.fetchWithdrawRequestsWithUserNames()
        clearAll()
        withdrawDao.insertAll(requestsWithNames)
    }

    suspend fun clearAll() {
        withdrawDao.deleteAll()
    }
    suspend fun deleteByStatus(status: String) {
        withdrawDao.deleteByStatus(status)
    }
}
