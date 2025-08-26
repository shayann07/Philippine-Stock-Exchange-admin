package com.codingempire.adminpse.fragments.withdraw

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingempire.adminpse.Factories.WithdrawViewModelFactory
import com.codingempire.adminpse.ViewModel.WithdrawViewModel
import com.codingempire.adminpse.adapter.WithdrawalsAdapter
import com.codingempire.adminpse.databinding.FragmentAllWithdrawsRequestBinding
import com.codingempire.adminpse.models.WithdrawWithUserName
import com.codingempire.adminpse.models.chat.User
import com.codingempire.adminpse.notifications.AccessToken
import com.codingempire.adminpse.notifications.Fcm
import com.codingempire.adminpse.repository.AppDatabase
import com.codingempire.adminpse.repository.WithdrawRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.codingempire.adminpse.Utils

class AllWithdrawalsRequestFragment : Fragment(), WithdrawalsAdapter.WithdrawHandler {
    private lateinit var binding: FragmentAllWithdrawsRequestBinding
    private lateinit var viewModel: WithdrawViewModel
    private lateinit var adapter: WithdrawalsAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersList : MutableList<User>
    private lateinit var deviceToken : String
    private lateinit var utils: Utils


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllWithdrawsRequestBinding.inflate(inflater, container, false)
        // Initialize Utils for showing loading animation
        utils = Utils(requireContext())

        firestore = FirebaseFirestore.getInstance()
        val db = AppDatabase.getInstance(requireContext())
        val repo = WithdrawRepository(db.withdrawDao(),requireContext())
        usersList = ArrayList()
        getAllUsers()
        viewModel = ViewModelProvider(
            this,
            WithdrawViewModelFactory(requireActivity().application, repo)
        )[WithdrawViewModel::class.java]

        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        observeWithdrawals()

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
        viewModel.refreshWithdrawsFromFirebase()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = WithdrawalsAdapter(emptyList(), this)
        binding.recyclerView.adapter = adapter
    }

    private fun observeWithdrawals() {
        viewModel.localWithdrawals.observe(viewLifecycleOwner) { list ->
            val allWithdrawalsList = list.filter {
                it.withdraw.status?.equals("pending", ignoreCase = true) == true
            }
            adapter.update(allWithdrawalsList)
            utils.endLoadingAnimation()

            if (binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun refreshData() {
        utils.startLoadingAnimation()
        binding.swipeRefreshLayout.isRefreshing = true
        viewModel.refreshWithdrawsFromFirebase()
    }

    private fun updateWithdrawStatus(withdrawId: String, newStatus: String) {
        FirebaseFirestore.getInstance().collection("transactions")
            .document(withdrawId)
            .update("status", newStatus)
            .addOnSuccessListener {
                utils.endLoadingAnimation()
                Toast.makeText(requireContext(), "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                utils.endLoadingAnimation()
                Toast.makeText(requireContext(), "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onConfirm(withdraw: WithdrawWithUserName) {
        utils.startLoadingAnimation()
        val matchedUser = usersList.find { it.uid!!.trim() == withdraw.withdraw.userId.trim() }
        deviceToken = matchedUser?.deviceToken.toString()

        if (true) {
            Log.d("DeviceToken", deviceToken)
        } else {
            Log.d("DeviceToken", "No matching user found or device token is null")
        }
        sendNotification(deviceToken,"Your Withdraw Request Has Been Approved!")
        updateWithdrawStatus(withdraw.withdraw.transactionId, "approved")
        refreshData()
        utils.endLoadingAnimation()
    }
    override fun onReject(withdraw: WithdrawWithUserName) {
        val matchedUser = usersList.find { user ->
            user.uid?.trim() == withdraw.withdraw.userId.trim()
        }
        deviceToken = matchedUser?.deviceToken.toString()

        if (true) {
            Log.d("DeviceToken", deviceToken)
        } else {
            Log.d("DeviceToken", "No matching user found or device token is null")
        }
        sendNotification(deviceToken,"Your Withdraw Request Has Been Rejected!")
        updateWithdrawStatus(withdraw.withdraw.transactionId, "rejected")
        refreshData()
        utils.endLoadingAnimation()
    }

    private fun sendNotification(deviceToken: String, notification: String) {
        AccessToken.getAccessTokenAsync(object : AccessToken.AccessTokenCallback {
            override fun onAccessTokenReceived(token: String?) {
                if (token != null) {
                    val fcm = Fcm()
                    fcm.sendFCMNotification(
                        deviceToken!!,
                        "Admin AI Trust",
                        "$notification!",
                        token
                    )
                }
            }
        })
    }
    override fun onBlock(withdraw: WithdrawWithUserName) {
        firestore.collection("users")
            .whereEqualTo("uid", withdraw.withdraw.userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]

                    val updates = mapOf(
                        "status" to "blocked",
                        "isBlocked" to true
                    )

                    firestore.collection("users").document(document.id)
                        .update(updates)
                        .addOnSuccessListener {
                            firestore.collection("transactions")
                                .whereEqualTo("userId", withdraw.withdraw.userId)
                                .get()
                                .addOnSuccessListener { transactionSnapshot ->
                                    val batch = firestore.batch()
                                    for (transactionDoc in transactionSnapshot.documents) {
                                        val transactionRef = firestore.collection("transactions").document(transactionDoc.id)
                                        batch.update(transactionRef, "status", "blocked")
                                    }

                                    batch.commit()
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), "User Blocked Successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(requireContext(), "User blocked but failed to update transactions: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(requireContext(), "Failed to fetch transactions: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Failed to block user: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to query user: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCopy(withdraw: WithdrawWithUserName) {
        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Wallet Address", withdraw.withdraw.address)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Address copied!", Toast.LENGTH_SHORT).show()
    }

    private fun getAllUsers(){
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    usersList.add(user)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }
}
