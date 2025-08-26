package com.codingempire.adminpse.fragments.withdraw

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
import com.codingempire.adminpse.databinding.FragmentRejectedWithdrawsBinding
import com.codingempire.adminpse.models.WithdrawWithUserName
import com.codingempire.adminpse.repository.AppDatabase
import com.codingempire.adminpse.repository.WithdrawRepository
import com.codingempire.adminpse.utils.Utils

class RejectedWithdrawalsFragment : Fragment(), ApprovedWithdrawRequestsAdapter.Handler {

    private lateinit var binding: FragmentRejectedWithdrawsBinding
    private lateinit var viewModel: WithdrawViewModel
    private lateinit var adapter: ApprovedWithdrawRequestsAdapter
    private lateinit var utils: Utils


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRejectedWithdrawsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Utils for showing loading animation
        utils = Utils(requireContext())


        val db = AppDatabase.getInstance(requireContext())
        val repo = WithdrawRepository(db.withdrawDao(),requireContext())
        viewModel = ViewModelProvider(
            this,
            WithdrawViewModelFactory(requireActivity().application, repo)
        )[WithdrawViewModel::class.java]

        setupRecyclerView()
        observeWithdrawals()

        // ✅ Swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ApprovedWithdrawRequestsAdapter(emptyList(),this)
        binding.recyclerView.adapter = adapter
    }

    private fun refreshData() {
        utils.startLoadingAnimation()
        viewModel.refreshWithdrawsFromFirebase()
    }

    private fun observeWithdrawals() {
        viewModel.localWithdrawals.observe(viewLifecycleOwner) { list ->
            val rejectedList = list.filter {
                it.withdraw.status?.equals("rejected", ignoreCase = true) == true
            }
            adapter.update(rejectedList)

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
