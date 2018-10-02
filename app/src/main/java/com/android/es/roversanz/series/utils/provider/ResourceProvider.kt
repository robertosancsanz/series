package com.android.es.roversanz.series.utils.provider

import android.support.annotation.ArrayRes
import android.support.annotation.StringRes

interface ResourceProvider {

    fun getString(@StringRes resource: Int): String

    fun getString(@StringRes resource: Int, vararg args: String): String

    fun getStringArray(@ArrayRes resource: Int): Array<String>

}