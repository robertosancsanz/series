package com.android.es.roversanz.series.utils.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

class PermissionHandler {

    companion object {

        val REQUEST_PERMISSION_CAMERA = 20
        val REQUEST_PERMISSION_LOCATION = 30
        val REQUEST_PERMISSION_PHONE = 40
        val REQUEST_PERMISSION_STORAGE = 50
        val REQUEST_PERMISSION_CONTACT = 60

        fun isPermissionGranted(
                permissionToCheck: Permission,
                requestCode: Int,
                grantResults: IntArray): Boolean = (requestCode == permissionToCheck.requestId
                                                    && grantResults.isNotEmpty()
                                                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)

        fun isPermissionDenied(
                permissionToCheck: Permission,
                requestCode: Int,
                grantResults: IntArray): Boolean = (requestCode == permissionToCheck.requestId
                                                    && grantResults.isNotEmpty()
                                                    && grantResults[0] == PackageManager.PERMISSION_DENIED)


        fun getPermissionFromRequestCode(requestCode: Int): Permission? = when (requestCode) {
            Permission.CAMERA.requestId   -> Permission.CAMERA
            Permission.LOCATION.requestId -> Permission.LOCATION
            Permission.PHONE.requestId    -> Permission.PHONE
            Permission.STORAGE.requestId  -> Permission.STORAGE
            Permission.CONTACT.requestId  -> Permission.CONTACT
            else                          -> null
        }
    }
}

/**
 * Check if permission is already given or not.
 *
 * @param context    [Context]
 * @param permission [Permission]
 * @return [Boolean]
 */

fun Context?.hasPermission(permission: Permission): Boolean = this?.let {
    ContextCompat.checkSelfPermission(this, permission.value) == PackageManager.PERMISSION_GRANTED
} ?: false


private fun Activity.requestPermission(permission: Permission) = ActivityCompat.requestPermissions(this, arrayOf(permission.value), permission.requestId)


fun Fragment.onRequestPermission(permission: Permission) = requestPermissions(arrayOf(permission.value), permission.requestId)
