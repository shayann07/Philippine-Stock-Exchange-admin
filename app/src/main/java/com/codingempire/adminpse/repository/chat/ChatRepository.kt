package com.codingempire.adminpse.repository.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.codingempire.adminpse.models.chat.Admin
import com.codingempire.adminpse.models.chat.ChatPreview
import com.codingempire.adminpse.models.chat.Message

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val chatPreviewList: MutableLiveData<List<ChatPreview>> = MutableLiveData()
    private var chatListenerRegistration: ListenerRegistration? = null

    val admin: MutableLiveData<List<Admin>> = MutableLiveData()


    fun fetchAdminList() {
        firestore.collection("Admin")
            .addSnapshotListener { querySnapshot, error ->
                if (querySnapshot != null) {
                    val admins = mutableListOf<Admin>()
                    for (document in querySnapshot.documents) {
                        val admin = document.toObject(Admin::class.java)
                        if (admin != null) {
                            admins.add(admin)
                        }
                    }
                    admin.value = admins
                } else {
                    Log.e("ChatRepository", "Error fetching admin list: ${error?.message}")
                }
            }
    }

    fun getAdmin(): LiveData<List<Admin>> {
        return admin
    }

    fun sendMessage(adminId: String?, messageText: String?, userId: String?) {
        val message = Message(
            id = "",
            message = messageText ?: "",
            senderId = adminId ?: "",
            receiverId = userId ?: "",
            status = Message.STATUS_SENT,
            sender = "2" // "2" means admin is sending
        )
        firestore.collection("chats").add(message).addOnSuccessListener { documentReference ->
            val id = documentReference.id
            firestore.collection("chats").document(id).update("id", id)
            Log.d("MessageRepository", "Admin Message sent successfully")
        }.addOnFailureListener { e ->
            Log.e("MessageRepository", "Error sending admin message", e)
        }
    }

    fun getMessages(userId: String?, messagesLiveData: MutableLiveData<List<Message>>) {
        firestore.collection("chats")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->

                if (e != null) {
                    Log.e("ChatRepository", "Error fetching messages: ", e)
                    messagesLiveData.postValue(emptyList())
                    return@addSnapshotListener
                }

                val messageList: MutableList<Message?> = ArrayList()

                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val message: Message? = document.toObject(Message::class.java)
                        if (message != null) {
                            if (message.senderId == userId || message.receiverId == userId) {
                                messageList.add(message)
                            }
                        }
                    }
                }

                messageList.sortBy { it?.createdAt?.toDate() }
                messagesLiveData.postValue(messageList.filterNotNull())
            }
    }


    fun getChats(userId: String?, adminId: String): LiveData<List<Message>> {
        val chats: MutableLiveData<List<Message>> = MutableLiveData()

        val chatList = mutableListOf<Message>()
        val messageIds = mutableSetOf<String>()

        firestore.collection("chats")
            .whereIn("senderId", listOf(userId, adminId))
            .whereIn("receiverId", listOf(userId, adminId))
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) return@addSnapshotListener

                chatList.clear()
                messageIds.clear()

                querySnapshot?.documents?.forEach { document ->
                    val message = document.toObject(Message::class.java)
                    message?.let {
                        if (!messageIds.contains(it.id)) {
                            chatList.add(it)
                            messageIds.add(it.id)
                        }
                    }
                }
                chatList.sortBy { it.createdAt?.toDate() }
                chats.postValue(chatList)
            }

        return chats
    }


    fun getChatPreviewList(): LiveData<List<ChatPreview>> {
        fetchChatPreviewList()
        return chatPreviewList
    }

    fun fetchChatPreviewList() {
        chatListenerRegistration?.remove()

        chatListenerRegistration = firestore.collection("chats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatPreview", "Error fetching chats", error)
                    return@addSnapshotListener
                }

                val chatPreviews = mutableMapOf<String, ChatPreview>()

                Log.d("ChatPreview", "Snapshot: ${snapshot?.size()}")
                val adminId = "AHvCIjxEC0bcMMNjM0UzDNSeSHg1"

                snapshot?.documents?.forEach { document ->
                    val senderId = document.getString("senderId") ?: return@forEach
                    val receiverId = document.getString("receiverId") ?: return@forEach
                    val message = document.getString("message") ?: "No Message"
                    val timestamp =
                        (document["createdAt"] as? Timestamp)?.toDate()?.time ?: return@forEach

                    // The other person in the chat (user)
                    val otherId = if (senderId == adminId) receiverId else senderId
                    if (otherId == adminId) return@forEach // just in case

                    val existingChat = chatPreviews[otherId]
                    if (existingChat == null || timestamp > existingChat.timestamp) {
                        chatPreviews[otherId] =
                            ChatPreview(otherId, "Fetching...", message, timestamp)
                    }
                }
                fetchUserNames(chatPreviews)
            }
    }

    private fun fetchUserNames(chatPreviews: MutableMap<String, ChatPreview>) {
        val userIds = chatPreviews.keys.toList()

        if (userIds.isEmpty()) {
            chatPreviewList.value = emptyList()
            return
        }

        firestore.collection("users")
            .whereIn("docId", userIds)
            .get()
            .addOnSuccessListener { userSnapshot ->
                userSnapshot.documents.forEach { doc ->
                    val userId = doc.getString("docId") ?: return@forEach
                    val userName = doc.getString("name") ?: "Unknown"
                    val timestamp =
                        (doc["createdAt"] as? Timestamp)?.toDate()?.time ?: return@forEach

                    chatPreviews[userId]?.let {
                        chatPreviews[userId] = it.copy(userName = userName)
                    }
                }
                chatPreviewList.value = chatPreviews.values.sortedByDescending { it.timestamp }
            }
    }
}