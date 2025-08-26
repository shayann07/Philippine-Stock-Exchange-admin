package com.codingempire.adminpse.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.codingempire.adminpse.models.AccountModel
import com.codingempire.adminpse.models.EarningsModel
import com.codingempire.adminpse.models.InvestmentModel
import com.codingempire.adminpse.models.PlanModel
import com.codingempire.adminpse.models.TeamSettings
import com.codingempire.adminpse.models.UserModel
import com.codingempire.adminpse.models.WithdrawModel
import com.codingempire.adminpse.models.WithdrawWithUserName
import com.codingempire.adminpse.utils.SharedPrefManager
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FirebaseHelper(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val plansCollection = firestore.collection("plans")
    private val usersCollection = firestore.collection("users")
    private val accountsCollection = firestore.collection("accounts")
    private val sharedPrefManager = SharedPrefManager(context)

    suspend fun registerUser(userModel: UserModel): Boolean {
        return try {
            val existingUser = firestore.collection("users")
                .whereEqualTo("email", userModel.email)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                Log.d("AdminRepo", "❌ User already exists!")
                return false
            }

            // Create user in Firebase Auth
            val firebaseUser = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(userModel.email, userModel.password)
                .await()
                .user

            if (firebaseUser == null) {
                Log.e("AdminRepo", "❌ FirebaseAuth user creation failed")
                return false
            }

            // OPTIONAL: Send email verification
            firebaseUser.sendEmailVerification().await()

            val uniqueUserId = generateUniqueUserId()
            sharedPrefManager.clearUserData()
            sharedPrefManager.saveId(uniqueUserId)

            val newUserRef = usersCollection.document()
            val newAccountRef = accountsCollection.document()

            val user = userModel.copy(
                uid = uniqueUserId,
                docId = newUserRef.id,
                createdAt = Timestamp.now(),
                createdByAdmin = true
            )

            val account = AccountModel(
                userId = uniqueUserId,
                accountId = newAccountRef.id,
                status = "inactive",
                createdAt = Timestamp.now(),
                investment = InvestmentModel(),
                earnings = EarningsModel()
            )

            firestore.runTransaction { transaction ->
                transaction.set(newUserRef, user.toMap())
                transaction.set(newAccountRef, account.toMap())
            }.await()

            Log.d("AdminRepo", "✅ User successfully created in FirebaseAuth and Firestore")
            true

        } catch (e: Exception) {
            Log.e("AdminRepo", "❌ Error registering user: ${e.message}", e)
            false
        }
    }

    private suspend fun generateUniqueUserId(): String {
        val prefix = "U"
        var userId: String
        var exists: Boolean

        do {
            val randomDigits = Random.nextInt(1000, 9999)
            userId = "$prefix$randomDigits"

            val userQuery = firestore.collection("users")
                .whereEqualTo("uid", userId)
                .get()
                .await()

            exists = userQuery.size() > 0
        } while (exists)

        return userId
    }


    fun savePlanToFirebase(plan: PlanModel) {
        plansCollection.add(plan)
            .addOnSuccessListener {
                Log.d("FirebaseHelper", "Plan saved successfully")
                plansCollection.document(it.id).update("docId", it.id)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "Error saving plan", e)
            }
    }

    fun updatePlanInFirebase(plan: PlanModel) {
        val id = plan.docId
        if (plan.docId.isEmpty()) {
            Log.e("FirebaseHelper", "Invalid document ID for updating plan. ID: $id OK")
            return
        }
        plansCollection.document(id)
            .set(plan)
            .addOnSuccessListener {
                Log.d("FirebaseHelper", "Plan updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "Error updating plan", e)
            }
    }


    suspend fun getUsersFromFirebase(): List<UserModel> {
        return try {
            val snapshot = usersCollection.get().await()
            Log.d("FirebaseHelperSnapshot", "Users count: ${snapshot.size()}")
            snapshot.documents.mapNotNull { document ->
//                Log.d("FirebaseHelperSnapshot", "Document: ${document.data}")
                document.toObject(UserModel::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error fetching users", e)
            emptyList()
        }
    }

    suspend fun getAccountsFromFirebase(): List<AccountModel> {
        return try {
            val snapshot = accountsCollection.get().await()
            Log.d("FirebaseHelperSnapshot", "Accounts count: ${snapshot.size()}")
            snapshot.documents.mapNotNull { doc ->
                val earnings = doc.get("earnings") as? Map<String, Number> ?: emptyMap()
                val investment = doc.get("investment") as? Map<String, Number> ?: emptyMap()

                AccountModel(
                    accountId = doc.id,
                    userId = doc.getString("userId") ?: "",
                    createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                    status = doc.getString("status") ?: "",
                    investment = InvestmentModel(
                        totalDeposit = investment["totalDeposit"]?.toDouble() ?: 0.0,
                        remainingBalance = investment["remainingBalance"]?.toDouble() ?: 0.0,
                        currentBalance = investment["currentBalance"]?.toDouble() ?: 0.0
                    ),
                    earnings = EarningsModel(
                        dailyProfit = earnings["dailyProfit"]?.toDouble() ?: 0.0,
                        buyingProfit = earnings["buyingProfit"]?.toDouble() ?: 0.0,
                        referralProfit = earnings["referralProfit"]?.toDouble() ?: 0.0,
                        totalEarned = earnings["totalEarned"]?.toDouble() ?: 0.0,
                        teamProfit = earnings["teamProfit"]?.toDouble() ?: 0.0
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error fetching accounts", e)
            emptyList()
        }
    }

    fun fetchPlansFromFirebase(): LiveData<List<PlanModel>> {
        val plansLiveData = MutableLiveData<List<PlanModel>>()

        plansCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                Log.e("FirebaseHelper", "Error listening for plans", error)
                return@addSnapshotListener
            }
            if (querySnapshot != null && !querySnapshot.isEmpty) {
                val plans = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(PlanModel::class.java)
                }
                plansLiveData.postValue(plans)
            } else {
                plansLiveData.postValue(emptyList())
            }
        }

        return plansLiveData
    }

    fun fetchTeamSettingsFromFirebase(): LiveData<List<TeamSettings>> {
        val plansLiveData = MutableLiveData<List<TeamSettings>>()

        firestore.collection("teamSettings").addSnapshotListener { querySnapshot, error ->
            if (error != null) {

                Log.e("FirebaseHelper", "Error listening for plans", error)
                return@addSnapshotListener
            }
            if (querySnapshot != null && !querySnapshot.isEmpty) {
                val plans = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(TeamSettings::class.java)
                }
                plansLiveData.postValue(plans)
            } else {
                plansLiveData.postValue(emptyList())
            }
        }

        return plansLiveData
    }

    // FirebaseHelper.kt
    fun saveTeamSettingToFirebase(teamSettings: TeamSettings) {
        val collection = firestore.collection("teamSettings")
        collection.add(teamSettings)
            .addOnSuccessListener { doc ->
                // store the doc ID back for future edits
                collection.document(doc.id).update("docId", doc.id)
                Log.d("FirebaseHelper", "Team setting ADDED, id=${doc.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "❌ Failed to add team setting", e)
            }
    }


    suspend fun fetchWithdrawRequestsWithUserNames(): List<WithdrawWithUserName> {
        return try {
            val users = getUsersFromFirebase()
            val userMap = users.associateBy { it.uid }

            val snapshot = firestore.collection("transactions").get().await()

            snapshot.documents.mapNotNull { doc ->
                val type = doc.getString("type") ?: return@mapNotNull null
                if (type != "withdraw") return@mapNotNull null
                val userId = doc.getString("userId") ?: return@mapNotNull null
                val userName = userMap[userId]?.name ?: "Unknown"
                val lastName = userMap[userId]?.lastName ?: "Unknown"

                val withdrawModel = WithdrawModel(
                    address = doc.getString("address") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    balanceUpdated = doc.getBoolean("balanceUpdated") ?: false,
                    status = doc.getString("status") ?: "",
                    timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                    transactionId = doc.getString("transactionId") ?: doc.id,
                    type = type,
                    userId = userId
                )

                WithdrawWithUserName(
                    withdraw = withdrawModel,
                    userName = userName,
                    lastName = lastName

                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error fetching withdraw transactions", e)
            emptyList()
        }
    }


    suspend fun getTotalApprovedWithdrawnAmount(userId: String): Double {
        return try {
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "withdraw")
                .whereEqualTo("status", "approved")
                .get()
                .await()

            val total = snapshot.documents.sumOf { doc ->
                doc.getDouble("amount") ?: 0.0
            }

            Log.d("FirebaseHelper", "Total approved withdrawn by $userId: $total")
            total
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error calculating approved withdrawn amount", e)
            0.0
        }
    }



    ///////////////////////////////////////////////
    fun updateTeamSettingInFirebase(teamSettings: TeamSettings) {
        val id = teamSettings.docId
        if (teamSettings.docId.isEmpty()) {
            Log.e("FirebaseHelper", "Invalid document ID for updating plan. ID: $id OK")
            return
        }
        firestore.collection("teamSettings").document(id)
            .set(teamSettings)
            .addOnSuccessListener {
                Log.d("FirebaseHelper", "Team Setting updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "Error updating Team Setting", e)
            }
    }
}
