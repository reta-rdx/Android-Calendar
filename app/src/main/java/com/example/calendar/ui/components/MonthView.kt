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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.data.Event
import java.util.Calendar

@Composable
fun MonthView(
    currentDate: Calendar,
    events: List<Event>,
    onDateClick: (Calendar) -> Unit,
    onEventClick: (Event) -> Unit
) {
    val calendar = currentDate.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // 计算需要显示的天数（包括上个月和下个月的日期）
    val startOffset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
    val totalDays = startOffset + daysInMonth
    val weeks = (totalDays + 6) / 7
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 星期标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 日期网格
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(weeks) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { dayIndex ->
                        val dayNumber = week * 7 + dayIndex - startOffset + 1
                        val dayCalendar = calendar.clone() as Calendar
                        
                        if (dayNumber <= 0) {
                            // 上个月的日期
                            dayCalendar.add(Calendar.MONTH, -1)
                            dayCalendar.set(Calendar.DAY_OF_MONTH, 
                                dayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) + dayNumber)
                        } else if (dayNumber > daysInMonth) {
                            // 下个月的日期
                            dayCalendar.add(Calendar.MONTH, 1)
                            dayCalendar.set(Calendar.DAY_OF_MONTH, dayNumber - daysInMonth)
                        } else {
                            dayCalendar.set(Calendar.DAY_OF_MONTH, dayNumber)
                        }
                        
                        val isCurrentMonth = dayNumber > 0 && dayNumber <= daysInMonth
                        val isToday = isSameDay(dayCalendar, Calendar.getInstance())
                        val dayEvents = events.filter { 
                            isSameDay(it.getStartCalendar(), dayCalendar) 
                        }
                        
                        DayCell(
                            dayNumber = if (dayNumber > 0 && dayNumber <= daysInMonth) dayNumber else null,
                            isCurrentMonth = isCurrentMonth,
                            isToday = isToday,
                            events = dayEvents,
                            onClick = { onDateClick(dayCalendar) },
                            onEventClick = onEventClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun RowScope.DayCell(
    dayNumber: Int?,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    events: List<Event>,
    onClick: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayNumber?.toString() ?: "",
                fontSize = 14.sp,
                color = if (isCurrentMonth) {
                    if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // 显示事件指示器
            events.take(3).forEach { event ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(event.color))
                        .clickable { onEventClick(event) }
                )
            }
            
            if (events.size > 3) {
                Text(
                    text = "+${events.size - 3}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

