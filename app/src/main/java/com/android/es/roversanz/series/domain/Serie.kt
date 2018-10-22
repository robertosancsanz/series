package com.android.es.roversanz.series.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Serie(
        val id: Int,
        val title: String,
        val subtitle: String,
        val description: String,
        val picture: String,
        val downloadUrl: String,
        var file: String? = null
) : Parcelable
