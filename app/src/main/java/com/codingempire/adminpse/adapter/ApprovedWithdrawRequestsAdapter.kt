package com.codingempire.adminpse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codingempire.adminpse.databinding.ItemApprovedWithdrawalsBinding
import com.codingempire.adminpse.models.WithdrawWithUserName

class ApprovedWithdrawRequestsAdapter (private var withdrawList: List<WithdrawWithUserName>, private val handler: Handler) : RecyclerView.Adapter<ApprovedWithdrawRequestsAdapter.ApprovedWithdrawRequestsViewHolder>()  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApprovedWithdrawRequestsViewHolder {
        return ApprovedWithdrawRequestsViewHolder(ItemApprovedWithdrawalsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: ApprovedWithdrawRequestsViewHolder,
        position: Int
    ) {
        holder.bind(withdrawList[position])
    }

    override fun getItemCount(): Int {
        return withdrawList.size
    }
    fun update(newList: List<WithdrawWithUserName>) {
        withdrawList = newList
        notifyDataSetChanged()
    }

    inner class ApprovedWithdrawRequestsViewHolder(val binding: ItemApprovedWithdrawalsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WithdrawWithUserName) {
            val initials = item.userName.split(" ")
                .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                .joinToString("").take(2).ifEmpty { "NA" }

            binding.avatar.text = initials
            binding.name.text = item.userName
            binding.amount.text = "$. ${item.withdraw.amount}"

            // Set status text and color
            val status = item.withdraw.status?.capitalize() ?: "Unknown"
            binding.status.text = status

            // Optional: Change status color based on value
            val color = when (item.withdraw.status?.lowercase()) {
                "approved" -> android.graphics.Color.parseColor("#4CAF50") // Green
                "rejected" -> android.graphics.Color.parseColor("#F44336") // Red
                else -> android.graphics.Color.parseColor("#9E9E9E") // Grey
            }
            binding.status.setTextColor(color)

            binding.walletAddress.text = item.withdraw.address

            binding.copyContainer.setOnClickListener {
                handler.onCopy(item)
            }

        }
    }

    interface Handler{
        fun onCopy(withdraw: WithdrawWithUserName)
    }
}