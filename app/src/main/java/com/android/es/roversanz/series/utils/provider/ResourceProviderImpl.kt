package com.android.es.roversanz.series.utils.provider

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.annotation.ArrayRes
import android.support.annotation.ColorRes
import android.support.annotation.StringRes

class ResourceProviderImpl(private val context: Context) : ResourceProvider {

    override fun getString(@StringRes resource: Int): String = context.getString(resource)

    override fun getString(@StringRes resource: Int, vararg args: String): String = context.getString(resource, args)

    override fun getStringArray(@ArrayRes resource: Int): Array<String> = context.resources.getStringArray(resource)

    override fun getColor(@ColorRes color: Int): Int = if (VERSION.SDK_INT >= VERSION_CODES.M) {
        context.resources.getColor(color, context.theme)
    } else {
        context.resources.getColor(color)
    }
}
