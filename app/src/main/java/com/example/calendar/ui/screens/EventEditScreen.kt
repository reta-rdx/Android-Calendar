package com.example.calendar.ui.screens

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
import java.util.Calendar

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
    val endDate = remember { mutableStateOf(event?.getEndCalendar() ?: initialDate ?: Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }) }
    
    val startHour = remember { mutableStateOf(startDate.value.get(Calendar.HOUR_OF_DAY)) }
    val startMinute = remember { mutableStateOf(startDate.value.get(Calendar.MINUTE)) }
    val endHour = remember { mutableStateOf(endDate.value.get(Calendar.HOUR_OF_DAY)) }
    val endMinute = remember { mutableStateOf(endDate.value.get(Calendar.MINUTE)) }
    
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column {
                        Text("开始时间")
                        Row {
                            OutlinedTextField(
                                value = startHour.value.toString(),
                                onValueChange = { 
                                    it.toIntOrNull()?.takeIf { h -> h in 0..23 }?.let { 
                                        startHour.value = it 
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("时") }
                            )
                            Text(":", modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp))
                            OutlinedTextField(
                                value = startMinute.value.toString(),
                                onValueChange = { 
                                    it.toIntOrNull()?.takeIf { m -> m in 0..59 }?.let { 
                                        startMinute.value = it 
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("分") }
                            )
                        }
                    }
                    
                    Column {
                        Text("结束时间")
                        Row {
                            OutlinedTextField(
                                value = endHour.value.toString(),
                                onValueChange = { 
                                    it.toIntOrNull()?.takeIf { h -> h in 0..23 }?.let { 
                                        endHour.value = it 
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("时") }
                            )
                            Text(":", modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp))
                            OutlinedTextField(
                                value = endMinute.value.toString(),
                                onValueChange = { 
                                    it.toIntOrNull()?.takeIf { m -> m in 0..59 }?.let { 
                                        endMinute.value = it 
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("分") }
                            )
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("提醒")
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
    }
}

