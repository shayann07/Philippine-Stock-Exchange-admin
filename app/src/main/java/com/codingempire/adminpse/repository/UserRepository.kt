
package com.codingempire.adminpse.repository
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.codingempire.adminpse.models.UserModel
import com.codingempire.adminpse.Dao.UserDao
import com.codingempire.adminpse.models.Announcement

class  UserRepository(private val userDao: UserDao, private val context: Context) {

    private val firebaseHelper = FirebaseHelper(context)
    private val firestore = FirebaseFirestore.getInstance()

    // NEW: Flow streams from Room
    val allUsersFlow    = userDao.getAllUsersFlow()
    val allAccountsFlow = userDao.getAllAccountsFlow()


    suspend fun registerUser(userModel: UserModel, onResult: (Boolean, Any?) -> Unit) {
        val isUserRegistered = firebaseHelper.registerUser(userModel)
        onResult(isUserRegistered,"")
        if (isUserRegistered) {
            refreshDataFromFirebase()
        }
    }

    // existing—write into Room from Firebase
    suspend fun refreshDataFromFirebase() {
        val users    = firebaseHelper.getUsersFromFirebase()
        val accounts = firebaseHelper.getAccountsFromFirebase()
        userDao.insertAllUsers(users)
        userDao.insertAllAccounts(accounts)
    }

    ////////////////////////// Announcement ///////////////////////////

    fun addAnnouncement(announcement: Announcement) {
        firestore.collection("announcements")
            .add(announcement)
            .addOnSuccessListener {
                val documentId = it.id
                firestore.collection("announcements").document(documentId).update("id",documentId)
            }
            .addOnFailureListener {
                it.localizedMessage
            }
    }



    suspend fun getTotalApprovedWithdraw(userId: String): Double {
        return firebaseHelper.getTotalApprovedWithdrawnAmount(userId)
    }




    fun deleteAnnouncement(id: String) {
        firestore.collection("announcements").document(id).delete()
    }

    fun fetchAnnouncements(): LiveData<List<Announcement>> {
        val announcementsListLiveData = MutableLiveData<List<Announcement>>()
        val announcements: MutableList<Announcement> = ArrayList()

        firestore.collection("announcements")
            .addSnapshotListener { queryDocumentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                if (e != null) {
                    Log.e("Repo", "Listen failed: ${e.message}")
                    return@addSnapshotListener
                }
                if (queryDocumentSnapshots != null) {
                    announcements.clear()
                    for (document in queryDocumentSnapshots.documents) {
                        document.toObject(Announcement::class.java)?.let { pack ->
                            announcements.add(pack)
                        }
                    }
                    announcements.sortByDescending { it.time?.toDate() }
                    announcementsListLiveData.postValue(announcements)
                }
            }
        return announcementsListLiveData
    }

    fun fetchUsers(): LiveData<List<UserModel>> {
        val announcementsListLiveData = MutableLiveData<List<UserModel>>()
        val announcements: MutableList<UserModel> = ArrayList()

        firestore.collection("users")
            .addSnapshotListener { queryDocumentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                if (e != null) {
                    Log.e("Repo", "Listen failed: ${e.message}")
                    return@addSnapshotListener
                }
                if (queryDocumentSnapshots != null) {
                    announcements.clear()
                    for (document in queryDocumentSnapshots.documents) {
                        document.toObject(UserModel::class.java)?.let { pack ->
                            announcements.add(pack)
                        }
                    }
                    announcementsListLiveData.postValue(announcements)
                }
            }
        return announcementsListLiveData
    }

//    fun fetchSingleUser(): LiveData<UserModel> {
//        val announcementsListLiveData = MutableLiveData<List<UserModel>>()
//        val announcements: MutableList<UserModel> = ArrayList()
//
//        firestore.collection("users")
//            .addSnapshotListener { queryDocumentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
//                if (e != null) {
//                    Log.e("Repo", "Listen failed: ${e.message}")
//                    return@addSnapshotListener
//                }
//                if (queryDocumentSnapshots != null) {
//                    announcements.clear()
//                    for (document in queryDocumentSnapshots.documents) {
//                        document.toObject(UserModel::class.java)?.let { pack ->
//                            announcements.add(pack)
//                        }
//                    }
//                    announcementsListLiveData.postValue(announcements)
//                }
//            }
//        return announcementsListLiveData
//    }

    fun updateDeposit(userId: String, deposit: String) {
        val depositAmount = deposit.toDoubleOrNull()
        if (depositAmount == null) {
            Log.e("Validation", "Invalid deposit value")
            return
        }

        firestore.collection("accounts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Log.e("Firestore", "No account found for userId: $userId")
                    return@addOnSuccessListener
                }

                val document = querySnapshot.documents[0].reference

                document.update("investment.totalDeposit", depositAmount)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Deposit updated successfully")
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Failed to update: ${it.localizedMessage}")
                    }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to fetch account: ${it.localizedMessage}")
            }
    }

//    private fun getAllUsers() {
//         var usersList : MutableList<User>
//        firestore.collection("users")
//            .get()
//            .addOnSuccessListener { result ->
//                usersList.clear()
//                for (document in result) {
//                    val user = document.toObject(User::class.java)
//                    usersList.add(user)
//                }
//
//                val activeUsers = usersList.count { it.status.equals("active", ignoreCase = true) }
//                val inactiveUsers = usersList.count { it.status.equals("inactive", ignoreCase = true) }
//
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
//            }
//    }


}