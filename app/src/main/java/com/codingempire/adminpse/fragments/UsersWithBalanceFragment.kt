package com.codingempire.adminpse.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.codingempire.adminpse.ViewModel.UserViewModel
import com.codingempire.adminpse.databinding.DialogEditBinding
import com.codingempire.adminpse.databinding.FragmentUsersWithBalanceBinding
import com.codingempire.adminpse.models.UserAccountItem
import com.codingempire.adminpse.Utils

class UsersWithBalanceFragment : Fragment() {
    private var _binding: FragmentUsersWithBalanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserViewModel
    private lateinit var utils: Utils
    private var currentItem: UserAccountItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersWithBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        utils = Utils(requireContext())
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Retrieve passed userAccountItem
        currentItem = arguments?.getSerializable("userAccountItem") as? UserAccountItem
        if (currentItem == null) {
            Toast.makeText(requireContext(), "User data missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Start loading and trigger data sync
        utils.startLoadingAnimation()
        viewModel.refreshData()

        // Observe combined data
        viewModel.mergedLiveData.observe(viewLifecycleOwner) { list ->
            utils.endLoadingAnimation()

            // Find the matching item
            val item = list.find { it.userId == currentItem?.userId } ?: return@observe
            currentItem = item

            // Populate UI fields
            binding.apply {
                uid.text = item.userId
                tvName.text = item.name
                tvDeposit.text = item.totalDeposit.toString()
                tvProfit.text = item.totalEarned.toString()
                tvEmail.text = item.email
                tvPhone.text = item.phone
                tvReferral.text = item.referralCode
                tvPassword.text = item.password


                // Edit deposit on button click
//                btnEditDeposit.setOnClickListener { showEditDepositDialog() }
            }
        }
    }

    /**
     * Show dialog to update deposit amount via ViewModel
     */
    private fun showEditDepositDialog() {
        val dialogBinding = DialogEditBinding.inflate(layoutInflater)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Pre-fill with current deposit
        dialogBinding.deposit.setText(currentItem?.totalDeposit.toString())
        dialogBinding.updateButton.setOnClickListener {
            val newDeposit = dialogBinding.deposit.text.toString().trim()
            if (newDeposit.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter deposit amount", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            // Update via ViewModel
            currentItem?.let {
                viewModel.updateDeposit(it.userId, newDeposit)
                Toast.makeText(requireContext(), "Deposit updated successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}