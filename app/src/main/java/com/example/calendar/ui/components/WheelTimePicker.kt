package com.example.calendar.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelTimePicker(
    hour: Int,
    minute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 为了实现循环滚动，我们创建一个很大的列表，中间位置对应实际值
    val hourOffset = 1000 * 24 // 确保有足够的循环空间
    val minuteOffset = 1000 * 60
    
    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = hourOffset + hour)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = minuteOffset + minute)
    val coroutineScope = rememberCoroutineScope()
    
    // 监听小时滚动状态变化
    LaunchedEffect(hourListState.firstVisibleItemIndex, hourListState.isScrollInProgress) {
        if (!hourListState.isScrollInProgress) {
            val newHour = hourListState.firstVisibleItemIndex % 24
            if (newHour != hour) {
                onTimeChanged(newHour, minute)
            }
        }
    }
    
    // 监听分钟滚动状态变化
    LaunchedEffect(minuteListState.firstVisibleItemIndex, minuteListState.isScrollInProgress) {
        if (!minuteListState.isScrollInProgress) {
            val newMinute = minuteListState.firstVisibleItemIndex % 60
            if (newMinute != minute) {
                onTimeChanged(hour, newMinute)
            }
        }
    }
    
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 小时选择器（循环滚动）
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                LazyColumn(
                    state = hourListState,
                    flingBehavior = rememberSnapFlingBehavior(hourListState),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 80.dp)
                ) {
                    items(Int.MAX_VALUE) { index ->
                        val hourValue = index % 24
                        val isSelected = (hourListState.firstVisibleItemIndex % 24) == hourValue && 
                                       kotlin.math.abs(hourListState.firstVisibleItemIndex - index) < 12
                        Text(
                            text = String.format("%02d", hourValue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                                .alpha(if (isSelected) 1f else 0.5f),
                            textAlign = TextAlign.Center,
                            fontSize = if (isSelected) 24.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // 选中指示器
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                )
            }
            
            // 分隔符
            Text(
                text = ":",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 8.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // 分钟选择器（1分钟间隔，循环滚动）
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                LazyColumn(
                    state = minuteListState,
                    flingBehavior = rememberSnapFlingBehavior(minuteListState),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 80.dp)
                ) {
                    items(Int.MAX_VALUE) { index ->
                        val minuteValue = index % 60
                        val isSelected = (minuteListState.firstVisibleItemIndex % 60) == minuteValue && 
                                       kotlin.math.abs(minuteListState.firstVisibleItemIndex - index) < 30
                        Text(
                            text = String.format("%02d", minuteValue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .wrapContentHeight(Alignment.CenterVertically)
                                .alpha(if (isSelected) 1f else 0.5f),
                            textAlign = TextAlign.Center,
                            fontSize = if (isSelected) 24.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // 选中指示器
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    showDialog: Boolean,
    hour: Int,
    minute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    title: String = "选择时间"
) {
    if (showDialog) {
        var selectedHour by remember { mutableStateOf(hour) }
        var selectedMinute by remember { mutableStateOf(minute) }
        
        // 重置选择值为当前传入的值
        LaunchedEffect(hour, minute) {
            selectedHour = hour
            selectedMinute = minute
        }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                WheelTimePicker(
                    hour = selectedHour,
                    minute = selectedMinute,
                    onTimeChanged = { h, m ->
                        selectedHour = h
                        selectedMinute = m
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(selectedHour, selectedMinute)
                        onDismiss()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}