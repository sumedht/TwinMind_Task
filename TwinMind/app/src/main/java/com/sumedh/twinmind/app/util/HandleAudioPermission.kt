package com.sumedh.twinmind.app.util

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandleAudioPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    when {
        permissionState.status.isGranted -> {
            onPermissionGranted()
        }
        permissionState.status.shouldShowRationale -> {
            // This is the case where the user has denied the permission at least once.
            // Here you could show a dialog explaining why you need the permission.
            // For simplicity, we'll treat it as denied for now.
            onPermissionDenied()
        }
        else -> {
            // If the user has permanently denied the permission, this will be the state.
            // You might want to guide them to the app settings.
            // For now, we also treat this as denied.
        }
    }
}