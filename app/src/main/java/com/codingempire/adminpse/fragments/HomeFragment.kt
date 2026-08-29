package com.codingempire.adminpse.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codingempire.adminpse.R
import com.codingempire.adminpse.ViewModel.UserViewModel
import com.codingempire.adminpse.databinding.FragmentHomeBinding
import com.codingempire.adminpse.models.TransactionModel
import com.codingempire.adminpse.models.chat.User
import com.codingempire.adminpse.ui.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usersList: MutableList<User>
    private var firestore = FirebaseFirestore.getInstance()
    private var transactionsList = mutableListOf<TransactionModel>()
    private val publicKey = "YOUR_COINPAYMENTS_PUBLIC_KEY"
    private val privateKey = "YOUR_COINPAYMENTS_PRIVATE_KEY"
    private val apiUrl = "https://www.coinpayments.net/api.php"

    private val usersCollection = firestore.collection("users")
    private val accountsCollection = firestore.collection("accounts")
    private val TAG = "UserAccountLog"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersList = ArrayList()
        getAllUsers()
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("MyPref", MODE_PRIVATE)


        viewModel.getUsers().observe(viewLifecycleOwner) { usersList ->
//            Toast.makeText(requireContext(),"List:${usersList.size}", Toast.LENGTH_SHORT).show()
            val activeUsers = usersList.count { it.status.equals("active", ignoreCase = true) }
            val inactiveUsers = usersList.count { it.status.equals("inactive", ignoreCase = true) }

            binding.activeTotalUsers.text = activeUsers.toString()
            binding.inActiveTotalUsers.text = inactiveUsers.toString()
            binding.activeActiveUsers.text = activeUsers.toString()
            binding.totalActiveUsers.text = usersList.size.toString()
            binding.activeUsersWithBalance.text = activeUsers.toString()

        }

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sharedPreferences.edit().clear().apply()

            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("fromLogout", true)
            }
            startActivity(intent)
        }


        binding.chatCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chatFragment)
        }

        binding.usersWithBalanceCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_fragmnetUser)
        }
        binding.planCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_plansByCategoryFragment)
        }
        binding.withdrawalsCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_withdrawalRequestsFragment)
        }
        binding.planSettingCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_planSettingFragment)
        }
        binding.announcementCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_announcementsFragment)
        }
        binding.bell.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }
        binding.createUserCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createUserFragment)
        }
        binding.addAnnouncementPoster.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addPosterFragment)
        }
    }


//    private val db = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()


//    private fun createAdminUserAndAccount() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // 1. Create user in Firebase Auth
//                val authResult = auth.createUserWithEmailAndPassword(
//                    "admin@example.com",
//                    "REDACTED_PASSWORD"
//                ).await()
//                val firebaseUid = authResult.user?.uid ?: throw Exception("No UID from Firebase Auth")
//
//                // 2. Generate unique user ID (custom referral code)
//                val userId = generateUniqueUserId()
//
//                // 3. Create User document
//                val userDocRef = db.collection("users").document()
//                val userData = hashMapOf(
//                    "uid" to userId,
//                    "createdAt" to Timestamp.now(),
//                    "createdByAdmin" to true,
//                    "lastName" to "Afridi",
//                    "password" to "REDACTED_PASSWORD", // WARNING: Never store plain passwords in production!
//                    "phoneNumber" to "03000000027",
//                    "docId" to userDocRef.id,
//                    "referralCode" to "U5684",
//                    "name" to "Feroz",
//                    "isBlocked" to false,
//                    "email" to "admin@example.com",
//                    "status" to "inactive"
//                )
//                userDocRef.set(userData).await()
//
//                // 4. Create Account document
//                val accountDocRef = db.collection("accounts").document()
//                val accountData = hashMapOf(
//                    "accountId" to accountDocRef.id,
//                    "createdAt" to Timestamp.now(),
//                    "earnings" to hashMapOf(
//                        "buyingProfit" to 0.0,
//                        "totalEarned" to 0.0,
//                        "referralProfit" to 0.0,
//                        "dailyProfit" to 0.0,
//                        "teamProfit" to 0.0
//                    ),
//                    "investment" to hashMapOf(
//                        "totalDeposit" to 121.76,
//                        "currentBalance" to 121.76,
//                        "remainingBalance" to 121.76
//                    ),
//                    "status" to "inactive",
//                    "userId" to userId
//                )
//                accountDocRef.set(accountData).await()
//
//                Log.d("AdminUserCreation", "User and account created successfully. UserId: $userId")
//            } catch (e: Exception) {
//                Log.e("AdminUserCreation", "Error creating user/account", e)
//            }
//        }
//    }
//
//    private suspend fun generateUniqueUserId(): String {
//        val prefix = "U"
//        var userId: String
//        var exists: Boolean
//        do {
//            val randomDigits = (1000..9999).random()
//            userId = "$prefix$randomDigits"
//            val result = db.collection("users")
//                .whereEqualTo("uid", userId)
//                .get()
//                .await()
//            exists = result.size() > 0
//        } while (exists)
//        return userId
//    }


