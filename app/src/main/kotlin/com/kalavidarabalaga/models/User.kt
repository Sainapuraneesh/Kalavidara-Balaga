package com.kalavidarabalaga.models

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "",
    val createdAt: Timestamp? = null
)
