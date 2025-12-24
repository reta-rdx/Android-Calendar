package com.example.calendar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.calendar.AlarmActivity
import com.example.calendar.util.NotificationHelper
import com.example.calendar.util.PermissionChecker

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("EventReminderReceiver", "收到事件提醒广播")
        
        val eventId = intent.getLongExtra("event_id", -1)
        val eventTitle = intent.getStringExtra("event_title") ?: "日程提醒"
        val eventDescription = intent.getStringExtra("event_description")
        val eventTime = intent.getLongExtra("event_time", System.currentTimeMillis())
        val isAlarm = intent.getBooleanExtra("is_alarm", false)
        val isSnooze = intent.getBooleanExtra("is_snooze", false)
        
        Log.d("EventReminderReceiver", "事件信息: ID=$eventId, 标题=$eventTitle, 是否闹钟=$isAlarm, 是否稍后提醒=$isSnooze")
        
        if (isAlarm) {
            // 启动全屏闹钟界面
            val alarmIntent = AlarmActivity.createIntent(
                context = context,
                eventId = eventId,
                eventTitle = eventTitle,
                eventDescription = eventDescription,
                eventTime = eventTime
            )
            context.startActivity(alarmIntent)
            Log.d("EventReminderReceiver", "已启动闹钟界面")
        } else {
            // 发送普通通知
            // 确保通知渠道已创建
            NotificationHelper.createNotificationChannel(context)
            
            // 检查通知权限
            if (!PermissionChecker.hasNotificationPermission(context)) {
                Log.w("EventReminderReceiver", "没有通知权限，无法发送提醒")
                return
            }
            
            // 发送通知
            NotificationHelper.sendEventNotification(
                context = context,
                eventTitle = eventTitle,
                eventDescription = eventDescription,
                notificationId = eventId.toInt()
            )
            
            Log.d("EventReminderReceiver", "事件提醒通知已发送")
        }
    }
}

