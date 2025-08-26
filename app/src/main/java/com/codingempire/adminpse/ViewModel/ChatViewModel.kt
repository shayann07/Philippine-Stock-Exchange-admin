package com.codingempire.adminpse.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codingempire.adminpse.models.chat.Admin
import com.codingempire.adminpse.models.chat.ChatPreview
import com.codingempire.adminpse.models.chat.Message
import com.codingempire.adminpse.repository.chat.ChatRepository

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _messagesLiveData = MutableLiveData<List<Message>>()
    val messagesLiveData: LiveData<List<Message>> get() = _messagesLiveData

    @JvmName("fetchChatPreviewList")
    fun getChatPreviewList(): LiveData<List<ChatPreview>> {
        return chatRepository.getChatPreviewList()

    }

    fun getAdmin(): LiveData<List<Admin>> {
        return chatRepository.getAdmin()
    }

    fun getMessages(userId: String): LiveData<List<Message>> {
        chatRepository.getMessages(userId, _messagesLiveData)
        return messagesLiveData
    }

    fun getChats(userId: String, adminId: String): LiveData<List<Message>> {
        return chatRepository.getChats(userId, adminId)
    }

    fun sendMessage(receiverId: String, text: String, userId: String) {
        chatRepository.sendMessage(receiverId, text, userId)
    }
}
