package com.example.calendar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
                    icon = { Text("月") },
                    label = { Text("月视图") }
                )
                NavigationBarItem(
                    selected = viewType == ViewType.WEEK,
                    onClick = { viewModel.setViewType(ViewType.WEEK) },
                    icon = { Text("周") },
                    label = { Text("周视图") }
                )
                NavigationBarItem(
                    selected = viewType == ViewType.DAY,
                    onClick = { viewModel.setViewType(ViewType.DAY) },
                    icon = { Text("日") },
                    label = { Text("日视图") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (viewType) {
                ViewType.MONTH -> {
                    MonthView(
                        currentDate = currentDate,
                        events = events,
                        onDateClick = { date ->
                            viewModel.setCurrentDate(date)
                            viewModel.setViewType(ViewType.DAY)
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

