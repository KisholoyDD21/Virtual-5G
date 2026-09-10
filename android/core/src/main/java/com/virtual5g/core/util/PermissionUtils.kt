package com.virtual5g.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun hasReadPhoneState(context: Context): Boolean =
        granted(context, Manifest.permission.READ_PHONE_STATE)

    /**
     * On API 33+, TelephonyDisplayInfo callbacks can work with the lighter
     * READ_BASIC_PHONE_STATE permission. We still accept full
     * READ_PHONE_STATE (a superset) as sufficient.
     */
    fun hasBasicOrFullPhoneStatePermission(context: Context): Boolean {
        if (hasReadPhoneState(context)) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            granted(context, "android.permission.READ_BASIC_PHONE_STATE")
        } else {
            false
        }
    }

    fun hasFineLocation(context: Context): Boolean =
        granted(context, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun granted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
