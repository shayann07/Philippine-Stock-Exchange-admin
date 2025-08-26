package com.codingempire.adminpse.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingempire.adminpse.R
import com.codingempire.adminpse.models.ImageAnnouncement


class ImageAnnouncementAdapter(
    private var items: List<ImageAnnouncement>,
    private val onDeleteClicked: (ImageAnnouncement) -> Unit
) : RecyclerView.Adapter<ImageAnnouncementAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImage: ImageView = itemView.findViewById(R.id.posterImageView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteImageBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = items[position]
        Glide.with(holder.itemView.context)
            .load(announcement.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.glassy_action_card_bg)
            .error(R.drawable.error_image)
            .into(holder.posterImage)

        holder.deleteButton.setOnClickListener {
            onDeleteClicked(announcement)
        }
    }

    /** Replace the list and refresh all items */
    fun updateData(newList: List<ImageAnnouncement>) {
        items = newList
        notifyDataSetChanged()
    }
}
