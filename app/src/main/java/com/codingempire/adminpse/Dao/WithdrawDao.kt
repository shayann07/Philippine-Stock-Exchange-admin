package com.codingempire.adminpse.Dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.codingempire.adminpse.models.WithdrawModel
import com.codingempire.adminpse.models.WithdrawWithUserName

@Dao
interface WithdrawDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawRequest(withdraw: WithdrawModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(withdraws: List<WithdrawWithUserName>)

    @Query("SELECT * FROM withdrawals")
    fun getAllWithdrawRequests(): LiveData<List<WithdrawWithUserName>>

    @Query("DELETE FROM withdrawals")
    suspend fun deleteAll()

    @Query("DELETE FROM withdrawals WHERE withdraw = :status")
    suspend fun deleteByStatus(status: String)
}

