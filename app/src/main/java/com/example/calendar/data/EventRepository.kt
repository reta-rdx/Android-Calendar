package com.example.calendar.data

import android.app.Application
import kotlinx.coroutines.flow.Flow

class EventRepository(application: Application) {
    private val eventDao: EventDao = CalendarDatabase.getDatabase(application).eventDao()
    
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    
    suspend fun getEventById(id: Long): Event? = eventDao.getEventById(id)
    
    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<Event>> =
        eventDao.getEventsInRange(startTime, endTime)
    
    suspend fun getEventsForDay(startOfDay: Long, endOfDay: Long): List<Event> =
        eventDao.getEventsForDay(startOfDay, endOfDay)
    
    suspend fun getEventsForWeek(startOfWeek: Long, endOfWeek: Long): List<Event> =
        eventDao.getEventsForWeek(startOfWeek, endOfWeek)
    
    suspend fun getEventsForMonth(startOfMonth: Long, endOfMonth: Long): List<Event> =
        eventDao.getEventsForMonth(startOfMonth, endOfMonth)
    
    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)
    
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    
    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)
}

