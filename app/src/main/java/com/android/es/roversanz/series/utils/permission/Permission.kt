package com.android.es.roversanz.series.utils.permission

import android.Manifest

enum class Permission private constructor(
        val value: String,
        val requestId: Int) {

    /**
     * Camera permission group.
     */
    CAMERA(Manifest.permission.CAMERA, PermissionHandler.REQUEST_PERMISSION_CAMERA),

    /**
     * Location permission group.
     */
    LOCATION(Manifest.permission.ACCESS_FINE_LOCATION, PermissionHandler.REQUEST_PERMISSION_LOCATION),

    /**
     * Phonen permission group.
     */
    PHONE(Manifest.permission.CALL_PHONE, PermissionHandler.REQUEST_PERMISSION_PHONE),

    /**
     * Storage permission group.
     */
    STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE, PermissionHandler.REQUEST_PERMISSION_STORAGE),

    /**
     * Contact permission group.
     */
    CONTACT(Manifest.permission.READ_CONTACTS, PermissionHandler.REQUEST_PERMISSION_CONTACT)
}