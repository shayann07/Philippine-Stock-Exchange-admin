package com.codingempire.adminpse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codingempire.adminpse.databinding.ItemAllWithdrawalsRequestsBinding
import com.codingempire.adminpse.models.WithdrawWithUserName

class WithdrawalsAdapter(
    private var withdrawList: List<WithdrawWithUserName> = listOf(),
    private val handler: WithdrawHandler
) : RecyclerView.Adapter<WithdrawalsAdapter.WithdrawViewHolder>() {


    inner class WithdrawViewHolder(val binding: ItemAllWithdrawalsRequestsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WithdrawWithUserName) {
            val initials = item.userName.split(" ")
                .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                .joinToString("").take(2).ifEmpty { "NA" }

            binding.avatar.text = initials
            binding.name.text = "${item.userName +" "+ item.lastName}"
            binding.amount.text = "$${item.withdraw.amount}"
            binding.walletAddress.text = item.withdraw.address
            binding.userIdTV.text = item.withdraw.userId
//            binding.status.text = item.withdraw.status.replaceFirstChar { it.uppercase() }

            binding.btnConfirm.setOnClickListener {
                handler.onConfirm(item)
            }
            binding.btnReject.setOnClickListener {
                handler.onReject(item)
            }
            binding.checkboxBlock.setOnClickListener{
                handler.onBlock(item)
            }
            binding.copyContainer.setOnClickListener {
                handler.onCopy(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WithdrawViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAllWithdrawalsRequestsBinding.inflate(inflater, parent, false)
        return WithdrawViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WithdrawViewHolder, position: Int) {
        holder.bind(withdrawList[position])
    }

    fun update(newList: List<WithdrawWithUserName>) {
        withdrawList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = withdrawList.size

    interface WithdrawHandler{
        fun onConfirm(withdraw: WithdrawWithUserName)
        fun onReject(withdraw: WithdrawWithUserName)
        fun onBlock(withdraw : WithdrawWithUserName)
        fun onCopy(withdraw: WithdrawWithUserName)
    }
}
