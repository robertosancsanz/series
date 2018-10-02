package com.android.es.roversanz.series.utils

import android.app.Activity
import com.android.es.roversanz.series.presentation.MyApplication

fun Activity.app() = this.application as? MyApplication
        ?: throw ClassCastException("The Application should be ${MyApplication::class.java.simpleName}")
