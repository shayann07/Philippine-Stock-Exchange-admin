package com.codingempire.adminpse.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codingempire.adminpse.databinding.ItemInvestmentPlansBinding
import com.codingempire.adminpse.models.PlanModel

class PlansByCategoryAdapter(var list: List<PlanModel>, val clickHandler: ClickHandler) :
    RecyclerView.Adapter<PlansByCategoryAdapter.Holder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {
        return Holder(
            ItemInvestmentPlansBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        val plan = list[position]
        holder.binding.apply {
            packageName.text = plan.planName
            planAmount.text =
                if (plan.maxAmount == null) "${plan.minAmount}$ +"          // e.g. “50 000$ +”
                else "${plan.minAmount}-${plan.maxAmount}$"
            planDailyPercentage.text = "${plan.dailyPercentage}%"
            planDirectProfit.text = "${plan.directProfit}%"
            planTotalPayout.text = "${plan.totalPayout}%"
            edit.setOnClickListener { clickHandler.onClick(plan) }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(newList: List<PlanModel>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class Holder(val binding: ItemInvestmentPlansBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface ClickHandler {
        fun onClick(planModel: PlanModel)
    }
}