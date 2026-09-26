package com.example.ui.scheduled

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.ScheduledMessageEntity
import com.example.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduledViewModel(application: Application) : AndroidViewModel(application) {

    private val scheduleRepository: ScheduleRepository =
        (application as SalimApplication).scheduleRepository

    val scheduledMessages: StateFlow<List<ScheduledMessageEntity>> =
        scheduleRepository.getPendingScheduledMessages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun sendNow(id: Long) {
        viewModelScope.launch {
            scheduleRepository.sendScheduledMessageNow(id)
        }
    }

    fun cancel(id: Long) {
        viewModelScope.launch {
            scheduleRepository.cancelScheduledMessage(id)
        }
    }
}
