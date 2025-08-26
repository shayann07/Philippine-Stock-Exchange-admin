package com.codingempire.adminpse.models

import com.google.firebase.Timestamp

data class Announcement(
    val id: String = "",
    val announcement: String = "",
    val message: String = "",
    val time: Timestamp = Timestamp.now()
)
