package com.android.es.roversanz.series.utils

import android.app.Activity
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.es.roversanz.series.presentation.MyApplication

fun Activity.app() = this.application as? MyApplication
                     ?: throw ClassCastException("The Application should be ${MyApplication::class.java.simpleName}")

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View
        = LayoutInflater.from(context).inflate(layoutRes, this, false)
