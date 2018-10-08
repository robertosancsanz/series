package com.android.es.roversanz.series.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Serie(
        val id: Long,
        val title: String,
        val subtitle: String,
        val description: String,
        val stars: Double,
        val picture: String
) : Parcelable
