package com.android.es.roversanz.series.utils

fun Int.toPercentage(): String = "${String.format("%.2f", if (this >= 0) this.toDouble() / 100 else 0.0)}%"
