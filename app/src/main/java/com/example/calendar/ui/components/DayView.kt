package com.example.calendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.data.Event
import com.example.calendar.util.LunarCalendar
import java.util.Calendar
import java.util.Locale

@Composable
fun DayView(
    currentDate: Calendar,
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    val sortedEvents = events.sortedBy { it.startTime }
    val isToday = isSameDay(currentDate, Calendar.getInstance())
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 日期标题
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isToday) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${currentDate.get(Calendar.YEAR)}年${currentDate.get(Calendar.MONTH) + 1}月${currentDate.get(Calendar.DAY_OF_MONTH)}日",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                val dayOfWeek = (currentDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
                Text(
                    text = weekDays[dayOfWeek],
                    fontSize = 16.sp,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LunarCalendar.getFullLunarInfo(currentDate),
                    fontSize = 14.sp,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        // 事件列表
        if (sortedEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "今天没有日程",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedEvents) { event ->
                    DayEventItem(
                        event = event,
                        onClick = { onEventClick(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun DayEventItem(
    event: Event,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(event.color).copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 颜色指示条
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(event.color))
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = event.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (event.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                if (!event.isAllDay) {
                    val startCal = event.getStartCalendar()
                    val endCal = event.getEndCalendar()
                    val locale = Locale.getDefault()
                    Text(
                        text = "${String.format(locale, "%02d", startCal.get(Calendar.HOUR_OF_DAY))}:${String.format(locale, "%02d", startCal.get(Calendar.MINUTE))} - ${String.format(locale, "%02d", endCal.get(Calendar.HOUR_OF_DAY))}:${String.format(locale, "%02d", endCal.get(Calendar.MINUTE))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = "全天",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                if (event.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "📍 ${event.location}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

