package com.codingempire.adminpse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codingempire.adminpse.databinding.ItemAnnouncementBinding
import com.codingempire.adminpse.models.Announcement

class AnnouncementAdapter(
    private var announcements: List<Announcement>,
    private val clickHandler: ClickHandler
) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnnouncementViewHolder {
        return AnnouncementViewHolder(ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: AnnouncementViewHolder,
        position: Int
    ) {
        val announcement = announcements[position]
        holder.binding.tvAnnouncementTitle.text = announcement.announcement
        holder.binding.tvAnnouncementMessage.text = announcement.message

        val fullDate = announcement.time.toDate().toString()
        val trimmedDate = fullDate.substringBefore("GMT").trim()
        holder.binding.tvAnnouncementTime.text = trimmedDate

        holder.binding.delete.setOnClickListener {
            clickHandler.onDelete(announcement)
        }
    }


    override fun getItemCount(): Int {
        return announcements.size
    }

    fun updateData(newAnnouncements: List<Announcement>) {
        announcements = newAnnouncements
        notifyDataSetChanged()
    }

    inner class AnnouncementViewHolder(val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root)

    interface ClickHandler{
        fun onDelete(announcement: Announcement)
    }
}