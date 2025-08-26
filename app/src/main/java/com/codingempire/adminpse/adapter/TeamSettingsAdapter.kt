package com.codingempire.adminpse.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codingempire.adminpse.databinding.ItemTeamSettingsBinding
import com.codingempire.adminpse.models.TeamSettings

class TeamSettingsAdapter (
    private var teamSettingsList: List<TeamSettings>,
    private val clickHandler: ClickHandler
) : RecyclerView.Adapter<TeamSettingsAdapter.TeamSettingsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TeamSettingsViewHolder {
        return TeamSettingsViewHolder(ItemTeamSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: TeamSettingsViewHolder,
        position: Int
    ) {
        val teamSettings = teamSettingsList[position]
        holder.binding.level.text = "Level ${teamSettings.level}"
        holder.binding.profitPercentage.text = "${teamSettings.profitPercentage}%"
        holder.binding.requiredMembers.text = teamSettings.requiredMembers.toString()

        holder.binding.edit.setOnClickListener {
            clickHandler.onEditClick(teamSettings)
        }
    }

    override fun getItemCount(): Int {
        return teamSettingsList.size
    }

    fun updateData(newTeamSettingsList: List<TeamSettings>) {
        teamSettingsList = newTeamSettingsList
        notifyDataSetChanged()
    }

    inner class TeamSettingsViewHolder(val binding: ItemTeamSettingsBinding) : RecyclerView.ViewHolder(binding.root)

    interface ClickHandler{
        fun onEditClick(teamSettings: TeamSettings)
    }
}