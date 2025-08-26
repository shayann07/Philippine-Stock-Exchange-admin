package com.codingempire.adminpse.repository.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codingempire.adminpse.ViewModel.ChatViewModel
import kotlin.jvm.java


class ChatViewModelFactory(private val chatRepository: ChatRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
