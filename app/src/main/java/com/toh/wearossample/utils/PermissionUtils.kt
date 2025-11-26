package com.toh.wearossample.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {
    fun isPostNotificationsPermissionRequired(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
    }

    fun isPostNotificationsPermissionGranted(context: android.content.Context): Boolean = if (isPostNotificationsPermissionRequired()) {
        PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        true
    }

    fun requestPostNotificationsPermission(activity: Activity, requestCode: Int = 1250) {
        if (isPostNotificationsPermissionRequired()) {
            activity.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), requestCode)
        }
    }
}