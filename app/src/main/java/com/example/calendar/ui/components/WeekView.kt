package com.example.calendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.calendar.util.CalendarUtils.isSameDay
import com.example.calendar.util.LunarCalendarUtil
import java.util.Calendar
import java.util.Locale

@Composable
fun WeekView(
    currentDate: Calendar,
    events: List<Event>,
    onDateClick: (Calendar) -> Unit,
    onEventClick: (Event) -> Unit
) {
    val calendar = currentDate.clone() as Calendar
    
    // 获取当前周的第一天（周日）
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysFromSunday = dayOfWeek - Calendar.SUNDAY
    calendar.add(Calendar.DAY_OF_MONTH, -daysFromSunday)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // 星期标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(7) { dayOffset ->
                val dayCalendar = calendar.clone() as Calendar
                dayCalendar.add(Calendar.DAY_OF_MONTH, dayOffset)
                
                val isToday = isSameDay(dayCalendar, Calendar.getInstance())
                val dayEvents = events.filter { 
                    isSameDay(it.getStartCalendar(), dayCalendar) 
                }
                
                WeekDayColumn(
                    dayCalendar = dayCalendar,
                    isToday = isToday,
                    events = dayEvents,
                    onClick = { onDateClick(dayCalendar) },
                    onEventClick = onEventClick
                )
            }
        }
    }
}

@Composable
fun RowScope.WeekDayColumn(
    dayCalendar: Calendar,
    isToday: Boolean,
    events: List<Event>,
    onClick: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val weekDays = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val dayOfWeek = dayCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        
        // 星期标题
        Text(
            text = weekDays[dayOfWeek],
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 日期和农历（紧贴）
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString(),
                fontSize = 24.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            
            // 农历日期（紧贴日期）
            Text(
                text = LunarCalendarUtil.getSimpleLunarDate(dayCalendar),
                fontSize = 8.sp,
                color = if (isToday) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 显示事件
        events.forEach { event ->
            EventItem(
                event = event,
                onClick = { onEventClick(event) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun EventItem(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(event.color).copy(alpha = 0.7f))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Column {
            Text(
                text = event.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
            if (!event.isAllDay) {
                val startCal = event.getStartCalendar()
                Text(
                    text = "${startCal.get(Calendar.HOUR_OF_DAY)}:${String.format(Locale.getDefault(), "%02d", startCal.get(Calendar.MINUTE))}",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

