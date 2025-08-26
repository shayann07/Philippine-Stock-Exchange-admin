package com.codingempire.adminpse.models

import com.google.firebase.Timestamp


data class ImageAnnouncement(
    val id: String = "",
    val imageUrl: String = "",
    val time: Timestamp? = null
)
