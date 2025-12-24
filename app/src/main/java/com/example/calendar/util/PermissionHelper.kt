package com.example.calendar.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    // 权限请求码
    const val PERMISSION_REQUEST_CODE = 1001
    
    // 核心权限列表 - 只包含必需的权限
    val CORE_PERMISSIONS = buildList {
        // 通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // 文件权限 - 仅用于导入导出日历文件
        // Android 13+ 不再需要文件权限来访问应用自己的文件
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 检查是否已授予所有核心权限
     */
    fun hasAllCorePermissions(context: Context): Boolean {
        return CORE_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 检查特定权限是否已授予
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        // 特殊处理精确闹钟权限
        if (permission == Manifest.permission.SCHEDULE_EXACT_ALARM) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else {
                true // Android 12 以下不需要此权限
            }
        }
        
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 获取未授予的核心权限列表
     */
    fun getDeniedCorePermissions(context: Context): List<String> {
        return CORE_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 请求核心权限 - 使用Android原生对话框
     */
    fun requestCorePermissions(activity: Activity) {
        val deniedPermissions = getDeniedCorePermissions(activity)
        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                deniedPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * 检查通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true // Android 13 以下不需要运行时权限
        }
    }
    
    /**
     * 检查文件权限
     */
    fun hasFilePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 不需要权限来访问应用自己的文件
            true
        } else {
            hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 检查精确闹钟权限
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM)
    }
    
    /**
     * 获取权限的友好名称
     */
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.POST_NOTIFICATIONS -> "通知权限"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "文件读取权限"
            Manifest.permission.SCHEDULE_EXACT_ALARM -> "精确闹钟权限"
            else -> permission
        }
    }
    
    /**
     * 获取权限说明文本
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.POST_NOTIFICATIONS -> "用于发送事件提醒通知"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "用于导入导出日历文件"
            Manifest.permission.SCHEDULE_EXACT_ALARM -> "用于设置精确的事件提醒"
            else -> "应用正常运行所需的权限"
        }
    }
}