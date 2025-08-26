package com.codingempire.adminpse.fragments.chat

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.codingempire.adminpse.ViewModel.ChatViewModel
import com.codingempire.adminpse.adapter.chat.ChatDetailAdapter
import com.codingempire.adminpse.databinding.FragmentDetailChatBinding
import com.codingempire.adminpse.models.chat.User
import com.codingempire.adminpse.notifications.AccessToken
import com.codingempire.adminpse.notifications.Fcm
import com.codingempire.adminpse.repository.chat.ChatRepository
import com.codingempire.adminpse.repository.chat.ChatViewModelFactory

class DetailChatFragment : Fragment() {
    private lateinit var binding: FragmentDetailChatBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var adapter: ChatDetailAdapter
    private lateinit var chatViewModel: ChatViewModel
    private var userId: String? = ""
    private var adminId: String? = ""
    private var userName: String? = null
    private var deviceToken: String? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailChatBinding.inflate(inflater, container, false)

        sharedPreferences = requireContext().getSharedPreferences("MyPref", MODE_PRIVATE)
        adminId = sharedPreferences.getString("adminId", null)

        firestore = FirebaseFirestore.getInstance()
        chatRecyclerView = binding.recyclerViewChat
        chatRecyclerView.layoutManager = LinearLayoutManager(context)

        arguments?.let { bundle ->
            userId = bundle.getString("userId")
            userName = bundle.getString("userName")
        }
//        Toast.makeText(requireContext(), "User ID: $userId \n User Name: $userName", Toast.LENGTH_SHORT).show()
        binding.userName.text = userName
        fetchUserDeviceToken()

        adapter = ChatDetailAdapter(mutableListOf())
        chatRecyclerView.adapter = adapter

        val chatRepository = ChatRepository()
        val factory = ChatViewModelFactory(chatRepository)
        chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        userId?.let {
            chatViewModel.getChats(it, adminId!!).observe(viewLifecycleOwner) { messages ->
                adapter.setMessages(messages)
                chatRecyclerView.scrollToPosition(messages.size - 1)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSendMessage.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()

            if (messageText.isEmpty()) {
                Toast.makeText(requireContext(), "Please type a message first.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            chatViewModel.sendMessage(adminId!!, messageText, userId!!)
            sendNotification()
            binding.editTextMessage.setText("")
        }
    }

    private fun fetchUserDeviceToken() {
        firestore.collection("users")
            .document(userId ?: "")
            .addSnapshotListener { snapshot, error ->
                error?.let {
                    Toast.makeText(
                        requireContext(),
                        "Error fetching user data: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val userModel = it.toObject(User::class.java)
                    deviceToken = userModel?.deviceToken
                }
            }
    }

    private fun sendNotification() {
        AccessToken.getAccessTokenAsync(object : AccessToken.AccessTokenCallback {
            override fun onAccessTokenReceived(token: String?) {
                if (token != null) {
                    val fcm = Fcm()
                    fcm.sendFCMNotification(
                        deviceToken!!,
                        "Admin AI Trust",
                        "Admin replied to your chat!",
                        token
                    )
                }
            }
        })
    }
}
