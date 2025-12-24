package com.example.calendar

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.calendar.ui.screens.EventViewScreen
import com.example.calendar.ui.theme.CalendarTheme
import com.example.calendar.viewmodel.CalendarViewModel
import com.example.calendar.util.LunarLibraryChecker
import com.example.calendar.util.PermissionHelper
import android.util.Log
import java.util.Calendar

class MainActivity : ComponentActivity() {
    
    // 权限请求启动器
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }.keys
        if (deniedPermissions.isNotEmpty()) {
            Log.w("MainActivity", "被拒绝的权限: $deniedPermissions")
            // 可以在这里显示简单的Toast提示
            android.widget.Toast.makeText(
                this, 
                "部分权限被拒绝，可能影响应用功能", 
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            Log.i("MainActivity", "所有权限已授予")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
            
            // 安全地检查农历库是否可用
            try {
                val isLunarLibraryAvailable = LunarLibraryChecker.checkLunarLibraryAvailability()
                Log.d("MainActivity", "农历库可用性: $isLunarLibraryAvailable")
                if (isLunarLibraryAvailable) {
                    Log.d("MainActivity", LunarLibraryChecker.getDetailedLibraryInfo())
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "农历库检查失败", e)
                // 继续执行，不让农历库问题阻止应用启动
            }
            
            // 创建通知渠道
            try {
                com.example.calendar.util.NotificationHelper.createNotificationChannel(this)
            } catch (e: Exception) {
                Log.e("MainActivity", "创建通知渠道失败", e)
            }
            
            // 检查并请求核心权限
            try {
                checkAndRequestCorePermissions()
            } catch (e: Exception) {
                Log.e("MainActivity", "权限检查失败", e)
            }
            
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
        } catch (e: Exception) {
            Log.e("MainActivity", "onCreate失败", e)
            // 显示错误信息给用户
            android.widget.Toast.makeText(
                this, 
                "应用启动失败: ${e.message}", 
                android.widget.Toast.LENGTH_LONG
            ).show()
            // 不要finish()，让用户有机会看到错误信息
        }
    }
    
    private fun checkAndRequestCorePermissions() {
        Log.d("MainActivity", "检查核心权限...")
        
        if (!PermissionHelper.hasAllCorePermissions(this)) {
            val deniedPermissions = PermissionHelper.getDeniedCorePermissions(this)
            Log.i("MainActivity", "需要请求的权限: $deniedPermissions")
            
            if (deniedPermissions.isNotEmpty()) {
                // 直接使用Android原生权限请求对话框
                requestPermissionLauncher.launch(deniedPermissions.toTypedArray())
            }
        } else {
            Log.i("MainActivity", "所有核心权限已授予")
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
    var showEventView by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<Event?>(null) }
    var viewingEvent by remember { mutableStateOf<Event?>(null) }
    var initialDate by remember { mutableStateOf<Calendar?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { }
    }
    
    // 处理返回键
    BackHandler(enabled = showEventEdit || showEventView) {
        when {
            showEventEdit -> {
                showEventEdit = false
                editingEvent = null
                initialDate = null
            }
            showEventView -> {
                showEventView = false
                viewingEvent = null
            }
        }
    }
    
    // 处理日历主屏幕的返回键 - 返回到系统主屏幕
    BackHandler(enabled = !showEventEdit && !showEventView) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    when {
        showEventEdit -> {
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
        }
        showEventView -> {
            EventViewScreen(
                event = viewingEvent!!,
                onEdit = {
                    editingEvent = viewingEvent
                    showEventView = false
                    showEventEdit = true
                },
                onDelete = {
                    viewModel.deleteEvent(viewingEvent!!)
                    showEventView = false
                    viewingEvent = null
                },
                onBack = {
                    showEventView = false
                    viewingEvent = null
                }
            )
        }
        else -> {
            CalendarScreen(
                viewModel = viewModel,
                onEventClick = { event ->
                    viewingEvent = event
                    showEventView = true
                },
                onAddEvent = {
                    editingEvent = null
                    initialDate = viewModel.currentDate.value
                    showEventEdit = true
                }
            )
        }
    }
}
