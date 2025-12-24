package com.example.calendar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.calendar.data.Event
import com.example.calendar.ui.components.TimePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    event: Event?,
    initialDate: Calendar? = null,
    onSave: (Event) -> Unit,
    onDelete: (() -> Unit)? = null,
    onCancel: () -> Unit
) {
    val titleState = remember { mutableStateOf(event?.title ?: "") }
    val descriptionState = remember { mutableStateOf(event?.description ?: "") }
    val locationState = remember { mutableStateOf(event?.location ?: "") }
    val isAllDayState = remember { mutableStateOf(event?.isAllDay ?: false) }
    val reminderMinutesState = remember { mutableStateOf(event?.reminderMinutes ?: 0) }
    
    val startDate = remember { mutableStateOf(event?.getStartCalendar() ?: initialDate ?: Calendar.getInstance()) }
    val endDate = remember { 
        mutableStateOf(
            event?.getEndCalendar() ?: run {
                val defaultEndDate = (initialDate ?: Calendar.getInstance()).clone() as Calendar
                defaultEndDate.add(Calendar.HOUR_OF_DAY, 1)
                defaultEndDate
            }
        )
    }
    
    val startHour = remember { mutableStateOf(startDate.value.get(Calendar.HOUR_OF_DAY)) }
    val startMinute = remember { mutableStateOf(startDate.value.get(Calendar.MINUTE)) }
    val endHour = remember { mutableStateOf(endDate.value.get(Calendar.HOUR_OF_DAY)) }
    val endMinute = remember { mutableStateOf(endDate.value.get(Calendar.MINUTE)) }
    
    // 时间选择器对话框状态
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (event == null) "新建日程" else "编辑日程") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("取消")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val startCal = startDate.value.clone() as Calendar
                            val endCal = endDate.value.clone() as Calendar
                            
                            if (!isAllDayState.value) {
                                startCal.set(Calendar.HOUR_OF_DAY, startHour.value)
                                startCal.set(Calendar.MINUTE, startMinute.value)
                                endCal.set(Calendar.HOUR_OF_DAY, endHour.value)
                                endCal.set(Calendar.MINUTE, endMinute.value)
                            } else {
                                startCal.set(Calendar.HOUR_OF_DAY, 0)
                                startCal.set(Calendar.MINUTE, 0)
                                endCal.set(Calendar.HOUR_OF_DAY, 23)
                                endCal.set(Calendar.MINUTE, 59)
                            }
                            
                            val newEvent = Event(
                                id = event?.id ?: 0,
                                title = titleState.value,
                                description = descriptionState.value,
                                startTime = startCal.timeInMillis,
                                endTime = endCal.timeInMillis,
                                location = locationState.value,
                                reminderMinutes = reminderMinutesState.value,
                                isAllDay = isAllDayState.value
                            )
                            onSave(newEvent)
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titleState.value,
                onValueChange = { titleState.value = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = descriptionState.value,
                onValueChange = { descriptionState.value = it },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            OutlinedTextField(
                value = locationState.value,
                onValueChange = { locationState.value = it },
                label = { Text("地点") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("全天事件")
                Switch(
                    checked = isAllDayState.value,
                    onCheckedChange = { isAllDayState.value = it }
                )
            }
            
            if (!isAllDayState.value) {
                // 开始时间选择
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartTimePicker = true },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "开始时间",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = timeFormat.format(startDate.value.apply {
                                set(Calendar.HOUR_OF_DAY, startHour.value)
                                set(Calendar.MINUTE, startMinute.value)
                            }.time),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 结束时间选择
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEndTimePicker = true },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "结束时间",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = timeFormat.format(endDate.value.apply {
                                set(Calendar.HOUR_OF_DAY, endHour.value)
                                set(Calendar.MINUTE, endMinute.value)
                            }.time),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // 提醒设置
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "提醒设置",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // 提醒时间选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("提醒时间")
                        var expanded by remember { mutableStateOf(false) }
                        val reminderOptions = listOf(0, 5, 10, 15, 30, 60, 120)
                        val reminderText = when (reminderMinutesState.value) {
                            0 -> "不提醒"
                            else -> "${reminderMinutesState.value}分钟前"
                        }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = reminderText,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                reminderOptions.forEach { minutes ->
                                    DropdownMenuItem(
                                        text = { Text(if (minutes == 0) "不提醒" else "${minutes}分钟前") },
                                        onClick = {
                                            reminderMinutesState.value = minutes
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // 提醒类型选择（只有在设置了提醒时间时才显示）
                    if (reminderMinutesState.value > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "提醒类型",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        var isAlarmReminder by remember { mutableStateOf(true) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 闹钟提醒选项
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { isAlarmReminder = true }
                                    .weight(1f)
                            ) {
                                RadioButton(
                                    selected = isAlarmReminder,
                                    onClick = { isAlarmReminder = true }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "闹钟提醒",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "全屏闹钟+声音+振动",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            
                            // 通知提醒选项
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { isAlarmReminder = false }
                                    .weight(1f)
                            ) {
                                RadioButton(
                                    selected = !isAlarmReminder,
                                    onClick = { isAlarmReminder = false }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "通知提醒",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "静默通知栏提醒",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        
                        // 更新reminderMinutesState的值来标记提醒类型
                        // 正数表示闹钟提醒，负数表示通知提醒
                        LaunchedEffect(isAlarmReminder, reminderMinutesState.value) {
                            if (reminderMinutesState.value > 0) {
                                val absValue = kotlin.math.abs(reminderMinutesState.value)
                                reminderMinutesState.value = if (isAlarmReminder) absValue else -absValue
                            }
                        }
                        
                        // 根据当前值设置isAlarmReminder的初始状态
                        LaunchedEffect(Unit) {
                            isAlarmReminder = reminderMinutesState.value > 0
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (event != null && onDelete != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除日程")
                }
            }
        }
        
        // 开始时间选择器对话框
        TimePickerDialog(
            showDialog = showStartTimePicker,
            hour = startHour.value,
            minute = startMinute.value,
            onTimeSelected = { hour, minute ->
                startHour.value = hour
                startMinute.value = minute
                
                // 自动调整结束时间为开始时间加一小时
                val newEndHour = if (hour == 23) 0 else hour + 1
                val newEndDay = if (hour == 23) 1 else 0
                
                endHour.value = newEndHour
                endMinute.value = minute
                
                // 如果跨天了，需要调整结束日期
                if (newEndDay > 0) {
                    endDate.value = (endDate.value.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                } else {
                    // 确保结束日期和开始日期相同（如果没有跨天）
                    endDate.value = startDate.value.clone() as Calendar
                }
            },
            onDismiss = { showStartTimePicker = false },
            title = "选择开始时间"
        )
        
        // 结束时间选择器对话框
        TimePickerDialog(
            showDialog = showEndTimePicker,
            hour = endHour.value,
            minute = endMinute.value,
            onTimeSelected = { hour, minute ->
                endHour.value = hour
                endMinute.value = minute
            },
            onDismiss = { showEndTimePicker = false },
            title = "选择结束时间"
        )
    }
}

