package com.example.calendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: Long, // 时间戳（毫秒）
    val endTime: Long, // 时间戳（毫秒）
    val location: String = "",
    val reminderMinutes: Int = 0, // 提醒提前分钟数，0表示不提醒
    val isAllDay: Boolean = false, // 是否全天事件
    val color: Int = 0xFF6200EE.toInt(), // 事件颜色
    val recurrenceRule: String = "", // 重复规则（RFC5545格式，如FREQ=DAILY;INTERVAL=1）
    val timezone: String = "Asia/Shanghai" // 时区
) {
    fun getStartCalendar(): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startTime
        return cal
    }
    
    fun getEndCalendar(): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = endTime
        return cal
    }
}

