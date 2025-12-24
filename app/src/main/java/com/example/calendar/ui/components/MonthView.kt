package com.example.calendar.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.calendar.data.Event
import com.example.calendar.util.CalendarUtils.isSameDay
import com.example.calendar.util.LunarCalendarUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// 缓存农历数据的数据类
data class LunarInfo(
    val lunarText: String,
    val solarTerm: String?, // 节气信息
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)

// 全局农历计算缓存和预加载管理器
object LunarPreloadManager {
    private val lunarCache = mutableMapOf<String, String>()
    private val solarTermCache = mutableMapOf<String, String?>()
    private val preloadedMonths = mutableSetOf<String>()
    
    // 清理缓存的函数（避免内存泄漏）
    fun clearOldCache() {
        if (lunarCache.size > 300) { // 增加缓存容量
            lunarCache.clear()
            preloadedMonths.clear()
        }
        if (solarTermCache.size > 300) {
            solarTermCache.clear()
        }
    }
    
    // 检查月份是否已预加载
    fun isMonthPreloaded(year: Int, month: Int): Boolean {
        return preloadedMonths.contains("$year-$month")
    }
    
    // 标记月份为已预加载
    fun markMonthPreloaded(year: Int, month: Int) {
        preloadedMonths.add("$year-$month")
    }
    
    // 获取缓存的农历信息
    fun getCachedLunar(cacheKey: String): String? = lunarCache[cacheKey]
    fun getCachedSolarTerm(cacheKey: String): String? = solarTermCache[cacheKey]
    
    // 缓存农历信息
    fun cacheLunar(cacheKey: String, value: String) {
        lunarCache[cacheKey] = value
    }
    
    fun cacheSolarTerm(cacheKey: String, value: String?) {
        solarTermCache[cacheKey] = value
    }
    
    // 预加载指定月份的农历信息
    suspend fun preloadMonth(year: Int, month: Int) = withContext(Dispatchers.Default) {
        val monthKey = "$year-$month"
        if (isMonthPreloaded(year, month)) return@withContext
        
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // month是1-12，Calendar.MONTH是0-11
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // 批量计算当月所有日期的农历信息
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val cacheKey = "$year-$month-$day"
            
            // 如果缓存中没有，则计算
            if (getCachedLunar(cacheKey) == null) {
                try {
                    val lunarText = LunarCalendarUtil.getSimpleLunarDate(calendar)
                    cacheLunar(cacheKey, lunarText)
                } catch (e: Exception) {
                    cacheLunar(cacheKey, "农历")
                }
            }
            
            if (getCachedSolarTerm(cacheKey) == null) {
                try {
                    val solarTerm = LunarCalendarUtil.getSolarTerm(calendar)
                    cacheSolarTerm(cacheKey, solarTerm)
                } catch (e: Exception) {
                    cacheSolarTerm(cacheKey, null)
                }
            }
        }
        
