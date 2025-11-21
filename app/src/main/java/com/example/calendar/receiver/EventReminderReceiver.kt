package com.example.calendar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.calendar.util.NotificationHelper

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createNotificationChannel(context)
        
        val eventId = intent.getLongExtra("event_id", -1)
        val eventTitle = intent.getStringExtra("event_title") ?: "日程提醒"
        val eventDescription = intent.getStringExtra("event_description") ?: ""
        
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(eventTitle)
            .setContentText(eventDescription.ifEmpty { "您有一个即将开始的日程" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        
        // 检查通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(eventId.toInt(), notification)
            }
        } else {
            // Android 13 以下版本不需要权限检查
            notificationManager.notify(eventId.toInt(), notification)
        }
    }
}

