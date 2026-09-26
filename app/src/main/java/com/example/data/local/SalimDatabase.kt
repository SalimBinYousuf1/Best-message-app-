package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.ScheduledMessageDao
import com.example.data.local.dao.TemplateDao
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.QuickReplyTemplateEntity
import com.example.data.local.entity.ScheduledMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ScheduledMessageEntity::class,
        QuickReplyTemplateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SalimDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile
        private var INSTANCE: SalimDatabase? = null

        fun getInstance(context: Context): SalimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalimDatabase::class.java,
                    "salim_messages.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default quick reply templates
                            CoroutineScope(Dispatchers.IO).launch {
                                val templateDao = getInstance(context).templateDao()
                                templateDao.insertAll(
                                    listOf(
                                        QuickReplyTemplateEntity(
                                            title = "On my way",
                                            content = "I'm on my way!",
                                            orderIndex = 0
                                        ),
                                        QuickReplyTemplateEntity(
                                            title = "Can't talk",
                                            content = "Can't talk right now. What's up?",
                                            orderIndex = 1
                                        ),
                                        QuickReplyTemplateEntity(
                                            title = "Call back",
                                            content = "I'll call you right back.",
                                            orderIndex = 2
                                        ),
                                        QuickReplyTemplateEntity(
                                            title = "Sounds good",
                                            content = "Sounds good, thanks!",
                                            orderIndex = 3
                                        ),
                                        QuickReplyTemplateEntity(
                                            title = "Running late",
                                            content = "Running a few minutes late, see you soon.",
                                            orderIndex = 4
                                        )
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