//
//    private fun logUserAccountDetailsByEmail(email: String) {
//        // 1) Find user by email
//        firestore.collection("users")
//            .whereEqualTo("email", email)
//            .limit(1)
//            .get()
//            .addOnSuccessListener { userSnap ->
//                if (userSnap.isEmpty) {
//                    Log.d(TAG, "No user found for email=$email")
//                } else {
//                    val userDoc = userSnap.documents[0]
//                    val uid = userDoc.getString("uid")
//                    Log.d(TAG, "=== User details for email=$email ===")
//                    userDoc.data?.forEach { (field, value) ->
//                        Log.d(TAG, "User.$field = $value")
//                    }
//
//                    if (uid.isNullOrEmpty()) {
//                        Log.d(TAG, "User found for email=$email but uid field is missing")
//                        return@addOnSuccessListener
//                    }
//
//                    // 2) Account doc for this uid
//                    firestore.collection("accounts")
//                        .whereEqualTo("userId", uid)
//                        .limit(1)
//                        .get()
//                        .addOnSuccessListener { acctSnap ->
//                            if (acctSnap.isEmpty) {
//                                Log.d(TAG, "No account found for uid=$uid")
//                            } else {
//                                val acctDoc = acctSnap.documents[0]
//                                Log.d(TAG, "=== Account details for uid=$uid ===")
//                                acctDoc.data?.forEach { (field, value) ->
//                                    Log.d(TAG, "Account.$field = $value")
//                                }
//                            }
//
//                            // 3) All transactions for this userId
//                            firestore.collection("transactions")
//                                .whereEqualTo("userId", uid)
//                                .get()
//                                .addOnSuccessListener { txSnap ->
//                                    if (txSnap.isEmpty) {
//                                        Log.d(TAG, "No transactions found for uid=$uid")
//                                    } else {
//                                        Log.d(TAG, "=== Transactions for uid=$uid ===")
//                                        txSnap.documents.forEach { txDoc ->
//                                            Log.d(TAG, "Transaction ${txDoc.id}:")
//                                            txDoc.data?.forEach { (field, value) ->
//                                                Log.d(TAG, "  $field = $value")
//                                            }
//                                        }
//                                    }
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.e(
//                                        TAG,
//                                        "Error fetching transactions for uid=$uid: ${e.message}",
//                                        e
//                                    )
//                                }
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e(TAG, "Error fetching account for uid=$uid: ${e.message}", e)
//                        }
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error fetching user for email=$email: ${e.message}", e)
//            }
//    }
//
//
//


    //    /**
//     * Fetches and logs every field from the `users` and `accounts` collections
//     * for the given uid.
//     */
//    private fun logUserAccountDetails(uid: String) {
//        // 1) User doc
//        firestore.collection("users")
//            .whereEqualTo("uid", uid)
//            .limit(1)
//            .get()
//            .addOnSuccessListener { userSnap ->
//                if (userSnap.isEmpty) {
//                    Log.d(TAG, "No user found for uid=$uid")
//                } else {
//                    val doc = userSnap.documents[0]
//                    Log.d(TAG, "=== User details for uid=$uid ===")
//                    doc.data?.forEach { (field, value) ->
//                        Log.d(TAG, "User.$field = $value")
//                    }
//                }
//
//                // 2) Account doc
//                firestore.collection("accounts")
//                    .whereEqualTo("userId", uid)
//                    .limit(1)
//                    .get()
//                    .addOnSuccessListener { acctSnap ->
//                        if (acctSnap.isEmpty) {
//                            Log.d(TAG, "No account found for uid=$uid")
//                        } else {
//                            val doc = acctSnap.documents[0]
//                            Log.d(TAG, "=== Account details for uid=$uid ===")
//                            doc.data?.forEach { (field, value) ->
//                                Log.d(TAG, "Account.$field = $value")
//                            }
//                        }
//
//                        // 3) All transactions for this userId
//                        firestore.collection("transactions")
//                            .whereEqualTo("userId", uid)
//                            .get()
//                            .addOnSuccessListener { txSnap ->
//                                if (txSnap.isEmpty) {
//                                    Log.d(TAG, "No transactions found for uid=$uid")
//                                } else {
//                                    Log.d(TAG, "=== Transactions for uid=$uid ===")
//                                    txSnap.documents.forEach { txDoc ->
//                                        Log.d(TAG, "Transaction ${txDoc.id}:")
//                                        txDoc.data?.forEach { (field, value) ->
//                                            Log.d(TAG, "  $field = $value")
//                                        }
//                                    }
//                                }
//                            }
//                            .addOnFailureListener { e ->
//                                Log.e(
//                                    TAG,
//                                    "Error fetching transactions for uid=$uid: ${e.message}",
//                                    e
//                                )
//                            }
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e(TAG, "Error fetching account for uid=$uid: ${e.message}", e)
//                    }
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error fetching user for uid=$uid: ${e.message}", e)
//            }
//    }
//
//
//    private suspend fun deleteSpecificUsersAndAccounts() {
//        // List of IDs to delete
//
//        val idsToDelete = listOf("U9497")
//        try {
//            // Get all user docs where uid is in idsToDelete
//            val usersToDelete = usersCollection
//                .whereIn("uid", idsToDelete)
//                .get()
//                .await()
//                .documents
//
//            // Get all account docs where userId is in idsToDelete
//            val accountsToDelete = accountsCollection
//                .whereIn("userId", idsToDelete)
//                .get()
//                .await()
//                .documents
//
//            // Batch delete
//            if (usersToDelete.isNotEmpty() || accountsToDelete.isNotEmpty()) {
//                val batch = firestore.batch()
//                usersToDelete.forEach { batch.delete(it.reference) }
//                accountsToDelete.forEach { batch.delete(it.reference) }
//                batch.commit().await()
//                Log.d(
//                    "FirebaseHelper",
//                    "Deleted ${usersToDelete.size} users and ${accountsToDelete.size} accounts for IDs $idsToDelete"
//                )
//            } else {
//                Log.d("FirebaseHelper", "No users or accounts to delete for IDs $idsToDelete")
//            }
//        } catch (e: Exception) {
//            Log.e("FirebaseHelper", "Error deleting specified docs: ${e.message}", e)
//        }
//    }


    private fun getAllUsers() {
        firestore.collection("users").get().addOnSuccessListener { result ->
            usersList.clear()
            for (document in result) {
                val user = document.toObject(User::class.java)
                usersList.add(user)
            }


            val activeUsers = usersList.count { it.status.equals("active", ignoreCase = true) }
            val inactiveUsers = usersList.count { it.status.equals("inactive", ignoreCase = true) }

            binding.activeTotalUsers.text = activeUsers.toString()
            binding.inActiveTotalUsers.text = inactiveUsers.toString()
            binding.activeActiveUsers.text = activeUsers.toString()
            binding.totalActiveUsers.text = usersList.size.toString()
            binding.activeUsersWithBalance.text = activeUsers.toString()
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }


    private fun fetchAllTransactionsList(uid: String) {
        FirebaseFirestore.getInstance().collection("transactions").whereEqualTo("status", "pending")
            .whereEqualTo("type", "deposit").whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Listen failed", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    transactionsList.clear()
                    for (document in snapshot.documents) {
                        val transaction = document.toObject(TransactionModel::class.java)
                        if (transaction != null) {
                            transactionsList.add(transaction)
                        }
                    }
                    checkForDeposit(uid)

                    // Optional: notify adapter or update UI
                    Log.d("Firestore", "Fetched ${transactionsList.size} transactions")
                }
            }
    }


    private fun checkForDeposit(uid: String) {


        for (transaction in transactionsList) {

            fetchDepositStatus(transaction, transaction.address, uid)


        }

    }


    private fun fetchDepositStatus(transactionModel: TransactionModel, txnId: String, uid: String) {
        CoroutineScope(Dispatchers.IO).launch {

            try {

                submitDepositInFragment(
                    transactionModel.transactionId, transactionModel, uid
                )
                val nonce = (System.currentTimeMillis() / 1000).toString()

                val params = mapOf(
                    "version" to "1",
                    "key" to publicKey,
                    "cmd" to "get_tx_info",
                    "txid" to txnId,
                    "format" to "json",
                    "nonce" to nonce
                )

                val postData = params.entries.joinToString("&") {
                    "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
                }

                val hmac = generateHmac(postData, privateKey)

                val request = Request.Builder().url(apiUrl)
                    .post(postData.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                    .addHeader("HMAC", hmac).build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    if (json.getString("error").equals("ok", ignoreCase = true)) {
                        val result = json.getJSONObject("result")
                        val statusText = result.getString("status_text")
                        val status = result.getInt("status")
                        withContext(Dispatchers.Main) {
                            if (status >= 100) {
                                submitDepositInFragment(
                                    transactionModel.transactionId, transactionModel, uid
                                )
                            } else {

                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            //showError("API Error: ${json.getString("error")}")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        // showError("HTTP Error: ${response.code}")
                    }
                }

            } catch (e: Exception) {
                //   Log.e(TAG, "Status Check Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    //   showError("Exception: ${e.message}")
                }
            }
        }
    }


    private fun submitDepositInFragment(
        transactionId: String, transactionModel: TransactionModel, uid: String
    ) {
        lifecycleScope.launch {
            val db = FirebaseFirestore.getInstance()


            try {
                val accSnapshot =
                    db.collection("accounts").whereEqualTo("userId", uid).limit(1).get().await()

                val accDoc = accSnapshot.documents.firstOrNull() ?: return@launch
                val accRef = accDoc.reference

                db.runTransaction { tr ->
                    val accSnap = tr.get(accRef)

                    val currentBalance =
                        (accSnap.get("investment.currentBalance") as? Number)?.toDouble() ?: 0.0
                    val remainingBalance =
                        (accSnap.get("investment.remainingBalance") as? Number)?.toDouble() ?: 0.0
                    val totalDeposit =
                        (accSnap.get("investment.totalDeposit") as? Number)?.toDouble() ?: 0.0

                    val newCurrentBalance = currentBalance + transactionModel.amount
                    val newRemainingBalance = remainingBalance + transactionModel.amount
                    val newTotalDeposit = totalDeposit + transactionModel.amount

                    tr.update(
                        accRef, mapOf(
                            "investment.currentBalance" to newCurrentBalance,
                            "investment.remainingBalance" to newRemainingBalance,
                            "investment.totalDeposit" to newTotalDeposit
                        )
                    )

                    val txRef = db.collection("transactions").document(transactionId)
                    tr.update(txRef, "status", "approved")
                    tr.update(txRef, "balanceUpdated", true)

                    newCurrentBalance
                }.addOnSuccessListener { newBalance ->
                    Log.d("Deposit", "✅ Updated balance: $newBalance")
                    // You can update UI or LiveData here if needed
                }.addOnFailureListener { e ->
                    Log.e("Deposit", "❌ Transaction failed", e)
                }
            } catch (e: Exception) {
                Log.e("Deposit", "❌ Error in submitDepositInFragment", e)
            }
        }
    }


    private fun generateHmac(data: String, key: String): String {
        val hmacSha512 = "HmacSHA512"
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), hmacSha512)
        val mac = Mac.getInstance(hmacSha512)
        mac.init(secretKey)
        return mac.doFinal(data.toByteArray(Charsets.UTF_8)).joinToString("") {
            "%02x".format(it)
        }
    }


}