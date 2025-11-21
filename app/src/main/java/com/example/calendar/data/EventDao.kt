package com.example.calendar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): Event?
    
    @Query("SELECT * FROM events WHERE startTime >= :startTime AND startTime < :endTime ORDER BY startTime ASC")
    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime ASC")
    suspend fun getEventsForDay(startOfDay: Long, endOfDay: Long): List<Event>
    
    @Query("SELECT * FROM events WHERE startTime >= :startOfWeek AND startTime < :endOfWeek ORDER BY startTime ASC")
    suspend fun getEventsForWeek(startOfWeek: Long, endOfWeek: Long): List<Event>
    
    @Query("SELECT * FROM events WHERE startTime >= :startOfMonth AND startTime < :endOfMonth ORDER BY startTime ASC")
    suspend fun getEventsForMonth(startOfMonth: Long, endOfMonth: Long): List<Event>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long
    
    @Update
    suspend fun updateEvent(event: Event)
    
    @Delete
    suspend fun deleteEvent(event: Event)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}

