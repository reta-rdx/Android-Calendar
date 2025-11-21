package com.example.calendar.util

import com.example.calendar.data.Event
import java.text.SimpleDateFormat
import java.util.*

object ICalImporter {
    private val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
    private val dateOnlyFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
    
    fun importFromICal(content: String): List<Event> {
        val events = mutableListOf<Event>()
        val lines = content.lines()
        
        var inEvent = false
        var currentEvent: MutableMap<String, String>? = null
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine == "BEGIN:VEVENT" -> {
                    inEvent = true
                    currentEvent = mutableMapOf()
                }
                trimmedLine == "END:VEVENT" -> {
                    if (currentEvent != null) {
                        events.add(parseEvent(currentEvent))
                    }
                    inEvent = false
                    currentEvent = null
                }
                inEvent && currentEvent != null -> {
                    val colonIndex = trimmedLine.indexOf(':')
                    if (colonIndex > 0) {
                        val key = trimmedLine.substring(0, colonIndex).uppercase()
                        val value = trimmedLine.substring(colonIndex + 1)
                        currentEvent[key] = value
                    }
                }
            }
        }
        
        return events
    }
    
    private fun parseEvent(eventData: Map<String, String>): Event {
        val title = unescapeText(eventData["SUMMARY"] ?: "")
        val description = unescapeText(eventData["DESCRIPTION"] ?: "")
        val location = unescapeText(eventData["LOCATION"] ?: "")
        
        val dtStart = eventData["DTSTART"] ?: ""
        val dtEnd = eventData["DTEND"] ?: ""
        
        val isAllDay = !dtStart.contains("T")
        val startTime = parseDateTime(dtStart, isAllDay)
        val endTime = parseDateTime(dtEnd, isAllDay)
        
        // 解析提醒时间
        var reminderMinutes = 0
        val alarmTrigger = eventData["TRIGGER"]
        if (alarmTrigger != null && alarmTrigger.startsWith("-PT")) {
            val minutesStr = alarmTrigger.substring(3).replace("M", "")
            reminderMinutes = minutesStr.toIntOrNull() ?: 0
        }
        
        val recurrenceRule = eventData["RRULE"] ?: ""
        
        return Event(
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = location,
            reminderMinutes = reminderMinutes,
            isAllDay = isAllDay,
            recurrenceRule = recurrenceRule
        )
    }
    
    private fun parseDateTime(dateTimeStr: String, isAllDay: Boolean): Long {
        return try {
            if (isAllDay) {
                val cal = Calendar.getInstance()
                val year = dateTimeStr.substring(0, 4).toInt()
                val month = dateTimeStr.substring(4, 6).toInt() - 1
                val day = dateTimeStr.substring(6, 8).toInt()
                cal.set(year, month, day, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            } else {
                dateFormat.parse(dateTimeStr)?.time ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    private fun unescapeText(text: String): String {
        return text.replace("\\n", "\n")
            .replace("\\;", ";")
            .replace("\\,", ",")
            .replace("\\\\", "\\")
    }
}