        markMonthPreloaded(year, month)
    }
}

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
    
    // 计算需要显示的天数（从周日开始）
    val startOffset = firstDayOfWeek - Calendar.SUNDAY
    val totalDays = startOffset + daysInMonth
    val weeks = (totalDays + 6) / 7
    
    // 获取屏幕配置和密度
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // 计算动态格子大小
    val screenWidthDp = configuration.screenWidthDp.dp
    val horizontalPadding = 16.dp // 恢复合理的边距
    val spacingBetweenCells = 12.dp // 7个格子之间有6个间距，每个间距2dp，总共12dp
    val availableWidth = screenWidthDp - horizontalPadding - spacingBetweenCells
    val cellSize = availableWidth / 7
    
    // 确保最小尺寸和最大尺寸 - 调整为更合理的范围
    val finalCellSize = cellSize.coerceIn(45.dp, 70.dp) // 适中的大小，确保显示完整
    
    // 根据格子大小动态调整字体大小
    val dayFontSize = (finalCellSize.value * 0.30f).coerceIn(13f, 20f).sp
    val lunarFontSize = (finalCellSize.value * 0.14f).coerceIn(6f, 10f).sp
    val weekHeaderFontSize = (finalCellSize.value * 0.28f).coerceIn(12f, 16f).sp
    
    // 用于显示日程悬浮窗的状态
    var showEventsDialog by remember { mutableStateOf(false) }
    var selectedDateEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var selectedDateCalendar by remember { mutableStateOf<Calendar?>(null) }
    
    // 农历信息状态
    var lunarInfoMap by remember { mutableStateOf<Map<String, LunarInfo>>(emptyMap()) }
    
    // 当前月份信息
    val currentYear = currentDate.get(Calendar.YEAR)
    val currentMonth = currentDate.get(Calendar.MONTH) + 1
    
    // 预计算基础日期信息（不包含农历）
    val baseDateInfoList = remember(currentDate.timeInMillis) {
        val today = Calendar.getInstance()
        val baseCalendar = calendar.clone() as Calendar
        
        (0 until 42).map { index ->
            val week = index / 7
            val dayIndex = index % 7
            val dayNumber = week * 7 + dayIndex - startOffset + 1
            
            val dayCalendar = when {
                dayNumber <= 0 -> {
                    (baseCalendar.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH) + dayNumber)
                    }
                }
                dayNumber > daysInMonth -> {
                    (baseCalendar.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                        set(Calendar.DAY_OF_MONTH, dayNumber - daysInMonth)
                    }
                }
                else -> {
                    (baseCalendar.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, dayNumber)
                    }
                }
            }
            
            val isCurrentMonth = dayNumber > 0 && dayNumber <= daysInMonth
            val isToday = isSameDay(dayCalendar, today)
            val dateKey = "${dayCalendar.get(Calendar.YEAR)}-${dayCalendar.get(Calendar.MONTH)}-${dayCalendar.get(Calendar.DAY_OF_MONTH)}"
            
            Triple(dayCalendar, isCurrentMonth, isToday to dateKey)
        }
    }
    
    // 事件信息
    val eventsByDate = remember(events) {
        events.groupBy { event ->
            val eventCal = event.getStartCalendar()
            "${eventCal.get(Calendar.YEAR)}-${eventCal.get(Calendar.MONTH)}-${eventCal.get(Calendar.DAY_OF_MONTH)}"
        }
    }
    
    // 智能加载农历信息
    LaunchedEffect(currentDate.timeInMillis) {
        LunarPreloadManager.clearOldCache()
        
        // 第一步：立即检查缓存，如果当前月份已预加载，立即显示
        val isCurrentMonthCached = LunarPreloadManager.isMonthPreloaded(currentYear, currentMonth)
        
        if (isCurrentMonthCached) {
            // 当前月份已缓存，立即构建农历信息
            val newLunarInfoMap = mutableMapOf<String, LunarInfo>()
            
            baseDateInfoList.forEach { (dayCalendar, isCurrentMonth, todayInfo) ->
                val (isToday, dateKey) = todayInfo
                
                if (isCurrentMonth) {
                    val cacheKey = "${dayCalendar.get(Calendar.YEAR)}-${dayCalendar.get(Calendar.MONTH) + 1}-${dayCalendar.get(Calendar.DAY_OF_MONTH)}"
                    
                    val lunarText = LunarPreloadManager.getCachedLunar(cacheKey) ?: "农历"
                    val solarTerm = LunarPreloadManager.getCachedSolarTerm(cacheKey)
                    
                    newLunarInfoMap[dateKey] = LunarInfo(
                        lunarText = lunarText,
                        solarTerm = solarTerm,
                        dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH),
                        isCurrentMonth = true,
                        isToday = isToday
                    )
                } else {
                    newLunarInfoMap[dateKey] = LunarInfo(
                        lunarText = "",
                        solarTerm = null,
                        dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH),
                        isCurrentMonth = false,
                        isToday = isToday
                    )
                }
            }
            
            lunarInfoMap = newLunarInfoMap
        } else {
            // 当前月份未缓存，先显示占位符，然后异步加载
            val placeholderMap = mutableMapOf<String, LunarInfo>()
            
            baseDateInfoList.forEach { (dayCalendar, isCurrentMonth, todayInfo) ->
                val (isToday, dateKey) = todayInfo
                
                placeholderMap[dateKey] = LunarInfo(
                    lunarText = if (isCurrentMonth) "..." else "",
                    solarTerm = null,
                    dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = isCurrentMonth,
                    isToday = isToday
                )
            }
            
            lunarInfoMap = placeholderMap
            
            // 异步加载当前月份
            withContext(Dispatchers.Default) {
                LunarPreloadManager.preloadMonth(currentYear, currentMonth)
                
                // 重新构建农历信息
                val newLunarInfoMap = mutableMapOf<String, LunarInfo>()
                
                baseDateInfoList.forEach { (dayCalendar, isCurrentMonth, todayInfo) ->
                    val (isToday, dateKey) = todayInfo
                    
                    if (isCurrentMonth) {
                        val cacheKey = "${dayCalendar.get(Calendar.YEAR)}-${dayCalendar.get(Calendar.MONTH) + 1}-${dayCalendar.get(Calendar.DAY_OF_MONTH)}"
                        
                        val lunarText = LunarPreloadManager.getCachedLunar(cacheKey) ?: "农历"
                        val solarTerm = LunarPreloadManager.getCachedSolarTerm(cacheKey)
                        
                        newLunarInfoMap[dateKey] = LunarInfo(
                            lunarText = lunarText,
                            solarTerm = solarTerm,
                            dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH),
                            isCurrentMonth = true,
                            isToday = isToday
                        )
                    } else {
                        newLunarInfoMap[dateKey] = LunarInfo(
                            lunarText = "",
                            solarTerm = null,
                            dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH),
                            isCurrentMonth = false,
                            isToday = isToday
                        )
                    }
                }
                
                // 在主线程更新UI
                withContext(Dispatchers.Main) {
                    lunarInfoMap = newLunarInfoMap
                }
            }
        }
        
        // 第二步：预加载相邻月份（在后台进行，不影响当前显示）
        withContext(Dispatchers.Default) {
            // 预加载前一个月
            val prevCalendar = currentDate.clone() as Calendar
            prevCalendar.add(Calendar.MONTH, -1)
            val prevYear = prevCalendar.get(Calendar.YEAR)
            val prevMonth = prevCalendar.get(Calendar.MONTH) + 1
            
            if (!LunarPreloadManager.isMonthPreloaded(prevYear, prevMonth)) {
                LunarPreloadManager.preloadMonth(prevYear, prevMonth)
            }
            
            // 预加载后一个月
            val nextCalendar = currentDate.clone() as Calendar
            nextCalendar.add(Calendar.MONTH, 1)
            val nextYear = nextCalendar.get(Calendar.YEAR)
            val nextMonth = nextCalendar.get(Calendar.MONTH) + 1
            
            if (!LunarPreloadManager.isMonthPreloaded(nextYear, nextMonth)) {
                LunarPreloadManager.preloadMonth(nextYear, nextMonth)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 星期标题（从周日开始）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val weekDays = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .width(finalCellSize)
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = weekHeaderFontSize,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 日期网格
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 固定显示6行
            repeat(6) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(7) { dayIndex ->
                        val index = week * 7 + dayIndex
                        
                        // 只在有效周数内显示日期，超出的周显示空白
                        if (week < weeks && index < baseDateInfoList.size) {
                            val (dayCalendar, isCurrentMonth, todayInfo) = baseDateInfoList[index]
                            val (isToday, dateKey) = todayInfo
                            val dayEvents = eventsByDate[dateKey] ?: emptyList()
                            
                            // 获取农历信息
                            val lunarInfo = lunarInfoMap[dateKey] ?: LunarInfo(
                                lunarText = if (isCurrentMonth) "..." else "",
                                solarTerm = null,
                                dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH),
                                isCurrentMonth = isCurrentMonth,
                                isToday = isToday
                            )
                            
                            OptimizedDayCell(
                                lunarInfo = lunarInfo,
                                hasEvents = dayEvents.isNotEmpty(),
                                cellSize = finalCellSize,
                                dayFontSize = dayFontSize,
                                lunarFontSize = lunarFontSize,
                                onClick = {
                                    if (dayEvents.isNotEmpty()) {
                                        selectedDateEvents = dayEvents
                                        selectedDateCalendar = dayCalendar
                                        showEventsDialog = true
                                    }
                                },
                                onLongClick = {
                                    onDateClick(dayCalendar)
                                }
                            )
                        } else {
                            // 空白格子，保持布局一致
                            Box(
                                modifier = Modifier
                                    .width(finalCellSize)
                                    .height(finalCellSize)
                            )
                        }
                    }
                }
            }
        }
        
        // 底部留白
        Spacer(modifier = Modifier.weight(1f))
    }
    
    // 日程悬浮窗
    if (showEventsDialog && selectedDateCalendar != null) {
        EventsDialog(
            date = selectedDateCalendar!!,
            events = selectedDateEvents,
            onDismiss = { showEventsDialog = false },
            onEventClick = { event ->
                showEventsDialog = false
                onEventClick(event)
            },
            onAddEvent = {
                showEventsDialog = false
                onDateClick(selectedDateCalendar!!)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OptimizedDayCell(
    lunarInfo: LunarInfo,
    hasEvents: Boolean,
    cellSize: androidx.compose.ui.unit.Dp,
    dayFontSize: androidx.compose.ui.unit.TextUnit,
    lunarFontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = remember(lunarInfo.isToday) {
        if (lunarInfo.isToday) null else Color.Transparent
    }
    
    // 根据格子大小动态调整圆角和内边距
    val cornerRadius = (cellSize.value * 0.125f).coerceIn(4f, 8f).dp
    val topPadding = (cellSize.value * 0.04f).coerceIn(1f, 3f).dp
    val indicatorHeight = (cellSize.value * 0.06f).coerceIn(2f, 4f).dp
    val bottomSpacing = (cellSize.value * 0.06f).coerceIn(2f, 4f).dp
    
    Box(
        modifier = Modifier
            .width(cellSize)
            .height(cellSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                backgroundColor ?: if (lunarInfo.isToday) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 日期和农历部分
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = topPadding)
            ) {
                // 阳历日期
                Text(
                    text = lunarInfo.dayOfMonth.toString(),
                    fontSize = dayFontSize,
                    color = if (lunarInfo.isCurrentMonth) {
                        if (lunarInfo.isToday) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                    fontWeight = if (lunarInfo.isToday) FontWeight.Bold else FontWeight.Medium,
                    lineHeight = dayFontSize
                )
                
                // 农历日期（只为当前月份显示）
                if (lunarInfo.isCurrentMonth && lunarInfo.lunarText.isNotEmpty()) {
                    Text(
                        text = lunarInfo.lunarText,
                        fontSize = lunarFontSize,
                        color = if (lunarInfo.isToday) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        },
                        textAlign = TextAlign.Center,
                        lineHeight = lunarFontSize,
                        modifier = Modifier.offset(y = (-0.5).dp)
                    )
                }
                
                // 节气信息 - 放在农历下面
                if (lunarInfo.isCurrentMonth && !lunarInfo.solarTerm.isNullOrEmpty()) {
                    Text(
                        text = lunarInfo.solarTerm,
                        fontSize = lunarFontSize,
                        color = if (lunarInfo.isToday) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                        },
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        lineHeight = lunarFontSize,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
            }
            
            // 空白区域 - 推开日程长条
            Spacer(modifier = Modifier.weight(1f))
            
            // 日程指示器 - 使用统一颜色
            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(indicatorHeight)
                        .clip(RoundedCornerShape(indicatorHeight / 2))
                        .background(Color(0xFF6200EE))
                )
                Spacer(modifier = Modifier.height(bottomSpacing))
            } else {
                Spacer(modifier = Modifier.height(indicatorHeight + bottomSpacing))
            }
        }
    }
}

@Composable
fun EventsDialog(
    date: Calendar,
    events: List<Event>,
    onDismiss: () -> Unit,
    onEventClick: (Event) -> Unit,
    onAddEvent: () -> Unit
) {
    // 缓存格式化器，避免重复创建
    val dateFormat = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 标题
                Text(
                    text = dateFormat.format(date.time),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 日程列表
                if (events.isNotEmpty()) {
                    Text(
                        text = "今日日程 (${events.size}个)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 使用LazyColumn优化大量事件的渲染
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(events) { event ->
                            EventItem(
                                event = event,
                                timeFormat = timeFormat,
                                onClick = { onEventClick(event) }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "今日暂无日程",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddEvent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("添加日程")
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    event: Event,
    timeFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色指示器
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        Color(0xFF6200EE),
                        RoundedCornerShape(6.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!event.isAllDay) {
                    Text(
                        text = "${timeFormat.format(event.startTime)} - ${timeFormat.format(event.endTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "全天",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

