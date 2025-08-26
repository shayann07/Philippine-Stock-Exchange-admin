package com.codingempire.adminpse.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingempire.adminpse.ViewModel.UserViewModel
import com.codingempire.adminpse.adapter.AnnouncementAdapter
import com.codingempire.adminpse.databinding.DialogAddAnnouncementBinding
import com.codingempire.adminpse.databinding.DialogDeleteBinding
import com.codingempire.adminpse.databinding.FragmentAnnouncementsBinding
import com.codingempire.adminpse.models.Announcement
import com.codingempire.adminpse.models.chat.User
import com.codingempire.adminpse.notifications.AccessToken
import com.codingempire.adminpse.notifications.Fcm
import com.google.firebase.firestore.FirebaseFirestore
import com.codingempire.adminpse.Utils

class AnnouncementsFragment : Fragment(), AnnouncementAdapter.ClickHandler {
    private var _binding: FragmentAnnouncementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserViewModel
    private lateinit var adapter: AnnouncementAdapter
    private lateinit var usersList : MutableList<User>
    private var firestore = FirebaseFirestore.getInstance()
    private  lateinit var utils: Utils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        utils= Utils(requireContext())
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        usersList = ArrayList()
        getAllUsers()
        setupRCV()


        binding.addAnnouncementBtn.setOnClickListener {
            showAddAnnouncementDialog()
        }
        utils.startLoadingAnimation()
        viewModel.fetchAnnouncements().observe(viewLifecycleOwner) { announcements ->
            utils.endLoadingAnimation()
            adapter.updateData(announcements)
        }
    }

    private fun showAddAnnouncementDialog() {
        val dialogBinding = DialogAddAnnouncementBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (dialog.window != null) {
            dialog.window!!.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialogBinding.makeAnnouncementButton.setOnClickListener {
            val title = dialogBinding.announcementTitle.text.toString()
            val message = dialogBinding.announcementMessage.text.toString()

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please Enter Announcement Title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Please Enter Announcement Message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val announcement = Announcement(id = "", announcement = title, message = message)
            viewModel.addAnnouncement(announcement)
            showNotification()
            Toast.makeText(requireContext(), "Announcement Added Successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNotification() {
        AccessToken.getAccessTokenAsync(object : AccessToken.AccessTokenCallback {
            override fun onAccessTokenReceived(token: String?) {
                if (token != null) {
                    val fcm = Fcm()
                    for (user in usersList){
                        Log.d("Notifications", "showNotification: ${user.uid}")
                        fcm.sendFCMNotification(
                            user.deviceToken!!,
                            "Admin AI Trust",
                            "New Announcement Alert!",
                            token
                        )
                    }
                }
            }
        })

    }

    private fun getAllUsers(){
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    usersList.add(user)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun setupRCV() {
        binding.announcementsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AnnouncementAdapter(emptyList(), this)
        binding.announcementsRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDelete(announcement: Announcement) {
        val dialogBinding = DialogDeleteBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (dialog.window != null) {
            dialog.window!!.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialogBinding.yesBtn.setOnClickListener {
            viewModel.deleteAnnouncement(announcement.id)
            dialog.dismiss()
        }
        dialogBinding.noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
