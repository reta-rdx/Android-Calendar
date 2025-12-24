package com.example.calendar.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calendar.data.Event
import com.example.calendar.ui.components.DayView
import com.example.calendar.ui.components.MonthView
import com.example.calendar.ui.components.WeekView
import com.example.calendar.viewmodel.CalendarViewModel
import com.example.calendar.viewmodel.ViewType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onEventClick: (Event) -> Unit,
    onAddEvent: () -> Unit
) {
    val currentDate by viewModel.currentDate.collectAsState()
    val viewType by viewModel.viewType.collectAsState()
    val events by viewModel.events.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    
    // 文件选择器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importFromFile(it) }
    }
    
    var showMenu by remember { mutableStateOf(false) }
    
    // 显示导入/导出结果
    LaunchedEffect(importResult) {
        importResult?.let {
            viewModel.clearImportResult()
        }
    }
    
    LaunchedEffect(exportResult) {
        exportResult?.let {
            viewModel.clearExportResult()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (viewType) {
                            ViewType.MONTH -> {
                                val sdf = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
                                sdf.format(currentDate.time)
                            }
                            ViewType.WEEK -> {
                                val sdf = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
                                sdf.format(currentDate.time)
                            }
                            ViewType.DAY -> {
                                val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                                sdf.format(currentDate.time)
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateToPrevious() }) {
                        Text("◀")
                    }
                },
                actions = {
                    // 添加菜单按钮
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    
                    // 下拉菜单
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("导入日程") },
                            onClick = {
                                showMenu = false
                                launcher.launch("text/calendar")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("导出日程") },
                            onClick = {
                                showMenu = false
                                viewModel.exportEvents()
                            }
                        )
                    }
                    
                    IconButton(onClick = { viewModel.navigateToToday() }) {
                        Text("今天")
                    }
                    IconButton(onClick = { viewModel.navigateToNext() }) {
                        Text("▶")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEvent) {
                Icon(Icons.Default.Add, contentDescription = "添加日程")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = viewType == ViewType.MONTH,
                    onClick = { viewModel.setViewType(ViewType.MONTH) },
                    icon = { Text("月") }
                )
                NavigationBarItem(
                    selected = viewType == ViewType.WEEK,
                    onClick = { viewModel.setViewType(ViewType.WEEK) },
                    icon = { Text("周") }
                )
                NavigationBarItem(
                    selected = viewType == ViewType.DAY,
                    onClick = { viewModel.setViewType(ViewType.DAY) },
                    icon = { Text("日") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (viewType) {
                ViewType.MONTH -> {
                    MonthView(
                        currentDate = currentDate,
                        events = events,
                        onDateClick = { date ->
                            viewModel.setCurrentDate(date)
                            onAddEvent()
                        },
                        onEventClick = onEventClick
                    )
                }
                ViewType.WEEK -> {
                    WeekView(
                        currentDate = currentDate,
                        events = events,
                        onDateClick = { date ->
                            viewModel.setCurrentDate(date)
                            viewModel.setViewType(ViewType.DAY)
                        },
                        onEventClick = onEventClick
                    )
                }
                ViewType.DAY -> {
                    DayView(
                        currentDate = currentDate,
                        events = events,
                        onEventClick = onEventClick
                    )
                }
            }
        }
    }
}

