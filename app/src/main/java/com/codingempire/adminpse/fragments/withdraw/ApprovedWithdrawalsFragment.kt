package com.trustledger.adminaitrust.fragments.withdraw

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingempire.adminpse.Factories.WithdrawViewModelFactory
import com.codingempire.adminpse.ViewModel.WithdrawViewModel
import com.codingempire.adminpse.adapter.ApprovedWithdrawRequestsAdapter
import com.codingempire.adminpse.databinding.FragmentApprovedWithdrawsBinding
import com.codingempire.adminpse.models.WithdrawWithUserName
import com.codingempire.adminpse.repository.AppDatabase
import com.codingempire.adminpse.repository.WithdrawRepository
import com.codingempire.adminpse.utils.Utils

class ApprovedWithdrawalsFragment : Fragment(), ApprovedWithdrawRequestsAdapter.Handler {

    private lateinit var binding: FragmentApprovedWithdrawsBinding
    private lateinit var viewModel: WithdrawViewModel
    private lateinit var adapter: ApprovedWithdrawRequestsAdapter
    private lateinit var utils: Utils


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentApprovedWithdrawsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Utils for showing loading animation
        utils = Utils(requireContext())
        utils.endLoadingAnimation()

        setupViewModel()
        setupRecyclerView()
        observeWithdrawals()

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun setupViewModel() {
        val db = AppDatabase.getInstance(requireContext())
        val repo = WithdrawRepository(db.withdrawDao(),requireContext())
        viewModel = ViewModelProvider(
            this,
            WithdrawViewModelFactory(requireActivity().application, repo)
        )[WithdrawViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = ApprovedWithdrawRequestsAdapter(emptyList(),this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun refreshData() {
        utils.startLoadingAnimation()
        viewModel.refreshWithdrawsFromFirebase()
    }

    private fun observeWithdrawals() {
        viewModel.localWithdrawals.observe(viewLifecycleOwner) { list ->
            val approvedList = list.filter {
                it.withdraw.status.equals("approved", ignoreCase = true)
            }
            adapter.update(approvedList)

            // ✅ Stop refresh animation once data is loaded
            if (binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            utils.endLoadingAnimation()
        }
    }

    override fun onCopy(withdraw: WithdrawWithUserName) {
        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Wallet Address", withdraw.withdraw.address)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Address copied!", Toast.LENGTH_SHORT).show()
    }
}
