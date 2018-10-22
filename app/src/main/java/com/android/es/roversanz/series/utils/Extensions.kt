package com.android.es.roversanz.series.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.widget.Toast

fun Int.toStringPercentage(): String = "${String.format("%.2f", if (this >= 0) this.toDouble() / 100 else 0.0)}%"

fun String?.toIntPercentage(): Int {
    val percentage = (this?.substringBefore("%")
                              ?.replace(",", ".")
                              ?.toDouble() ?: 0.0)
    return (percentage * 100).toInt()
}

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.updateMedia(path: String) {
    MediaScannerConnection.scanFile(this, Array(1) { path }, null, null)
}

