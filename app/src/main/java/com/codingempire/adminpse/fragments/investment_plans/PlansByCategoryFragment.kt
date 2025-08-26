package com.codingempire.adminpse.fragments.investment_plans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingempire.adminpse.Factories.PlanViewModelFactory
import com.codingempire.adminpse.R
import com.codingempire.adminpse.ViewModel.PlanViewModel
import com.codingempire.adminpse.adapter.PlansByCategoryAdapter
import com.codingempire.adminpse.databinding.FragmentPlansByCategoryBinding
import com.codingempire.adminpse.models.PlanModel
import com.codingempire.adminpse.repository.AppDatabase
import com.codingempire.adminpse.repository.PlanRepository

class PlansByCategoryFragment : Fragment(),
    PlansByCategoryAdapter.ClickHandler {

    private var _binding: FragmentPlansByCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PlansByCategoryAdapter
    private lateinit var viewModel: PlanViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlansByCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set up Room → repo → VM
        val dao = AppDatabase.getInstance(requireContext()).planDao()
        val repo = PlanRepository(dao, requireContext())
        viewModel = ViewModelProvider(
            this,
            PlanViewModelFactory(requireActivity().application, repo)
        )[PlanViewModel::class.java]

        setupRecyclerView()
        setupFab()

        // observe Firestore plans and submit to adapter
        viewModel.fetchPlansFromFirebase().observe(viewLifecycleOwner) { plans ->
            // sort by minAmount, low → high
            val sortedByMin = plans.sortedBy { it.minAmount }
            adapter.updateList(sortedByMin)
        }
        viewModel.refreshData() // trigger an initial fetch
    }

    private fun setupRecyclerView() {
        adapter = PlansByCategoryAdapter(emptyList(), this)
        binding.plansRCV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlansByCategoryFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddPlan.setOnClickListener {
            // no args → add‐mode
            findNavController().navigate(
                R.id.action_plansByCategoryFragment_to_planDetailFragment
            )
        }
    }

    override fun onClick(planModel: PlanModel) {
        // edit‐mode: pass the selected plan
        val args = Bundle().apply { putSerializable("plan", planModel) }
        findNavController().navigate(
            R.id.action_plansByCategoryFragment_to_planDetailFragment,
            args
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}