package com.example.tffapp.util

import android.app.Activity
import androidx.navigation.Navigation
import com.example.tffapp.R
import com.example.tffapp.feature.display.PermissionsFragment

object PermissionUtil {
    fun checkPermission(activity: Activity) {
        if (!PermissionsFragment.hasPermissions(activity)) {
            Navigation.findNavController(activity, R.id.fragment_container)
                .navigate(R.id.action_camera_to_permissions)
        }
    }
}