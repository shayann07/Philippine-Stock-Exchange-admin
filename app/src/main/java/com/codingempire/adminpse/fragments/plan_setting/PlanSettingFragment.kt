package com.codingempire.adminpse.fragments.plan_setting

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
import com.codingempire.adminpse.Utils
import com.codingempire.adminpse.ViewModel.PlanViewModel
import com.codingempire.adminpse.adapter.TeamSettingsAdapter
import com.codingempire.adminpse.databinding.FragmentPlanSettingBinding
import com.codingempire.adminpse.models.TeamSettings
import com.codingempire.adminpse.repository.AppDatabase
import com.codingempire.adminpse.repository.PlanRepository

class PlanSettingFragment : Fragment(), TeamSettingsAdapter.ClickHandler {
    private var _binding: FragmentPlanSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PlanViewModel
    private lateinit var database: AppDatabase
    private lateinit var teamSettingsAdapter: TeamSettingsAdapter
    private lateinit var utils: Utils


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        utils = Utils(requireContext())
        database = AppDatabase.getInstance(requireContext())
        val planDao = database.planDao()
        val planRepository = PlanRepository(planDao, requireContext())
        viewModel = ViewModelProvider(
            this,
            PlanViewModelFactory(requireActivity().application, planRepository)
        )[PlanViewModel::class.java]
        setupRecyclerView()
        utils.startLoadingAnimation()
        viewModel.fetchTeamSettingsFromRoom().observe(viewLifecycleOwner) { teamSettings ->
            val sortedList = teamSettings.sortedBy { it.level }
            utils.endLoadingAnimation()
            teamSettingsAdapter.updateData(sortedList)
        }

        binding.floatingButton.setOnClickListener {
            findNavController().navigate(R.id.action_planSettingFragment_to_editPlanSettingFragment)
        }
    }

    private fun setupRecyclerView() {
        binding.planSettingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        teamSettingsAdapter = TeamSettingsAdapter(emptyList(), this)
        binding.planSettingRecyclerView.adapter = teamSettingsAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditClick(teamSettings: TeamSettings) {
        val bundle = Bundle()
        bundle.putSerializable("teamSettings", teamSettings)
        findNavController().navigate(
            R.id.action_planSettingFragment_to_editPlanSettingFragment,
            bundle
        )
    }
}
