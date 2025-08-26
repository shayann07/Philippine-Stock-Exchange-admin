package com.codingempire.adminpse.fragments.investment_plans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.codingempire.adminpse.Factories.PlanViewModelFactory
import com.codingempire.adminpse.R
import com.codingempire.adminpse.ViewModel.PlanViewModel
import com.codingempire.adminpse.databinding.FragmentPlanDetailBinding
import com.codingempire.adminpse.models.PlanModel
import com.codingempire.adminpse.repository.AppDatabase
import com.codingempire.adminpse.repository.PlanRepository

class PlanDetailFragment : Fragment() {

    // ────────────── ViewBinding ──────────────
    private var _binding: FragmentPlanDetailBinding? = null
    private val binding get() = _binding!!

    // ────────────── Members ───────────────────
    private lateinit var viewModel: PlanViewModel
    private var planModel: PlanModel? = null   // null ⇒ add-mode

    private var originalNameKeyListener: android.text.method.KeyListener? = null

    // ────────────── lifecycle ────────────────
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanDetailBinding.inflate(inflater, container, false)
        planModel = arguments?.getSerializable("plan") as? PlanModel
        originalNameKeyListener = binding.etPlanName.keyListener
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // VM setup
        val dao = AppDatabase.getInstance(requireContext()).planDao()
        val repo = PlanRepository(dao, requireContext())
        val factory = PlanViewModelFactory(requireActivity().application, repo)
        viewModel = ViewModelProvider(this, factory)[PlanViewModel::class.java]

        preFillIfEditing()

        val isEdit = planModel != null
        setPlanNameEditable(!isEdit)   // from earlier
        binding.tvTitle.text = if (isEdit) getString(R.string.edit_plan) else getString(R.string.add_plan)

        setupClickListener()
    }

    // ────────────── helpers ───────────────────
    private fun preFillIfEditing() = planModel?.let { plan ->
        binding.etPlanName.setText(plan.planName)
        binding.etMinAmount.setText(plan.minAmount.toString())
        binding.etMaxAmount.setText(plan.maxAmount.toString())
        binding.etDailyPercentage.setText(plan.dailyPercentage.toString())
        binding.etDirectProfit.setText(plan.directProfit.toString())
        binding.etTotalPayout.setText(plan.totalPayout.toString())
    }

    private fun setPlanNameEditable(editable: Boolean) = with(binding.etPlanName) {
        if (editable) {
            // restore full editability
            keyListener = originalNameKeyListener
            isEnabled = true
            isFocusable = true
            isFocusableInTouchMode = true
            isCursorVisible = true

            // bring up keyboard
            requestFocus()
            post {
                val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            // lock typing but keep normal look
            if (originalNameKeyListener == null) {
                originalNameKeyListener = keyListener // safety
            }
            keyListener = null
            isFocusable = false
            isFocusableInTouchMode = false
            isCursorVisible = false
            isEnabled = true // don’t grey it out
        }
    }

    private fun setupClickListener() = binding.btnConfirm.setOnClickListener {

        // 1️⃣ Validate
        val name = binding.etPlanName.text.toString().trim()
        val minAmount = binding.etMinAmount.text.toString().toIntOrNull() ?: 0
        val maxAmountTxt = binding.etMaxAmount.text.toString().trim()
        val maxAmount = maxAmountTxt.toIntOrNull()        // ←  NULL ⇒ unlimited
        val dailyPct = binding.etDailyPercentage.text.toString().toFloatOrNull() ?: 0f
        val directProfit = binding.etDirectProfit.text.toString().toFloatOrNull() ?: 0f
        val totalPayout = binding.etTotalPayout.text.toString().toFloatOrNull() ?: 0f

        if (name.isBlank() || minAmount <= 0 || dailyPct <= 0f ||
            directProfit <= 0f || totalPayout <= 0f
        ) {
            toast("Please fill all required fields"); return@setOnClickListener
        }

        if (maxAmount != null && maxAmount <= minAmount) {
            toast("Maximum amount must be greater than minimum amount"); return@setOnClickListener
        }

// ★ enforce the “top tier must be ≥ 50 000” rule if this is that tier
        if (planModel == null /* creating new */ && maxAmount == null && minAmount < 50_000) {
            toast("For an unlimited plan, minimum must be at least 50 000"); return@setOnClickListener
        }

        // 2️⃣ Build model
        val plan = PlanModel(
            id = planModel?.id ?: 0,
            docId = planModel?.docId ?: "",
            planName = name,
            minAmount = minAmount,
            maxAmount = maxAmount,
            dailyPercentage = dailyPct,
            directProfit = directProfit,
            totalPayout = totalPayout
        )

        // 3️⃣ Save
        val ctx = requireContext()
        if (planModel == null) {
            viewModel.insertPlan(plan, ctx)
            Toast.makeText(ctx, "Plan saved!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updatePlan(plan)
            Toast.makeText(ctx, "Plan updated!", Toast.LENGTH_SHORT).show()
        }

        // 4️⃣ Clear form & navigate up
        clearForm()
        findNavController().popBackStack()    // go back to list
    }

    private fun clearForm() = with(binding) {
        etPlanName.text?.clear()
        etMinAmount.text?.clear()
        etMaxAmount.text?.clear()
        etDailyPercentage.text?.clear()
        etDirectProfit.text?.clear()
        etTotalPayout.text?.clear()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}