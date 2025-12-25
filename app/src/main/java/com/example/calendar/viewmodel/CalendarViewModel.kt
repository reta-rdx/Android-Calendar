package com.example.calendar.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendar.data.Event
import com.example.calendar.data.EventRepository
import com.example.calendar.util.ICalImporter
import com.example.calendar.util.ICalExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ViewType {
    MONTH, WEEK, DAY
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EventRepository(application)
    
    private val _currentDate = MutableStateFlow(Calendar.getInstance())
    val currentDate: StateFlow<Calendar> = _currentDate.asStateFlow()
    
    private val _viewType = MutableStateFlow(ViewType.MONTH)
    val viewType: StateFlow<ViewType> = _viewType.asStateFlow()
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()
    
    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult.asStateFlow()
    
    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()
    
    init {
        // 观察事件变化
        repository.getAllEvents()
            .onEach { allEvents ->
                val cal = _currentDate.value
                val startTime: Long
                val endTime: Long
                
                when (_viewType.value) {
                    ViewType.MONTH -> {
                        // 月视图需要包含显示的所有天数（包括上个月和下个月的部分）
                        val firstDayOfMonth = cal.clone() as Calendar
                        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                        
                        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
                        val startOffset = firstDayOfWeek - Calendar.SUNDAY
                        
                        val startOfDisplay = firstDayOfMonth.clone() as Calendar
                        startOfDisplay.add(Calendar.DAY_OF_MONTH, -startOffset)
                        startOfDisplay.set(Calendar.HOUR_OF_DAY, 0)
                        startOfDisplay.set(Calendar.MINUTE, 0)
                        startOfDisplay.set(Calendar.SECOND, 0)
                        startOfDisplay.set(Calendar.MILLISECOND, 0)
                        
                        val endOfDisplay = startOfDisplay.clone() as Calendar
                        endOfDisplay.add(Calendar.DAY_OF_MONTH, 41) // 6周 = 42天
                        endOfDisplay.set(Calendar.HOUR_OF_DAY, 23)
                        endOfDisplay.set(Calendar.MINUTE, 59)
                        endOfDisplay.set(Calendar.SECOND, 59)
                        
                        startTime = startOfDisplay.timeInMillis
                        endTime = endOfDisplay.timeInMillis
                    }
                    ViewType.WEEK -> {
                        val startOfWeek = cal.clone() as Calendar
                        val dayOfWeek = startOfWeek.get(Calendar.DAY_OF_WEEK)
                        val daysFromSunday = dayOfWeek - Calendar.SUNDAY
                        startOfWeek.add(Calendar.DAY_OF_MONTH, -daysFromSunday)
                        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
                        startOfWeek.set(Calendar.MINUTE, 0)
                        startOfWeek.set(Calendar.SECOND, 0)
                        startOfWeek.set(Calendar.MILLISECOND, 0)
                        
                        val endOfWeek = startOfWeek.clone() as Calendar
                        endOfWeek.add(Calendar.DAY_OF_MONTH, 6)
                        endOfWeek.set(Calendar.HOUR_OF_DAY, 23)
                        endOfWeek.set(Calendar.MINUTE, 59)
                        endOfWeek.set(Calendar.SECOND, 59)
                        
                        startTime = startOfWeek.timeInMillis
                        endTime = endOfWeek.timeInMillis
                    }
                    ViewType.DAY -> {
                        val startOfDay = cal.clone() as Calendar
                        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
                        startOfDay.set(Calendar.MINUTE, 0)
                        startOfDay.set(Calendar.SECOND, 0)
                        startOfDay.set(Calendar.MILLISECOND, 0)
                        
                        val endOfDay = cal.clone() as Calendar
                        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
                        endOfDay.set(Calendar.MINUTE, 59)
                        endOfDay.set(Calendar.SECOND, 59)
                        
                        startTime = startOfDay.timeInMillis
                        endTime = endOfDay.timeInMillis
                    }
                }
                
                _events.value = allEvents.filter { 
                    it.startTime >= startTime && it.startTime < endTime 
                }
            }
            .launchIn(viewModelScope)
        
        loadEvents()
    }
    
    fun setViewType(type: ViewType) {
        _viewType.value = type
        loadEvents()
    }
    
    fun setCurrentDate(date: Calendar) {
        _currentDate.value = date
        loadEvents()
    }
    
    fun navigateToPrevious() {
        val cal = _currentDate.value.clone() as Calendar
        when (_viewType.value) {
            ViewType.MONTH -> cal.add(Calendar.MONTH, -1)
            ViewType.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            ViewType.DAY -> cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        _currentDate.value = cal
        loadEvents()
    }
    
    fun navigateToNext() {
        val cal = _currentDate.value.clone() as Calendar
        when (_viewType.value) {
            ViewType.MONTH -> cal.add(Calendar.MONTH, 1)
            ViewType.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            ViewType.DAY -> cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        _currentDate.value = cal
        loadEvents()
    }
    
    fun navigateToToday() {
        _currentDate.value = Calendar.getInstance()
        loadEvents()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            val cal = _currentDate.value
            val startTime: Long
            val endTime: Long
            
            when (_viewType.value) {
                ViewType.MONTH -> {
                    // 月视图需要包含显示的所有天数（包括上个月和下个月的部分）
                    val firstDayOfMonth = cal.clone() as Calendar
                    firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                    
                    val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
                    val startOffset = firstDayOfWeek - Calendar.SUNDAY
                    
                    val startOfDisplay = firstDayOfMonth.clone() as Calendar
                    startOfDisplay.add(Calendar.DAY_OF_MONTH, -startOffset)
                    startOfDisplay.set(Calendar.HOUR_OF_DAY, 0)
                    startOfDisplay.set(Calendar.MINUTE, 0)
                    startOfDisplay.set(Calendar.SECOND, 0)
                    startOfDisplay.set(Calendar.MILLISECOND, 0)
                    
                    val endOfDisplay = startOfDisplay.clone() as Calendar
                    endOfDisplay.add(Calendar.DAY_OF_MONTH, 41) // 6周 = 42天
                    endOfDisplay.set(Calendar.HOUR_OF_DAY, 23)
                    endOfDisplay.set(Calendar.MINUTE, 59)
                    endOfDisplay.set(Calendar.SECOND, 59)
                    
                    startTime = startOfDisplay.timeInMillis
                    endTime = endOfDisplay.timeInMillis
                }
                ViewType.WEEK -> {
                    val startOfWeek = cal.clone() as Calendar
                    val dayOfWeek = startOfWeek.get(Calendar.DAY_OF_WEEK)
                    val daysFromSunday = dayOfWeek - Calendar.SUNDAY
                    startOfWeek.add(Calendar.DAY_OF_MONTH, -daysFromSunday)
                    startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
                    startOfWeek.set(Calendar.MINUTE, 0)
                    startOfWeek.set(Calendar.SECOND, 0)
                    startOfWeek.set(Calendar.MILLISECOND, 0)
                    
                    val endOfWeek = startOfWeek.clone() as Calendar
                    endOfWeek.add(Calendar.DAY_OF_MONTH, 6)
                    endOfWeek.set(Calendar.HOUR_OF_DAY, 23)
                    endOfWeek.set(Calendar.MINUTE, 59)
                    endOfWeek.set(Calendar.SECOND, 59)
                    
                    startTime = startOfWeek.timeInMillis
                    endTime = endOfWeek.timeInMillis
                }
                ViewType.DAY -> {
                    val startOfDay = cal.clone() as Calendar
                    startOfDay.set(Calendar.HOUR_OF_DAY, 0)
                    startOfDay.set(Calendar.MINUTE, 0)
                    startOfDay.set(Calendar.SECOND, 0)
                    startOfDay.set(Calendar.MILLISECOND, 0)
                    
                    val endOfDay = cal.clone() as Calendar
                    endOfDay.set(Calendar.HOUR_OF_DAY, 23)
                    endOfDay.set(Calendar.MINUTE, 59)
                    endOfDay.set(Calendar.SECOND, 59)
                    
                    startTime = startOfDay.timeInMillis
                    endTime = endOfDay.timeInMillis
                }
            }
            
            _events.value = repository.getEventsInRange(startTime, endTime).first()
        }
    }
    
    fun selectEvent(event: Event?) {
        _selectedEvent.value = event
    }
    
    fun insertEvent(event: Event) {
        viewModelScope.launch {
            val eventId = repository.insertEvent(event)
            val newEvent = event.copy(id = eventId)
            com.example.calendar.util.ReminderManager.scheduleReminder(getApplication(), newEvent)
            loadEvents()
        }
    }
    
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
            com.example.calendar.util.ReminderManager.cancelReminder(getApplication(), event)
            com.example.calendar.util.ReminderManager.scheduleReminder(getApplication(), event)
            loadEvents()
        }
    }
    
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            com.example.calendar.util.ReminderManager.cancelReminder(getApplication(), event)
            loadEvents()
        }
    }
    
    fun deleteEventById(id: Long) {
        viewModelScope.launch {
            val event = repository.getEventById(id)
            if (event != null) {
                repository.deleteEventById(id)
                com.example.calendar.util.ReminderManager.cancelReminder(getApplication(), event)
            }
            loadEvents()
        }
    }
    
    fun importFromFile(uri: Uri) {
        viewModelScope.launch {
            try {
                val contentResolver = getApplication<Application>().contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() }
                
                if (content != null) {
                    val importedEvents = ICalImporter.importFromICal(content)
                    importedEvents.forEach { event ->
                        val eventId = repository.insertEvent(event)
                        if (event.reminderMinutes > 0) {
                            val newEvent = event.copy(id = eventId)
                            com.example.calendar.util.ReminderManager.scheduleReminder(getApplication(), newEvent)
                        }
                    }
                    loadEvents() // 刷新显示
                    
                    _importResult.value = "成功导入 ${importedEvents.size} 个日程"
                } else {
                    _importResult.value = "导入失败: 无法读取文件内容"
                }
            } catch (e: Exception) {
                _importResult.value = "导入失败: ${e.message}"
            }
        }
    }
    
    fun exportEvents() {
        viewModelScope.launch {
            try {
                val allEvents = repository.getAllEvents().first()
                val icalContent = ICalExporter.exportToICal(allEvents)
                
                // 保存到文件
                saveToFile(icalContent)
                _exportResult.value = "成功导出 ${allEvents.size} 个日程"
            } catch (e: Exception) {
                _exportResult.value = "导出失败: ${e.message}"
            }
        }
    }
    
    private fun saveToFile(content: String) {
        try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "calendar_export_${dateFormat.format(Date())}.ics"
            
            // 使用 MediaStore API 保存到 Downloads 目录
            val contentResolver = getApplication<Application>().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/calendar")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }
            
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                // 对于API 28及以下，使用外部存储
                null
            }
            
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
            } else {
                // 降级到传统方式
                throw Exception("MediaStore not available, fallback to legacy method")
            }
        } catch (e: Exception) {
            // 如果 MediaStore 失败，尝试直接保存到应用目录
            try {
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "calendar_export_${dateFormat.format(Date())}.ics"
                val file = File(getApplication<Application>().getExternalFilesDir(null), fileName)
                file.writeText(content)
            } catch (e2: Exception) {
                throw e2
            }
        }
    }
    
    fun clearImportResult() {
        _importResult.value = null
    }
    
    fun clearExportResult() {
        _exportResult.value = null
    }
}

