package com.example.calendar.util

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * 权限检查工具类，提供便捷的权限检查方法
 */
object PermissionChecker {
    
    /**
     * 检查通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return PermissionHelper.hasNotificationPermission(context)
    }
    
    /**
     * 检查文件权限
     */
    fun hasFilePermission(context: Context): Boolean {
        return PermissionHelper.hasFilePermission(context)
    }
    
    /**
     * 检查精确闹钟权限
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        return PermissionHelper.hasExactAlarmPermission(context)
    }
    
    /**
     * 获取缺失的关键权限
     */
    fun getMissingCriticalPermissions(context: Context): List<String> {
        val missing = mutableListOf<String>()
        
        if (!hasNotificationPermission(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                missing.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (!hasFilePermission(context)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                missing.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        return missing
    }
}

/**
 * Composable 函数，用于检查权限状态
 */
@Composable
fun rememberPermissionState(permission: String): Boolean {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    
    LaunchedEffect(permission) {
        hasPermission = PermissionHelper.hasPermission(context, permission)
    }
    
    return hasPermission
}