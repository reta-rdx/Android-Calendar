package com.example.calendar.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.calendar.data.Event
import com.example.calendar.receiver.EventReminderReceiver

object ReminderManager {
    private const val REQUEST_CODE_PREFIX = 1000
    private const val SNOOZE_REQUEST_CODE_PREFIX = 2000
    
    fun scheduleReminder(context: Context, event: Event) {
        if (event.reminderMinutes == 0) return
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val absReminderMinutes = kotlin.math.abs(event.reminderMinutes)
        val reminderTime = event.startTime - (absReminderMinutes * 60 * 1000L)
        
        // 如果提醒时间已过，不设置提醒
        if (reminderTime <= System.currentTimeMillis()) {
            Log.d("ReminderManager", "提醒时间已过，不设置提醒")
            return
        }
        
        // 判断提醒类型：正数为闹钟提醒，负数为通知提醒
        val isAlarmReminder = event.reminderMinutes > 0
        
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra("event_id", event.id)
            putExtra("event_title", event.title)
            putExtra("event_description", event.description)
            putExtra("event_time", event.startTime)
            putExtra("is_alarm", isAlarmReminder) // 标记为闹钟提醒或通知提醒
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_PREFIX + event.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 检查是否有精确闹钟权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
                Log.d("ReminderManager", "已设置${if (isAlarmReminder) "闹钟" else "通知"}提醒")
            } else {
                // 如果没有精确闹钟权限，使用普通闹钟
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
                Log.d("ReminderManager", "已设置普通${if (isAlarmReminder) "闹钟" else "通知"}提醒（无精确权限）")
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
            Log.d("ReminderManager", "已设置${if (isAlarmReminder) "闹钟" else "通知"}提醒")
        }
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
        Log.d("ReminderManager", "已取消闹钟提醒")
    }
    
    /**
     * 设置稍后提醒（贪睡功能）
     */
    fun scheduleSnoozeReminder(
        context: Context,
        eventId: Long,
        eventTitle: String,
        eventDescription: String?,
        eventTime: Long,
        snoozeMinutes: Int = 5
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra("event_id", eventId)
            putExtra("event_title", eventTitle)
            putExtra("event_description", eventDescription)
            putExtra("event_time", eventTime)
            putExtra("is_alarm", true)
            putExtra("is_snooze", true)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (SNOOZE_REQUEST_CODE_PREFIX + eventId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 设置稍后提醒
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
            )
        }
        
        Log.d("ReminderManager", "已设置${snoozeMinutes}分钟后的稍后提醒")
    }
    
    /**
     * 取消稍后提醒
     */
    fun cancelSnoozeReminder(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EventReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (SNOOZE_REQUEST_CODE_PREFIX + eventId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("ReminderManager", "已取消稍后提醒")
    }
}

