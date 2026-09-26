package com.example

import android.app.Application
import com.example.data.local.SalimDatabase
import com.example.data.local.preferences.SalimPreferences
import com.example.data.repository.ConversationRepository
import com.example.data.repository.ConversationRepositoryImpl
import com.example.data.repository.MessagingRepository
import com.example.data.repository.MessagingRepositoryImpl
import com.example.data.repository.ScheduleRepository
import com.example.data.repository.ScheduleRepositoryImpl
import com.example.data.repository.TemplateRepository
import com.example.data.repository.TemplateRepositoryImpl
import com.example.telephony.NotificationCoordinator
import com.example.telephony.SmsTransport

class SalimApplication : Application() {

    lateinit var database: SalimDatabase
        private set

    lateinit var preferences: SalimPreferences
        private set

    lateinit var conversationRepository: ConversationRepository
        private set

    lateinit var messagingRepository: MessagingRepository
        private set

    lateinit var scheduleRepository: ScheduleRepository
        private set

    lateinit var templateRepository: TemplateRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = SalimDatabase.getInstance(this)
        preferences = SalimPreferences(this)

        val smsTransport = SmsTransport(this)
        conversationRepository = ConversationRepositoryImpl(database.conversationDao())
        messagingRepository = MessagingRepositoryImpl(
            database.messageDao(),
            database.conversationDao(),
            smsTransport
        )
        scheduleRepository = ScheduleRepositoryImpl(this, database.scheduledMessageDao(), messagingRepository)
        templateRepository = TemplateRepositoryImpl(database.templateDao())

        // Initialize Notification Channels
        NotificationCoordinator.initNotificationChannels(this)
    }

    companion object {
        lateinit var instance: SalimApplication
            private set
    }
}
