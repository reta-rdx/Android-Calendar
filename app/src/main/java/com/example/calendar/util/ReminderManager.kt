package com.example.calendar.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.calendar.data.Event
import com.example.calendar.receiver.EventReminderReceiver

object ReminderManager {
    private const val REQUEST_CODE_PREFIX = 1000
    
    fun scheduleReminder(context: Context, event: Event) {
        if (event.reminderMinutes <= 0) return
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderTime = event.startTime - (event.reminderMinutes * 60 * 1000L)
        
        // 如果提醒时间已过，不设置提醒
        if (reminderTime <= System.currentTimeMillis()) return
        
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra("event_id", event.id)
            putExtra("event_title", event.title)
            putExtra("event_description", event.description)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_PREFIX + event.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
        )
    }
    
    fun cancelReminder(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EventReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_PREFIX + event.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

