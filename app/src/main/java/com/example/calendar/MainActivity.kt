package com.example.calendar

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.data.Event
import com.example.calendar.ui.screens.CalendarScreen
import com.example.calendar.ui.screens.EventEditScreen
import com.example.calendar.ui.theme.CalendarTheme
import com.example.calendar.viewmodel.CalendarViewModel
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 创建通知渠道
        com.example.calendar.util.NotificationHelper.createNotificationChannel(this)
        
        setContent {
            CalendarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarApp()
                }
            }
        }
    }
}

@Composable
fun CalendarApp() {
    // 在 Composable 上下文中获取 Application
    val context = LocalContext.current
    val application = context.applicationContext as Application
    
    val viewModel: CalendarViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CalendarViewModel(application = application) as T
            }
        }
    )
    
    var showEventEdit by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<Event?>(null) }
    var initialDate by remember { mutableStateOf<Calendar?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { }
    }
    
    if (showEventEdit) {
        EventEditScreen(
            event = editingEvent,
            initialDate = initialDate,
            onSave = { event ->
                if (editingEvent == null) {
                    viewModel.insertEvent(event)
                } else {
                    viewModel.updateEvent(event)
                }
                showEventEdit = false
                editingEvent = null
                initialDate = null
            },
            onDelete = if (editingEvent != null) {
                {
                    viewModel.deleteEvent(editingEvent!!)
                    showEventEdit = false
                    editingEvent = null
                }
            } else null,
            onCancel = {
                showEventEdit = false
                editingEvent = null
                initialDate = null
            }
        )
    } else {
        CalendarScreen(
            viewModel = viewModel,
            onEventClick = { event ->
                editingEvent = event
                showEventEdit = true
            },
            onAddEvent = {
                editingEvent = null
                initialDate = viewModel.currentDate.value
                showEventEdit = true
            }
        )
    }
}
