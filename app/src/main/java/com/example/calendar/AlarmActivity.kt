package com.example.calendar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.calendar.ui.screens.AlarmScreen
import com.example.calendar.ui.theme.CalendarTheme
import com.example.calendar.util.ReminderManager

class AlarmActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_DESCRIPTION = "event_description"
        const val EXTRA_EVENT_TIME = "event_time"
        
        fun createIntent(
            context: Context,
            eventId: Long,
            eventTitle: String,
            eventDescription: String?,
            eventTime: Long
        ): Intent {
            return Intent(context, AlarmActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
                putExtra(EXTRA_EVENT_TITLE, eventTitle)
                putExtra(EXTRA_EVENT_DESCRIPTION, eventDescription)
                putExtra(EXTRA_EVENT_TIME, eventTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                       Intent.FLAG_ACTIVITY_CLEAR_TOP or
                       Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1)
        val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "日程提醒"
        val eventDescription = intent.getStringExtra(EXTRA_EVENT_DESCRIPTION)
        val eventTime = intent.getLongExtra(EXTRA_EVENT_TIME, System.currentTimeMillis())
        
        setContent {
            CalendarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmScreen(
                        eventTitle = eventTitle,
                        eventDescription = eventDescription,
                        eventTime = eventTime,
                        onDismiss = {
                            finish()
                        },
                        onSnooze = {
                            // 设置5分钟后再次提醒
                            ReminderManager.scheduleSnoozeReminder(
                                context = this@AlarmActivity,
                                eventId = eventId,
                                eventTitle = eventTitle,
                                eventDescription = eventDescription,
                                eventTime = eventTime,
                                snoozeMinutes = 5
                            )
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    override fun onBackPressed() {
        // 防止用户通过返回键关闭闹钟，必须点击按钮
        // 可以选择允许或禁止
        super.onBackPressed()
    }
}