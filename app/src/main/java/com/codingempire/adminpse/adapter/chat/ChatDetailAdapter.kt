package com.codingempire.adminpse.adapter.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codingempire.adminpse.R
import com.codingempire.adminpse.models.chat.Message
import java.text.DateFormat
import java.util.Date


class ChatDetailAdapter(private var messages: List<Message>) : RecyclerView.Adapter<ChatDetailAdapter.ChatDetailViewHolder>() {

    fun setMessages(messages: List<Message?>) {
        this.messages = messages as List<Message>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatDetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ChatDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatDetailViewHolder, position: Int) {
        val message = messages[position]

        // Ensure that 'message' exists as a property in the Message class
        holder.messageText.text = message.message
        holder.messageTime.text = DateFormat.getTimeInstance().format(message.createdAt?.toDate()
            ?.let { Date(it.time) })
        holder.messageDate.text = message.createdAt?.toDate()?.let { getFormattedDate(it.time) }

        // Log the sender information for debugging
        Log.d("ChatDetailAdapter", "Message sender: ${message.sender}")

        // Set the background based on the sender ID
        when (message.sender) {
            "1" -> {
                holder.messageText.setBackgroundResource(R.drawable.bubble_left) // Sent bubble
                holder.itemView.layoutDirection = View.LAYOUT_DIRECTION_LTR
            }
            "2" -> {
                holder.messageText.setBackgroundResource(R.drawable.bubble_right) // Received bubble
                holder.itemView.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
            else -> Log.e("ChatDetailAdapter", "Unknown sender ID: ${message.sender}")
        }
    }

    override fun getItemCount(): Int = messages.size

    class ChatDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)  // Ensure this is the correct view ID
        val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        val messageDate: TextView = itemView.findViewById(R.id.messageDate)
    }

    private fun getFormattedDate(timestamp: Long): String {
        val date = Date(timestamp)
        return DateFormat.getDateInstance().format(date)
    }
}