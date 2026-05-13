package com.kalavidarabalaga.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Troupe(
    val id: String = "",
    val name: String = "",
    val artForm: String = "",
    val district: String = "",
    val leadContactName: String = "",
    val leadContactPhone: String = "",
    val groupPhotoUrl: String = "",
    val portfolioImages: List<String> = emptyList(),
    val portfolioVideoLinks: List<String> = emptyList(),
    val equipmentList: List<String> = emptyList(),
    val serviceArea: List<String> = emptyList(),
    val description: String = "",
    val isActive: Boolean = true,
    val ownerId: String = "",
    @Transient val createdAt: Timestamp? = null
) : Parcelable
