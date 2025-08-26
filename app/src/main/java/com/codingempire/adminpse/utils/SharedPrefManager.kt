package com.codingempire.adminpse.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.codingempire.adminpse.models.NotificationItem
import com.codingempire.adminpse.models.UserModel
import com.codingempire.adminpse.utils.Constants


class SharedPrefManager(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(Constants.PREFERENCE, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPref.edit()
    private val gson = Gson()

    fun saveId(id: String) {
        editor.putString("userId", id)
        editor.apply()
    }
    fun saveUserName(name: String) {
        editor.putString("userName", name)
        editor.apply()
    }

    fun saveUserEmail(email: String) {
        editor.putString("userEmail", email)
        editor.apply()
    }
    fun getEmail(): String? {
        return sharedPref.getString("userEmail", null)
    }

    fun getId(): String? {
        return sharedPref.getString("userId", null)
    }

    fun saveLogin() {
        editor.putString(Constants.LOGIN, "login").apply()
    }

    fun checkLogin(): Boolean = sharedPref.getString(Constants.LOGIN, null) == "login"

    fun saveNotifications(list: List<NotificationItem>) {
        val json = gson.toJson(list)
        editor.putString("notification_list", json)
        editor.apply()
    }

    fun getNotifications(): List<NotificationItem> {
        val json = sharedPref.getString("notification_list", null)
        val type = object : TypeToken<List<NotificationItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveUsers(list: List<UserModel>) {
        val json = gson.toJson(list)
        editor.putString("users_list", json)
        editor.apply()
    }

    fun getUsers(): List<UserModel> {
        val json = sharedPref.getString("users_list", null)
        val type = object : TypeToken<List<NotificationItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    fun clearUserData() {
        editor.apply()
    }



}
