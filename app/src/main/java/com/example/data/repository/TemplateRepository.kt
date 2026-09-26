package com.example.data.repository

import com.example.data.local.dao.TemplateDao
import com.example.data.local.entity.QuickReplyTemplateEntity
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<QuickReplyTemplateEntity>>
    suspend fun addTemplate(title: String, content: String): Long
    suspend fun deleteTemplate(id: Long)
}

class TemplateRepositoryImpl(
    private val templateDao: TemplateDao
) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<QuickReplyTemplateEntity>> =
        templateDao.getAllTemplates()

    override suspend fun addTemplate(title: String, content: String): Long =
        templateDao.insert(QuickReplyTemplateEntity(title = title, content = content))

    override suspend fun deleteTemplate(id: Long) =
        templateDao.deleteById(id)
}
