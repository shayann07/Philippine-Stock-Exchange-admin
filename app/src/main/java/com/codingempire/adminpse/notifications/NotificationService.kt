package com.codingempire.adminpse.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.codingempire.adminpse.R
import com.codingempire.adminpse.models.NotificationItem
import com.codingempire.adminpse.ui.MainActivity
import com.codingempire.adminpse.utils.SharedPrefManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class NotificationService : FirebaseMessagingService() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", MODE_PRIVATE)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val id = sharedPreferences.getString("id", null)
        if (!id.isNullOrEmpty()) {
            firestore.collection("users").document(id).update("deviceToken", token)
        } else {
            Log.e("NotificationService", "User ID not found in SharedPreferences")
        }
    }

    private val channelId = "AssignId"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("MYTAG", "onMessageReceived: ${message.data}")

        val title = message.data["title"] ?: "No Title"
        val body = message.data["body"] ?: "No Body"
        val newNotification = NotificationItem(title, body)

        val sharedPrefManager = SharedPrefManager(applicationContext)
        val currentList = sharedPrefManager.getNotifications().toMutableList()
        currentList.add(0, newNotification)
        sharedPrefManager.saveNotifications(currentList)


        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        manager?.let {
            createNotificationChannel(it)
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["body"])
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .build()
            it.notify(Random.nextInt(), notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(manager: NotificationManager) {
        val channel =
            NotificationChannel(channelId, "assignwork", NotificationManager.IMPORTANCE_HIGH)
                .apply {
                    description = "New work"
                    enableLights(true)
                }
        manager.createNotificationChannel(channel)
    }
}
