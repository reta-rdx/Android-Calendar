package com.example.calendar.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.calendar.MainActivity
import com.example.calendar.R

object NotificationHelper {
    const val CHANNEL_ID = "event_reminder_channel"
    const val CHANNEL_NAME = "日程提醒"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "日程提醒通知"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 发送事件提醒通知
     */
    fun sendEventNotification(
        context: Context,
        eventTitle: String,
        eventDescription: String? = null,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        // 检查通知权限
        if (!PermissionChecker.hasNotificationPermission(context)) {
            android.util.Log.w("NotificationHelper", "没有通知权限，无法发送通知")
            return
        }
        
        // 创建点击通知时的Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 确保有这个图标
            .setContentTitle(eventTitle)
            .setContentText(eventDescription ?: "您有一个日程提醒")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "发送通知失败: ${e.message}")
        }
    }
    
    /**
     * 检查通知权限状态
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                PermissionChecker.hasNotificationPermission(context)
    }
}

