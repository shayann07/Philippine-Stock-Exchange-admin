package com.codingempire.adminpse.fragments.create_user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codingempire.adminpse.R
import com.codingempire.adminpse.databinding.FragmentDepositBinding
import com.codingempire.adminpse.models.TransactionModel
import com.codingempire.adminpse.models.UserModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.codingempire.adminpse.Utils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DepositFragment : Fragment() {

    private var _binding: FragmentDepositBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var utils: Utils

    private var user: UserModel? = null
    private var email: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDepositBinding.inflate(inflater, container, false)
        utils = Utils(requireContext())
        email = arguments?.getString("user_email")

        binding.btnDepositRequest.setOnClickListener {
            val amtText = binding.amountValue.text.toString().trim()
            val amount = amtText.toDoubleOrNull()

            if (amount == null || amount <= 0.0) {
                Toast.makeText(requireContext(), "❌ Please enter a valid amount!", Toast.LENGTH_SHORT).show()
            } else {
                getUserByEmail(amount)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUserByEmail(amount: Double) {
        if (email.isNullOrBlank()) {
            Toast.makeText(requireContext(), "❌ No email provided!", Toast.LENGTH_SHORT).show()
            return
        }

        utils.startLoadingAnimation()

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val document = snapshot.documents[0]
                    user = document.toObject(UserModel::class.java)

                    if (user != null) {
                        updateAccounts(amount)
                    } else {
                        utils.endLoadingAnimation()
                        Toast.makeText(requireContext(), "❌ Failed to retrieve user data.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    utils.endLoadingAnimation()
                    Toast.makeText(requireContext(), "❌ No user found with email: $email", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                utils.endLoadingAnimation()
                Log.e("Deposit", "❌ Error fetching user: ${e.message}")
                Toast.makeText(requireContext(), "❌ Failed to fetch user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAccounts(amount: Double) {
        lifecycleScope.launch {
            try {
                val accSnapshot = db.collection("accounts")
                    .whereEqualTo("userId", user!!.uid)
                    .limit(1)
                    .get().await()

                val accDoc = accSnapshot.documents.firstOrNull()
                if (accDoc == null) {
                    utils.endLoadingAnimation()
                    Toast.makeText(requireContext(), "❌ Account not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val accRef = accDoc.reference

                db.runTransaction { tr ->
                    val accSnap = tr.get(accRef)

                    val currentBalance = (accSnap.get("investment.currentBalance") as? Number)?.toDouble() ?: 0.0
                    val remainingBalance = (accSnap.get("investment.remainingBalance") as? Number)?.toDouble() ?: 0.0
                    val totalDeposit = (accSnap.get("investment.totalDeposit") as? Number)?.toDouble() ?: 0.0

                    tr.update(
                        accRef, mapOf(
                            "investment.currentBalance" to currentBalance + amount,
                            "investment.remainingBalance" to remainingBalance + amount,
                            "investment.totalDeposit" to totalDeposit + amount
                        )
                    )
                }.addOnSuccessListener {
                    logAdminTransaction(amount)
                    utils.endLoadingAnimation()

                    Toast.makeText(requireContext(), "✅ Deposit successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to HomeFragment
                    findNavController().navigate(R.id.action_depositFragment_to_homeFragment)
                }.addOnFailureListener { e ->
                    utils.endLoadingAnimation()
                    Log.e("Deposit", "❌ Transaction failed", e)
                    Toast.makeText(requireContext(), "❌ Transaction failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                utils.endLoadingAnimation()
                Log.e("Deposit", "❌ Error during deposit", e)
                Toast.makeText(requireContext(), "❌ Error during deposit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logAdminTransaction(amount: Double) {
        val transactionRef = db.collection("users")
            .document(user!!.uid)
            .collection("transactions")
            .document() // Firestore will auto-generate ID

        val transaction = TransactionModel(
            rankName = "", // if you want to populate, do it here
            transactionId = transactionRef.id,
            userId = user!!.uid,
            amount = amount,
            type = TransactionModel.TYPE_DEPOSIT,
            address =  "",
            status = TransactionModel.STATUS_PENDING,
            balanceUpdated = false,
            timestamp = Timestamp.now()
        )

        transactionRef.set(transaction.toMap())
            .addOnSuccessListener {
                utils.endLoadingAnimation()
                Toast.makeText(requireContext(), "✅ Deposit & transaction logged!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_depositFragment_to_homeFragment)
            }
            .addOnFailureListener { e ->
                utils.endLoadingAnimation()
                Log.e("Deposit", "❌ Failed to log transaction", e)
                Toast.makeText(requireContext(), "❌ Transaction not logged!", Toast.LENGTH_SHORT).show()
            }
    }

}
