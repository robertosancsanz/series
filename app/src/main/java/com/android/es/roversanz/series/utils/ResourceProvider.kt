package com.android.es.roversanz.series.utils

import android.content.Context
import android.support.annotation.ArrayRes
import android.support.annotation.StringRes

interface ResourceProvider {

    fun getString(@StringRes resource: Int): String

    fun getString(@StringRes resource: Int, vararg args: String): String

    fun getStringArray(@ArrayRes resource: Int): Array<String>

}

class ResourceProviderImpl(private val context: Context) : ResourceProvider {

    override fun getString(@StringRes resource: Int): String = context.getString(resource)

    override fun getString(@StringRes resource: Int, vararg args: String): String = context.getString(resource, args)

    override fun getStringArray(@ArrayRes resource: Int): Array<String> = context.resources.getStringArray(resource)

}
