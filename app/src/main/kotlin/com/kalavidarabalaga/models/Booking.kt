package com.kalavidarabalaga.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Booking(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val troupeId: String = "",
    val troupeName: String = "",
    val troupeLeaderId: String = "",
    val eventDate: String = "",
    val message: String = "",
    val status: String = "pending",
    @Transient val createdAt: Timestamp? = null
) : Parcelable
