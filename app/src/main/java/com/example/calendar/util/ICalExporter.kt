package com.example.calendar.util

import com.example.calendar.data.Event
import java.text.SimpleDateFormat
import java.util.*

object ICalExporter {
    private val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
    
    fun exportToICal(events: List<Event>): String {
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//Calendar App//EN")
        sb.appendLine("CALSCALE:GREGORIAN")
        sb.appendLine("METHOD:PUBLISH")
        
        events.forEach { event ->
            sb.appendLine("BEGIN:VEVENT")
            sb.appendLine("UID:${event.id}@calendar.app")
            sb.appendLine("DTSTART:${formatDateTime(event.startTime, event.isAllDay)}")
            sb.appendLine("DTEND:${formatDateTime(event.endTime, event.isAllDay)}")
            sb.appendLine("SUMMARY:${escapeText(event.title)}")
            if (event.description.isNotEmpty()) {
                sb.appendLine("DESCRIPTION:${escapeText(event.description)}")
            }
            if (event.location.isNotEmpty()) {
                sb.appendLine("LOCATION:${escapeText(event.location)}")
            }
            if (event.reminderMinutes > 0) {
                sb.appendLine("BEGIN:VALARM")
                sb.appendLine("TRIGGER:-PT${event.reminderMinutes}M")
                sb.appendLine("ACTION:DISPLAY")
                sb.appendLine("DESCRIPTION:${escapeText(event.title)}")
                sb.appendLine("END:VALARM")
            }
            if (event.recurrenceRule.isNotEmpty()) {
                sb.appendLine("RRULE:${event.recurrenceRule}")
            }
            sb.appendLine("END:VEVENT")
        }
        
        sb.appendLine("END:VCALENDAR")
        return sb.toString()
    }
    
    private fun formatDateTime(timestamp: Long, isAllDay: Boolean): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        
        return if (isAllDay) {
            String.format(
                Locale.US,
                "%04d%02d%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        } else {
            dateFormat.format(Date(timestamp))
        }
    }
    
    private fun escapeText(text: String): String {
        return text.replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
    }
}

