package com.codingempire.adminpse.fragments.plan_setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.codingempire.adminpse.Factories.PlanViewModelFactory
import com.codingempire.adminpse.R
import com.codingempire.adminpse.ViewModel.PlanViewModel
import com.codingempire.adminpse.databinding.FragmentEditPlanSettingBinding
import com.codingempire.adminpse.models.TeamSettings
import com.codingempire.adminpse.repository.AppDatabase
import com.codingempire.adminpse.repository.PlanRepository

class EditPlanSettingFragment : Fragment() {

    private var _binding: FragmentEditPlanSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PlanViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPlanSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ─────────── ViewModel / DB setup ───────────
        val planDao = AppDatabase.getInstance(requireContext()).planDao()
        val repo = PlanRepository(planDao, requireContext())
        viewModel = ViewModelProvider(
            this,
            PlanViewModelFactory(requireActivity().application, repo)
        )[PlanViewModel::class.java]

        // ─────────── Check if we are EDITING or ADDING ───────────
        val incoming = arguments?.getSerializable("teamSettings") as? TeamSettings
        incoming?.let {
            binding.level.setText(it.level.toString())
            binding.profitPercentage.setText(it.profitPercentage.toString())
            binding.requiredMembers.setText(it.requiredMembers.toString())
        }

        // ─────────── Confirm Action ───────────
        binding.btnConfirm.setOnClickListener {
            val levelStr = binding.level.text.toString().trim()
            val pctStr = binding.profitPercentage.text.toString().trim()
            val membersStr = binding.requiredMembers.text.toString().trim()

            if (levelStr.isEmpty() || pctStr.isEmpty() || membersStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter all fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val obj = TeamSettings(
                id = incoming?.id ?: 0,                // Room PK
                docId = incoming?.docId ?: "",        // blank if NEW
                level = levelStr.toInt(),
                profitPercentage = pctStr.toDouble(),
                requiredMembers = membersStr.toInt()
            )

            if (incoming == null) {
                // ───── ADD mode ─────
                viewModel.addTeamSetting(obj)
                Toast.makeText(requireContext(), "Level added", Toast.LENGTH_SHORT).show()
            } else {
                // ───── EDIT mode ─────
                viewModel.updateTeamSetting(obj)
                Toast.makeText(requireContext(), "Level updated", Toast.LENGTH_SHORT).show()
            }

            // Navigate back to the list (or Home, your choice)
            findNavController().navigate(
                R.id.planSettingFragment,                   // back to list
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.planSettingFragment, true)
                    .build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}