package com.android.es.roversanz.series.utils

import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast

fun Int.toPercentage(): String = "${String.format("%.2f", if (this >= 0) this.toDouble() / 100 else 0.0)}%"

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

inline fun View.snack(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit)
        = snack(resources.getString(messageRes), length, f)

fun View.snack(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_LONG)
        = snack(resources.getString(messageRes), length)

inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
    val snack = Snackbar.make(this, message, length)
    snack.f()
    snack.show()
}

fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG) {
    val snack = Snackbar.make(this, message, length)
    snack.show()
}

fun Snackbar.action(@StringRes actionRes: Int, color: Int? = null, listener: (View) -> Unit) {
    action(view.resources.getString(actionRes), color, listener)
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(color) }
}
