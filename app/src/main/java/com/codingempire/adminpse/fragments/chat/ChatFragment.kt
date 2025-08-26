package com.codingempire.adminpse.fragments.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingempire.adminpse.R
import com.codingempire.adminpse.ViewModel.ChatViewModel
import com.codingempire.adminpse.databinding.FragmentChatBinding
import com.codingempire.adminpse.models.chat.ChatPreview
import com.codingempire.adminpse.repository.chat.ChatRepository
import com.codingempire.adminpse.repository.chat.ChatViewModelFactory
import com.codingempire.adminpse.adapter.chat.ChatPreviewAdapter

class ChatFragment : Fragment(), ChatPreviewAdapter.OnChatPreviewClickListener {
    private var chatViewModel: ChatViewModel? = null
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var adapter: ChatPreviewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.chatListRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ChatPreviewAdapter(ArrayList(), this)
        binding.chatListRecyclerView.adapter = adapter

        val chatRepository = ChatRepository()
        val factory = ChatViewModelFactory(chatRepository)

        chatViewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        chatViewModel!!.getChatPreviewList().observe(viewLifecycleOwner) { chatPreviews ->
            for (preview in chatPreviews) {
                Log.d("ChatFragment", "ChatPreview User ID: ${preview.getUserId()}")
            }
            adapter!!.setChatPreviews(chatPreviews)
        }
        return binding.root
    }

    override fun onChatPreviewClick(chatPreview : ChatPreview) {
        val bundle = Bundle().apply {
            putString("userId", chatPreview.userId)
            putString("userName", chatPreview.userName)
        }
        findNavController().navigate(R.id.action_chatFragment_to_detailChatFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}