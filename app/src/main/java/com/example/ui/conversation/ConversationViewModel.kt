package com.example.ui.conversation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.AttachmentType
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.QuickReplyTemplateEntity
import com.example.data.local.preferences.SalimUserPreferences
import com.example.data.repository.ConversationRepository
import com.example.data.repository.MessagingRepository
import com.example.data.repository.ScheduleRepository
import com.example.data.repository.TemplateRepository
import com.example.telephony.ContactInfo
import com.example.telephony.ContactResolver
import com.example.telephony.OtpDetectionResult
import com.example.telephony.OtpDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConversationUiState(
    val conversation: ConversationEntity? = null,
    val contactInfo: ContactInfo? = null,
    val messages: List<MessageEntity> = emptyList(),
    val draftText: String = "",
    val detectedOtp: OtpDetectionResult? = null,
    val quickTemplates: List<QuickReplyTemplateEntity> = emptyList(),
    val preferences: SalimUserPreferences = SalimUserPreferences(),
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

class ConversationViewModel(
    application: Application,
    private val conversationId: Long,
    private val recipientAddress: String
) : AndroidViewModel(application) {

    private val conversationRepository: ConversationRepository =
        (application as SalimApplication).conversationRepository
    private val messagingRepository: MessagingRepository =
        (application as SalimApplication).messagingRepository
    private val scheduleRepository: ScheduleRepository =
        (application as SalimApplication).scheduleRepository
    private val templateRepository: TemplateRepository =
        (application as SalimApplication).templateRepository
    private val preferences = (application as SalimApplication).preferences

    private val _contactInfo = MutableStateFlow<ContactInfo?>(null)
    private val _draftText = MutableStateFlow("")
    val draftText: StateFlow<String> = _draftText.asStateFlow()

    private val _detectedOtp = MutableStateFlow<OtpDetectionResult?>(null)
    private val _isSending = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private var draftSaveJob: Job? = null

    val uiState: StateFlow<ConversationUiState> = combine(
        conversationRepository.getConversationFlow(conversationId),
        _contactInfo,
        messagingRepository.getMessagesForConversation(conversationId),
        _draftText,
        _detectedOtp,
        templateRepository.getAllTemplates(),
        preferences.preferencesFlow,
        _isSending,
        _errorMessage
    ) { params ->
        val conv = params[0] as? ConversationEntity
        val contact = params[1] as? ContactInfo
        @Suppress("UNCHECKED_CAST")
        val msgs = params[2] as List<MessageEntity>
        val draft = params[3] as String
        val otp = params[4] as? OtpDetectionResult
        @Suppress("UNCHECKED_CAST")
        val templates = params[5] as List<QuickReplyTemplateEntity>
        val prefs = params[6] as SalimUserPreferences
        val sending = params[7] as Boolean
        val error = params[8] as? String

        ConversationUiState(
            conversation = conv,
            contactInfo = contact,
            messages = msgs,
            draftText = draft,
            detectedOtp = otp,
            quickTemplates = templates,
            preferences = prefs,
            isSending = sending,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConversationUiState()
    )

    init {
        loadConversation()
        markRead()
    }

    private fun loadConversation() {
        viewModelScope.launch {
            val contact = ContactResolver.resolveContact(getApplication(), recipientAddress)
            _contactInfo.value = contact

            val conv = conversationRepository.getConversationById(conversationId)
            if (conv != null && !conv.draftText.isNullOrBlank()) {
                _draftText.value = conv.draftText
            }

            // Check latest incoming message for OTP
            messagingRepository.getMessagesForConversation(conversationId).collect { list ->
                val latestIncoming = list.lastOrNull { it.isIncoming }
                if (latestIncoming != null) {
                    _detectedOtp.value = OtpDetector.detectOtp(latestIncoming.body)
                }
            }
        }
    }

    fun markRead() {
        viewModelScope.launch {
            messagingRepository.markConversationAsRead(conversationId)
        }
    }

    fun onDraftChanged(newDraft: String) {
        _draftText.value = newDraft
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(500) // Debounce
            conversationRepository.updateDraft(conversationId, newDraft.ifBlank { null })
        }
    }

    fun sendMessage(
        body: String,
        attachmentUri: String? = null,
        attachmentType: AttachmentType = AttachmentType.NONE,
        attachmentName: String? = null
    ) {
        val trimmed = body.trim()
        if (trimmed.isEmpty() && attachmentUri == null) return

        viewModelScope.launch {
            _isSending.value = true
            _errorMessage.value = null
            _draftText.value = ""
            conversationRepository.updateDraft(conversationId, null)

            val result = messagingRepository.sendMessage(
                conversationId = conversationId,
                recipientAddress = recipientAddress,
                body = trimmed,
                attachmentUri = attachmentUri,
                attachmentType = attachmentType,
                attachmentName = attachmentName
            )

            _isSending.value = false
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send message"
            }
        }
    }

    fun retryMessage(messageId: Long) {
        viewModelScope.launch {
            val res = messagingRepository.retryMessage(messageId)
            if (res.isFailure) {
                _errorMessage.value = res.exceptionOrNull()?.message ?: "Retry failed"
            }
        }
    }

    fun scheduleMessage(body: String, timestamp: Long) {
        viewModelScope.launch {
            scheduleRepository.scheduleMessage(
                conversationId = conversationId,
                recipientAddress = recipientAddress,
                recipientName = _contactInfo.value?.name,
                body = body,
                scheduledTimestamp = timestamp
            )
            _draftText.value = ""
            conversationRepository.updateDraft(conversationId, null)
        }
    }

    fun toggleStar(message: MessageEntity) {
        viewModelScope.launch {
            messagingRepository.setStarred(message.id, !message.isStarred)
        }
    }

    fun toggleReaction(message: MessageEntity, emoji: String) {
        viewModelScope.launch {
            val newReaction = if (message.reaction == emoji) null else emoji
            messagingRepository.updateReaction(message.id, newReaction)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            messagingRepository.deleteMessage(messageId)
        }
    }

    fun dismissOtpBanner() {
        _detectedOtp.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
