package com.codingempire.adminpse.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingempire.adminpse.R
import com.codingempire.adminpse.ViewModel.UserViewModel
import com.codingempire.adminpse.adapter.UserAdapter
import com.codingempire.adminpse.databinding.FragmentUserBinding
import com.codingempire.adminpse.models.UserAccountItem
import com.codingempire.adminpse.models.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.codingempire.adminpse.Utils

class FragmentUser : Fragment(), UserAdapter.ClickHandler {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserViewModel
    private lateinit var userAdapter: UserAdapter
    private lateinit var utils: Utils
    private var fullUserList: List<UserAccountItem> = emptyList()
    private var allAccount: List<UserModel> = emptyList()
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Utils for showing loading animation
        utils = Utils(requireContext())

        viewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userAdapter = UserAdapter(emptyList(), this)
        binding.usersRecyclerView.adapter = userAdapter

        // 1. Observe the “isRefreshing” flag instead of trying to stop in mergedLiveData:
        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefreshLayout.isRefreshing = refreshing
        }

        // Observe all users for filtering
        viewModel.allUsers.observe(viewLifecycleOwner) { accounts ->
            if (_binding == null) return@observe
            allAccount = accounts
            applyFilters()
        }

        // 2. Keep your mergedLiveData observer purely for data:
        viewModel.mergedLiveData.observe(viewLifecycleOwner) { items ->
            utils.endLoadingAnimation()             // still end your custom loader here
            fullUserList = items
            userAdapter.updateData(items)
            applyFilters()
        }

        // 3. On pull-to-refresh, just call refreshData():
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }

        // Text change listener for search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Item selection listener for status spinner
        binding.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyFilters() {
        val currentBinding = _binding ?: return
        val searchQuery = currentBinding.searchEditText.text.toString().trim().lowercase()
        val selectedStatus = currentBinding.statusSpinner.selectedItem.toString().lowercase()
        val userIdToStatus = allAccount.associateBy({ it.uid }, { it.status.lowercase() })
        userAdapter.filterList(searchQuery, selectedStatus, userIdToStatus)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(userAccountItem: UserAccountItem) {
        val bundle = Bundle()
        bundle.putSerializable("userAccountItem", userAccountItem)
        findNavController().navigate(R.id.action_fragmnetUser_to_usersWithBalanceFragment, bundle)
    }

    override fun onBlock(userAccountItem: UserAccountItem) {
        // Start loading animation before blocking user
        utils.startLoadingAnimation()

        firestore.collection("users")
            .whereEqualTo("uid", userAccountItem.userId)
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
                                .whereEqualTo("userId", userAccountItem.userId)
                                .get()
                                .addOnSuccessListener { transactionSnapshot ->
                                    val batch = firestore.batch()
                                    for (transactionDoc in transactionSnapshot.documents) {
                                        val transactionRef = firestore.collection("transactions")
                                            .document(transactionDoc.id)
                                        batch.update(transactionRef, "status", "blocked")
                                    }

                                    batch.commit()
                                        .addOnSuccessListener {
                                            utils.endLoadingAnimation() // End loading animation on success
                                            Toast.makeText(
                                                requireContext(),
                                                "User Blocked!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            utils.endLoadingAnimation() // End loading animation on failure
                                            Toast.makeText(
                                                requireContext(),
                                                "User blocked but failed to update transactions: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    utils.endLoadingAnimation() // End loading animation on failure
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to fetch transactions: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { exception ->
                            utils.endLoadingAnimation() // End loading animation on failure
                            Toast.makeText(
                                requireContext(),
                                "Failed to block user: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    utils.endLoadingAnimation() // End loading animation if user not found
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                utils.endLoadingAnimation() // End loading animation on failure
                Toast.makeText(
                    requireContext(),
                    "Failed to query user: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}