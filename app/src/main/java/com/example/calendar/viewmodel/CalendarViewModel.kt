package com.example.calendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendar.data.Event
import com.example.calendar.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar

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
    
    init {
        // 观察事件变化
        repository.getAllEvents()
            .onEach { allEvents ->
                val cal = _currentDate.value
                val startTime: Long
                val endTime: Long
                
                when (_viewType.value) {
                    ViewType.MONTH -> {
                        val startOfMonth = cal.clone() as Calendar
                        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                        startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
                        startOfMonth.set(Calendar.MINUTE, 0)
                        startOfMonth.set(Calendar.SECOND, 0)
                        startOfMonth.set(Calendar.MILLISECOND, 0)
                        
                        val endOfMonth = cal.clone() as Calendar
                        endOfMonth.add(Calendar.MONTH, 1)
                        endOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                        endOfMonth.add(Calendar.DAY_OF_MONTH, -1)
                        endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
                        endOfMonth.set(Calendar.MINUTE, 59)
                        endOfMonth.set(Calendar.SECOND, 59)
                        
                        startTime = startOfMonth.timeInMillis
                        endTime = endOfMonth.timeInMillis
                    }
                    ViewType.WEEK -> {
                        val startOfWeek = cal.clone() as Calendar
                        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
                        startOfWeek.set(Calendar.MINUTE, 0)
                        startOfWeek.set(Calendar.SECOND, 0)
                        startOfWeek.set(Calendar.MILLISECOND, 0)
                        
                        val endOfWeek = startOfWeek.clone() as Calendar
                        endOfWeek.add(Calendar.DAY_OF_WEEK, 6)
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
                    val startOfMonth = cal.clone() as Calendar
                    startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                    startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
                    startOfMonth.set(Calendar.MINUTE, 0)
                    startOfMonth.set(Calendar.SECOND, 0)
                    startOfMonth.set(Calendar.MILLISECOND, 0)
                    
                    val endOfMonth = cal.clone() as Calendar
                    endOfMonth.add(Calendar.MONTH, 1)
                    endOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                    endOfMonth.add(Calendar.DAY_OF_MONTH, -1)
                    endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
                    endOfMonth.set(Calendar.MINUTE, 59)
                    endOfMonth.set(Calendar.SECOND, 59)
                    
                    startTime = startOfMonth.timeInMillis
                    endTime = endOfMonth.timeInMillis
                }
                ViewType.WEEK -> {
                    val startOfWeek = cal.clone() as Calendar
                    startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
                    startOfWeek.set(Calendar.MINUTE, 0)
                    startOfWeek.set(Calendar.SECOND, 0)
                    startOfWeek.set(Calendar.MILLISECOND, 0)
                    
                    val endOfWeek = startOfWeek.clone() as Calendar
                    endOfWeek.add(Calendar.DAY_OF_WEEK, 6)
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
}

