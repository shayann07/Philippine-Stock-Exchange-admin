package com.codingempire.adminpse.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codingempire.adminpse.R
import com.codingempire.adminpse.models.UserAccountItem

class UserAdapter(
    private var fullList: List<UserAccountItem>,
    private val clickHandler: ClickHandler
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var filteredList: List<UserAccountItem> = fullList
    private val blockedUserIds = mutableSetOf<String>()

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.userNameTextView)
        val currentBalance: TextView = itemView.findViewById(R.id.currentBalance)
        val withdrawText: TextView = itemView.findViewById(R.id.withdrawTextView)
        val profitText: TextView = itemView.findViewById(R.id.profitTextView)
        val blockCheckBox: CheckBox = itemView.findViewById(R.id.blockCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = filteredList[position]
        holder.nameText.text = item.name
        holder.currentBalance.text = item.currentBalance.toString()
        holder.withdrawText.text = item.withdraw.toString()
        holder.profitText.text = item.totalEarned.toString()

        holder.itemView.setOnClickListener {
            clickHandler.onClick(item)
        }

        // Prevent recycled view issues
        holder.blockCheckBox.setOnCheckedChangeListener(null)
        holder.blockCheckBox.isChecked = blockedUserIds.contains(item.userId)

        holder.blockCheckBox.setOnClickListener {
            if (!blockedUserIds.contains(item.userId)) {
                blockedUserIds.clear()
                blockedUserIds.add(item.userId)
                notifyDataSetChanged()
                clickHandler.onBlock(item)
            }
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun updateData(newList: List<UserAccountItem>) {
        fullList = newList
        filteredList = newList
        blockedUserIds.clear()
        notifyDataSetChanged()
    }

    fun filterList(
        searchQuery: String,
        selectedStatus: String,
        userIdToStatus: Map<String, String>
    ) {
        filteredList = fullList.filter { user ->
            val matchesSearch = user.name.lowercase().contains(searchQuery)
            val userStatus = userIdToStatus[user.userId] ?: "inactive"
            val matchesStatus = when (selectedStatus) {
                "all" -> true
                else -> userStatus == selectedStatus
            }
            matchesSearch && matchesStatus
        }
        notifyDataSetChanged()
    }

    interface ClickHandler {
        fun onClick(userAccountItem: UserAccountItem)
        fun onBlock(userAccountItem: UserAccountItem)
    }
}
