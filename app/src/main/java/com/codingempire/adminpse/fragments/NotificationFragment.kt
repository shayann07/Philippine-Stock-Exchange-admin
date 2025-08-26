package com.codingempire.adminpse.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingempire.adminpse.adapter.NotificationAdapter
import com.codingempire.adminpse.databinding.FragmentNotificationBinding
import com.codingempire.adminpse.utils.SharedPrefManager

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRCV()
        sharedPrefManager = SharedPrefManager(requireContext())
        val notification = sharedPrefManager.getNotifications()
        notificationAdapter.updateData(notification)
//        Toast.makeText(requireContext(), "Size: ${notification.size}", Toast.LENGTH_SHORT).show()
    }

    private fun setupRCV() {
        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationAdapter = NotificationAdapter(emptyList())
        binding.notificationsRecyclerView.adapter = notificationAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
